package pl.example.connectnear

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import pl.example.connectnear.ui.theme.getCategoryGradient
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(
    myUserId: String,
    myUserName: String,
    otherUserId: String,
    otherUserName: String,
    userCategory: String,
    isGroup: Boolean = false,
    initialMessage: String = "",
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var messageText by remember { mutableStateOf(initialMessage) }
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    val chatId = if (isGroup) otherUserId else FirebaseService.getChatId(myUserId, otherUserId)
    val listState = rememberLazyListState()

    var replyToMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var typingUsers by remember { mutableStateOf<List<String>>(emptyList()) }
    var isBlocked by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val recorder = remember { AudioRecorder(context) }
    val player = remember { AudioPlayer(context) }
    var isRecording by remember { mutableStateOf(false) }

    // Stany AI (tymczasowo wyłączone)
    // val isAiAvailable by remember { mutableStateOf(AIRepo.isAiAvailable()) }
    // var showAiPanel by remember { mutableStateOf(false) }
    // var isAiLoading by remember { mutableStateOf(false) }
    // var aiSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    // var selectedTone by remember { mutableStateOf("Popraw") }

    var showImageUploadDialog by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var ephemeralType by remember { mutableStateOf("standard") }
    var viewingEphemeralImage by remember { mutableStateOf<ChatMessage?>(null) }

    LaunchedEffect(chatId) {
        FirebaseService.getMessages(chatId) { newMessages ->
            messages = newMessages
            FirebaseService.markMessagesAsRead(chatId, myUserId)
        }
    }

    LaunchedEffect(chatId) {
        FirebaseService.listenForTypingStatus(chatId, myUserId) { users -> typingUsers = users }
    }

    LaunchedEffect(otherUserId) {
        if (!isGroup) {
            FirebaseService.getBlockedUsers(myUserId) { blockedUsers -> isBlocked = blockedUsers.contains(otherUserId) }
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            if (recorder.start()) isRecording = true else Toast.makeText(context, "Błąd", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Brak uprawnień", Toast.LENGTH_SHORT).show()
        }
    }

    val filePicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { scope.launch {
            val fileName = getFileName(it, context)
            FirebaseService.uploadFileToStorage(uri = it, folder = "chat_files", userId = myUserId, fileName = fileName,
                onSuccess = { url ->
                    val msg = createMessage(myUserId, myUserName, otherUserName, replyToMessage, fileUrl = url, fileName = fileName)
                    FirebaseService.sendMessage(chatId, msg, otherUserId, myUserName, isGroup)
                    replyToMessage = null
                }, onError = { e -> Toast.makeText(context, "Błąd: $e", Toast.LENGTH_SHORT).show() }
            )
        } }
    }

    val imagePicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            showImageUploadDialog = true
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(getCategoryGradient(userCategory))) {
        ChatHeader(otherUserName, isGroup, typingUsers, onBackClick) { showMenu = true }

        LazyColumn(state = listState, modifier = Modifier.weight(1f).padding(horizontal = 8.dp), reverseLayout = true) {
            items(messages.sortedByDescending { it.timestamp }) { message ->
                MessageItem(
                    message = message, isMyMessage = message.senderId == myUserId,
                    onReply = { replyToMessage = it },
                    onPin = { FirebaseService.pinMessage(chatId, message.id, !message.isPinned) },
                    onViewEphemeral = { viewingEphemeralImage = it },
                    player = player
                )
            }
        }
        
        // Panel AI (tymczasowo wyłączony)
        // AiSuggestionsPanel(
        //     isVisible = showAiPanel,
        //     isLoading = isAiLoading,
        //     suggestions = aiSuggestions,
        //     selectedTone = selectedTone,
        //     onToneChange = { selectedTone = it },
        //     onGenerateClick = {
        //         if (messageText.isBlank()) {
        //             Toast.makeText(context, "Wpisz tekst, aby uzyskać sugestie", Toast.LENGTH_SHORT).show()
        //             return@AiSuggestionsPanel
        //         }
        //         scope.launch {
        //             isAiLoading = true
        //             aiSuggestions = emptyList()
        //             val prompt = when (selectedTone) {
        //                 "Popraw" -> "Popraw poniższy tekst, zachowując jego znaczenie: \"$messageText\""
        //                 "Formalny" -> "Napisz tę wiadomość w formalnym tonie: \"$messageText\""
        //                 "Luźny" -> "Napisz tę wiadomość w luźnym, koleżeńskim tonie: \"$messageText\""
        //                 "Zabawny" -> "Napisz tę wiadomość w zabawny sposób: \"$messageText\""
        //                 "Poetycki" -> "Napisz tę wiadomość w poetyckim stylu: \"$messageText\""
        //                 else -> messageText
        //             }
        //             try {
        //                 val result = AIRepo.generateContent(prompt)
        //                 aiSuggestions = result.split('\n').map { it.trim().removePrefix("-").trim() }.filter { it.isNotEmpty() }
        //             } catch (e: Exception) {
        //                 Toast.makeText(context, "Błąd AI: ${e.message}", Toast.LENGTH_LONG).show()
        //             } finally {
        //                 isAiLoading = false
        //             }
        //         }
        //     },
        //     onSuggestionClick = { suggestion ->
        //         messageText = suggestion
        //         showAiPanel = false
        //     },
        //     onDismiss = { showAiPanel = false }
        // )

        if (replyToMessage != null) {
            ReplyPreview(replyToMessage, myUserId, myUserName, otherUserName) { replyToMessage = null }
        }

        BottomInputPanel(
            messageText = messageText, 
            isRecording = isRecording, 
            // isAiAvailable = isAiAvailable, // Tymczasowo wyłączone
            onMessageChange = { messageText = it },
            onSendClick = {
                if (messageText.isNotBlank()) {
                    val msg = createMessage(myUserId, myUserName, otherUserName, replyToMessage, text = messageText)
                    FirebaseService.sendMessage(chatId, msg, otherUserId, myUserName, isGroup)
                    messageText = ""
                    replyToMessage = null
                }
            },
            onFileClick = { filePicker.launch("*/*") },
            onImageClick = { imagePicker.launch("image/*") },
            onMicClick = { micPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO) },
            onStopRecordingClick = {
                isRecording = false
                recorder.stop()?.let { (file, duration) ->
                    FirebaseService.uploadFileToStorage(Uri.fromFile(file), "chat_audio", myUserId, fileName = "${System.currentTimeMillis()}.mp3",
                        onSuccess = { url -> 
                            val msg = createMessage(myUserId, myUserName, otherUserName, replyToMessage, voiceUrl = url, voiceDuration = duration)
                            FirebaseService.sendMessage(chatId, msg, otherUserId, myUserName, isGroup)
                        }, onError = { Toast.makeText(context, "Błąd wysyłania audio", Toast.LENGTH_SHORT).show() }
                    )
                }
            }
            // onAiClick = { showAiPanel = !showAiPanel } // Tymczasowo wyłączone
        )
    }

    if (showImageUploadDialog) {
        ImageUploadDialog(
            imageUri = selectedImageUri,
            onDismiss = { showImageUploadDialog = false },
            onSend = { type ->
                showImageUploadDialog = false
                selectedImageUri?.let { uri ->
                    scope.launch {
                        FirebaseService.uploadFileToStorage(uri, "chat_images", myUserId,
                            onSuccess = { url ->
                                val msg = createMessage(myUserId, myUserName, otherUserName, replyToMessage, imageUrl = url, isEphemeral = true, ephemeralType = type)
                                FirebaseService.sendMessage(chatId, msg, otherUserId, myUserName, isGroup)
                                replyToMessage = null
                            },
                            onError = { Toast.makeText(context, "Błąd wysyłania", Toast.LENGTH_SHORT).show() }
                        )
                    }
                }
            }
        )
    }
    
    if (viewingEphemeralImage != null) {
        val canView = viewingEphemeralImage?.senderId == myUserId || !viewingEphemeralImage!!.viewedBy.contains(myUserId)
        if (canView) {
            Dialog(onDismissRequest = { 
                FirebaseService.markMessageAsViewed(chatId, viewingEphemeralImage!!.id, myUserId)
                if (viewingEphemeralImage!!.ephemeralType == "one_time") {
                    FirebaseService.deleteMessage(chatId, viewingEphemeralImage!!.id)
                }
                viewingEphemeralImage = null
            }) {
                AsyncImage(model = viewingEphemeralImage!!.imageUrl, contentDescription = "Ephemeral Image", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
            }
        }
    }
}

