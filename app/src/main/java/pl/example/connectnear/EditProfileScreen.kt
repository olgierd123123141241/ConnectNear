package pl.example.connectnear

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.example.connectnear.ui.theme.getCategoryGradient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var user by remember { mutableStateOf<UserSelection?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    var instagramLink by remember { mutableStateOf("") }
    var facebookLink by remember { mutableStateOf("") }
    var tiktokLink by remember { mutableStateOf("") }
    var youtubeLink by remember { mutableStateOf("") }
    var twitterLink by remember { mutableStateOf("") }
    var snapchatLink by remember { mutableStateOf("") }

    var saveState by remember { mutableStateOf("Zapisano") }
    var debounceJob by remember { mutableStateOf<Job?>(null) }

    fun triggerSave() {
        debounceJob?.cancel()
        debounceJob = scope.launch {
            saveState = "Zapisywanie..."
            delay(1000)
            user?.let {
                val updatedUser = it.copy(
                    instagramLink = instagramLink,
                    facebookLink = facebookLink,
                    tiktokLink = tiktokLink,
                    youtubeLink = youtubeLink,
                    twitterLink = twitterLink,
                    snapchatLink = snapchatLink
                )
                FirebaseService.updateFullProfile(updatedUser,
                    onSuccess = { 
                        saveState = "Zapisano"
                    },
                    onError = { saveState = "Błąd zapisu" }
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        FirebaseService.getCurrentUserProfile { loadedUser ->
            user = loadedUser
            instagramLink = loadedUser?.instagramLink ?: ""
            facebookLink = loadedUser?.facebookLink ?: ""
            tiktokLink = loadedUser?.tiktokLink ?: ""
            youtubeLink = loadedUser?.youtubeLink ?: ""
            twitterLink = loadedUser?.twitterLink ?: ""
            snapchatLink = loadedUser?.snapchatLink ?: ""
            isLoading = false
        }
    }

    LaunchedEffect(instagramLink, facebookLink, tiktokLink, youtubeLink, twitterLink, snapchatLink) {
        if (!isLoading) triggerSave()
    }

    Box(modifier = Modifier.fillMaxSize().background(getCategoryGradient(user?.category ?: ""))) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Edytuj Social Media", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Wróć", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Połącz swoje konta", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Wpisz swoje nazwy użytkownika. Zapisują się automatycznie.", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = instagramLink,
                        onValueChange = { instagramLink = it },
                        label = { Text("Instagram") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                        ),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.White) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = facebookLink,
                        onValueChange = { facebookLink = it },
                        label = { Text("Facebook") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                        ),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.White) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = tiktokLink,
                        onValueChange = { tiktokLink = it },
                        label = { Text("TikTok") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                        ),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.White) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = youtubeLink,
                        onValueChange = { youtubeLink = it },
                        label = { Text("YouTube") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                        ),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.White) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = twitterLink,
                        onValueChange = { twitterLink = it },
                        label = { Text("Twitter / X") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                        ),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.White) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = snapchatLink,
                        onValueChange = { snapchatLink = it },
                        label = { Text("Snapchat") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                        ),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.White) }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Surface(
                            color = getSaveStateColor(saveState),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = saveState,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(50.dp))
                }
            }
        }
    }
}

@Composable
private fun getSaveStateColor(state: String): Color {
    return when (state) {
        "Zapisano" -> Color(0xFF4CAF50).copy(alpha = 0.8f)
        "Zapisywanie..." -> Color(0xFFFFC107).copy(alpha = 0.8f)
        else -> Color(0xFFF44336).copy(alpha = 0.8f)
    }
}