@Composable
private fun ChatHeader(userName: String, isGroup: Boolean, typingUsers: List<String>, onBackClick: () -> Unit, onMenuClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.2f)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Button(onClick = onBackClick, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)) { Text("<") }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(userName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            if (typingUsers.isNotEmpty()) Text(if (isGroup) "${typingUsers.size} osób pisze..." else "pisze...", fontSize = 12.sp, color = Color.White.copy(0.8f))
        }
        IconButton(onClick = onMenuClick) { Icon(Icons.Default.MoreVert, "Opcje", tint = Color.White) }
    }
}

@Composable
private fun BottomInputPanel(
    messageText: String, 
    isRecording: Boolean, 
    // isAiAvailable: Boolean, // Tymczasowo wyłączone
    onMessageChange: (String) -> Unit, 
    onSendClick: () -> Unit, 
    onFileClick: () -> Unit, 
    onImageClick: () -> Unit, 
    onMicClick: () -> Unit, 
    onStopRecordingClick: () -> Unit
    // onAiClick: () -> Unit // Tymczasowo wyłączone
) {
    if (isRecording) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Button(onClick = onStopRecordingClick, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Icon(Icons.Default.Stop, "Stop"); Text("Stop", modifier = Modifier.padding(start = 8.dp)) }
        }
    } else {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            // if (isAiAvailable) { // Tymczasowo wyłączone
            //     IconButton(onClick = onAiClick) { Icon(Icons.Default.Star, "Asystent AI", tint = Color.White) } // Zmieniono ikonę
            // }
            IconButton(onClick = onImageClick) { Icon(Icons.Default.Photo, "Obraz", tint = Color.White) }
            IconButton(onClick = onFileClick) { Icon(Icons.Default.AttachFile, "Plik", tint = Color.White) }
            TextField(
                value = messageText, 
                onValueChange = onMessageChange, 
                modifier = Modifier.weight(1f), 
                placeholder = { Text("Wiadomość...", color = Color.LightGray) }, 
                shape = RoundedCornerShape(20.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                    unfocusedContainerColor = Color.Black.copy(alpha = 0.2f),
                    disabledContainerColor = Color.Black.copy(alpha = 0.2f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )
            Spacer(Modifier.width(8.dp))
            if (messageText.isNotBlank()) Button(onClick = onSendClick, shape = RoundedCornerShape(50)) { Icon(Icons.Default.Send, "Wyślij") } else Button(onClick = onMicClick, shape = RoundedCornerShape(50)) { Icon(Icons.Default.Mic, "Nagraj") }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageItem(message: ChatMessage, isMyMessage: Boolean, onReply: (ChatMessage) -> Unit, onPin: () -> Unit, onViewEphemeral: (ChatMessage) -> Unit, player: AudioPlayer) {
    val context = LocalContext.current
    val alignment = if (isMyMessage) Alignment.CenterEnd else Alignment.CenterStart
    val backgroundColor = if (isMyMessage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
    var showMenu by remember { mutableStateOf(false) }
    var offsetX by remember { mutableStateOf(0f) }

    val draggableState = rememberDraggableState { delta -> if ((isMyMessage && delta < 0) || (!isMyMessage && delta > 0)) offsetX += delta }

    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).offset { IntOffset(offsetX.roundToInt(), 0) }.draggable(state = draggableState, orientation = Orientation.Horizontal, onDragStopped = { if (kotlin.math.abs(offsetX) > 150) onReply(message); offsetX = 0f }),
        contentAlignment = alignment
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!isMyMessage && offsetX > 20) Icon(Icons.Default.Reply, null, tint = Color.White.copy(alpha = (offsetX / 200f).coerceIn(0f, 1f)))
            Column(horizontalAlignment = if (isMyMessage) Alignment.End else Alignment.Start) {
                if (message.replyToText.isNotEmpty()) {
                     Box(modifier = Modifier.background(color = Color.Black.copy(alpha = 0.3f), shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                         Text("${message.replyToSenderName}: ${message.replyToText}", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Box {
                    Surface(shape = RoundedCornerShape(8.dp), color = backgroundColor, tonalElevation = 2.dp, modifier = Modifier.combinedClickable(onClick = { 
                        if(message.isEphemeral) onViewEphemeral(message)
                    }, onLongClick = { showMenu = true })) {
                        MessageContent(message, player)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(text = { Text("Odpowiedz") }, onClick = { onReply(message); showMenu = false })
                        DropdownMenuItem(text = { Text(if (message.isPinned) "Odepnij" else "Przypnij") }, onClick = { onPin(); showMenu = false })
                        if (message.text.isNotEmpty()) DropdownMenuItem(text = { Text("Kopiuj") }, onClick = {
                            (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("Wiadomość", message.text))
                            Toast.makeText(context, "Skopiowano", Toast.LENGTH_SHORT).show(); showMenu = false
                        })
                    }
                }
            }
            if (isMyMessage && offsetX < -20) Icon(Icons.Default.Reply, null, tint = Color.White.copy(alpha = (-offsetX / 200f).coerceIn(0f, 1f)))
        }
    }
}

@Composable
private fun MessageContent(message: ChatMessage, player: AudioPlayer) {
    val myUserId = FirebaseService.auth.currentUser?.uid ?: ""
    val canView = !message.isEphemeral || message.senderId == myUserId || message.viewedBy.contains(myUserId)

    Column(modifier = Modifier.padding(8.dp)) {
        if (message.isPinned) Icon(Icons.Default.PushPin, "Przypięta", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        
        if (message.isEphemeral && !canView) {
            Button(onClick = { /* on view */ }) { Text("Dotknij, aby wyświetlić") }
        } else {
            if (message.imageUrl.isNotEmpty()) AsyncImage(model = message.imageUrl, contentDescription = "Zdjęcie", modifier = Modifier.heightIn(max = 200.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
            if (message.fileUrl.isNotEmpty()) Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) { Icon(Icons.Default.Description, "Plik"); Spacer(Modifier.width(8.dp)); Text(message.fileName) }
            if (message.text.isNotEmpty()) Text(text = message.text)
            if (message.voiceUrl.isNotEmpty()) Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = { player.playFile(message.voiceUrl) }) { Icon(Icons.Default.PlayArrow, "Odtwórz") }; Text("${message.voiceDuration / 1000}s") }
        }
    }
}

@Composable
fun ImageUploadDialog(imageUri: Uri?, onDismiss: () -> Unit, onSend: (String) -> Unit) {
    if (imageUri == null) return
    var ephemeralType by remember { mutableStateOf("standard") }

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                AsyncImage(model = imageUri, contentDescription = "Wybrane zdjęcie", modifier = Modifier.height(200.dp).fillMaxWidth().clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Wybierz typ wiadomości:", style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = ephemeralType == "standard", onClick = { ephemeralType = "standard" })
                    Text("Standardowa")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = ephemeralType == "one_time", onClick = { ephemeralType = "one_time" })
                    Text("Jednorazowa")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = ephemeralType == "keep", onClick = { ephemeralType = "keep" })
                    Text("Zachowaj w czacie")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Anuluj") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onSend(ephemeralType) }) { Text("Wyślij") }
                }
            }
        }
    }
}

private fun createMessage(myUserId: String, myUserName: String, otherUserName: String, replyToMessage: ChatMessage?, text: String = "", imageUrl: String = "", fileUrl: String = "", fileName: String = "", voiceUrl: String = "", voiceDuration: Long = 0, isEphemeral: Boolean = false, ephemeralType: String = "standard"): ChatMessage {
    return ChatMessage(
        senderId = myUserId, text = text, imageUrl = imageUrl, fileUrl = fileUrl, fileName = fileName, voiceUrl = voiceUrl, voiceDuration = voiceDuration, timestamp = System.currentTimeMillis(), isEphemeral = isEphemeral, ephemeralType = ephemeralType,
        replyToId = replyToMessage?.id ?: "",
        replyToText = replyToMessage?.let { msg -> when { msg.text.isNotEmpty() -> msg.text; msg.imageUrl.isNotEmpty() -> "[Zdjęcie]"; msg.fileUrl.isNotEmpty() -> "[Plik]"; msg.voiceUrl.isNotEmpty() -> "[Głosówka]"; else -> "" } } ?: "",
        replyToSenderName = if (replyToMessage != null) (if (replyToMessage.senderId == myUserId) myUserName else otherUserName) else ""
    )
}

@Composable
private fun ReplyPreview(replyToMessage: ChatMessage?, myUserId: String, myUserName: String, otherUserName: String, onCancel: () -> Unit) {
    val replyText = when { replyToMessage?.text?.isNotEmpty() == true -> replyToMessage.text; replyToMessage?.imageUrl?.isNotEmpty() == true -> "[Zdjęcie]"; replyToMessage?.fileUrl?.isNotEmpty() == true -> "[Plik]"; replyToMessage?.voiceUrl?.isNotEmpty() == true -> "[Głosówka]"; else -> "" }
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).background(Color.Black.copy(alpha = 0.2f)), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f).padding(8.dp)) {
            Text("Odpowiadasz dla: ${if (replyToMessage?.senderId == myUserId) myUserName else otherUserName}", color = Color.White, fontWeight = FontWeight.Bold)
            Text(replyText, color = Color.White.copy(alpha = 0.8f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        IconButton(onClick = onCancel) { Icon(Icons.Default.Close, "Anuluj", tint = Color.White) }
    }
}

fun getFileName(uri: Uri, context: Context): String {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if(columnIndex != -1) result = cursor.getString(columnIndex)
            }
        } finally { cursor?.close() }
    }
    if (result == null) { result = uri.path; val cut = result?.lastIndexOf('/'); if (cut != -1) { if (result != null) { result = result.substring(cut!! + 1) } } }
    return result ?: "unknown_file"
}
