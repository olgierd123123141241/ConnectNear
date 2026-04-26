package pl.example.connectnear

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.* // ktlint-disable no-wildcard-imports
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.example.connectnear.ui.theme.ConnectNearTheme
import pl.example.connectnear.ui.theme.getCategoryGradient

// GŁÓWNY EKRAN PROFILU - Obsługuje wyświetlanie danych, edycję i automatyczny zapis
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogoutClick: () -> Unit,
    onEditPreferencesClick: () -> Unit,
    onBlockedUsersClick: () -> Unit,
    onEditSocialsClick: () -> Unit,
    onFriendsClick: () -> Unit,
    onChatsClick: () -> Unit,
    onGroupsClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Stan użytkownika i pól edycyjnych
    var userSelection by remember { mutableStateOf<UserSelection?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var profileImageUrl by remember { mutableStateOf("") }
    var interests by remember { mutableStateOf<List<String>>(emptyList()) }
    var userStatus by remember { mutableStateOf("") }
    var smoking by remember { mutableStateOf("") }
    var drinking by remember { mutableStateOf("") }
    var personalityType by remember { mutableStateOf("") }
    var isProfilePublic by remember { mutableStateOf(true) }

    var selectedTab by remember { mutableStateOf(0) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Mechanizm opóźnionego zapisu (Debounce)
    var debounceJob by remember { mutableStateOf<Job?>(null) }

    // FUNKCJA ZAPISUJĄCA: Wywoływana automatycznie przy zmianie danych
    fun triggerAutoSave() {
        debounceJob?.cancel()
        debounceJob = scope.launch {
            delay(1000) // Czekaj 1s przed wysłaniem do Firebase
            userSelection?.let {
                val updatedUser = it.copy(
                    name = name,
                    description = description,
                    profileImageUrl = profileImageUrl,
                    interests = interests.joinToString(","),
                    userStatus = userStatus,
                    smoking = smoking,
                    drinking = drinking,
                    personalityType = personalityType,
                    isProfilePublic = isProfilePublic
                )
                FirebaseService.updateFullProfile(updatedUser, 
                    onSuccess = { message = "Zapisano automatycznie" }, 
                    onError = { err -> message = "Błąd zapisu" }
                )
            }
        }
    }

    // Ładowanie danych z bazy przy otwarciu ekranu
    LaunchedEffect(Unit) {
        userEmail = FirebaseService.auth.currentUser?.email ?: "Brak"
        FirebaseService.getCurrentUserProfile { data ->
            if (data != null) {
                userSelection = data; name = data.name; description = data.description
                profileImageUrl = data.profileImageUrl; interests = if (data.interests.isNullOrEmpty()) emptyList() else data.interests.split(",").map { it.trim() }
                userStatus = data.userStatus; smoking = data.smoking; drinking = data.drinking; personalityType = data.personalityType
                isProfilePublic = data.isProfilePublic
            }
            isLoading = false
        }
    }

    // AUTOMATYCZNY ZAPIS: Śledzi zmiany we wszystkich polach
    LaunchedEffect(name, description, userStatus, smoking, drinking, personalityType, isProfilePublic, interests) {
        if (!isLoading) triggerAutoSave()
    }

    // Wybór zdjęcia z galerii
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
             val oldImageUrl = profileImageUrl
            isLoading = true
            message = "Wgrywanie zdjęcia..."
            FirebaseService.uploadFileToStorage(it, "profile_pictures", userSelection!!.userId, onSuccess = { newUrl ->
                profileImageUrl = newUrl
                triggerAutoSave()
                if(oldImageUrl.isNotEmpty()) FirebaseService.deleteFileFromStorage(oldImageUrl, {}, {})
                isLoading = false
            }, onError = { err -> message = "Błąd: $err"; isLoading = false })
        }
    }

    // Główny kontener tła (Zapewnia jednolity kolor na całym ekranie)
    Box(modifier = Modifier.fillMaxSize().background(getCategoryGradient(userSelection?.category ?: ""))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding() // Obsługa paska powiadomień
                .padding(16.dp)
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Twój Profil", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center, modifier = Modifier.padding(bottom = 20.dp))

            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Zalogowano jako: $userEmail", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                
                ProfileAvatarSection(imageUrl = profileImageUrl, isLoading = isLoading, onImageClick = { imagePicker.launch("image/*") })
                
                Spacer(modifier = Modifier.height(20.dp))
                CustomProfileTextField(value = name, onValueChange = { name = it }, label = "Twój Nick", modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("Twój Status:", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                
                val statuses = listOf(
                    Triple("available", "🟢 Wolny (Czas)", "Mam czas na spotkania"),
                    Triple("relationship", "❤️ Wolny (Związek)", "Szukam relacji"),
                    Triple("limited", "🟡 Ograniczony (Czas)", "Rzadko odpisuję / mało czasu"),
                    Triple("chat_only", "🖤 Zajęty", "Nie szukam nikogo / tylko czat")
                )

                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    statuses.forEach { (key, label, descriptionText) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(if (userStatus == key) Color.White.copy(0.2f) else Color.Transparent, RoundedCornerShape(8.dp))
                                .border(1.dp, if (userStatus == key) Color.White else Color.Gray, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { userStatus = key }
                                .padding(8.dp)
                        ) {
                            RadioButton(selected = userStatus == key, onClick = { userStatus = key }, colors = RadioButtonDefaults.colors(selectedColor = Color.White, unselectedColor = Color.Gray))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column { 
                                Text(text = label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                if (descriptionText.isNotEmpty()) Text(text = descriptionText, color = Color.White.copy(0.7f), fontSize = 12.sp) 
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                CustomProfileTextField(value = description, onValueChange = { description = it }, label = "O mnie (Opis)", minLines = 3, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).background(Color.White.copy(0.1f), RoundedCornerShape(25.dp)).padding(4.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    TabButton(icon = Icons.Default.Person, text = "Info & Hobby", isSelected = selectedTab == 0, onClick = { selectedTab = 0 })
                    TabButton(icon = Icons.Default.Settings, text = "Social & Opcje", isSelected = selectedTab == 1, onClick = { selectedTab = 1 })
                }
                Spacer(modifier = Modifier.height(20.dp))

                 AnimatedVisibility(visible = selectedTab == 0) {
                    Column {
                        InterestTagEditor(currentInterests = interests.joinToString(", "), onInterestsChange = { interests = it.split(',').map { tag -> tag.trim() } })
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Styl Życia (Opcjonalne)", color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Palenie:", color = Color.White, fontSize = 14.sp)
                            Row {
                                val options = listOf("Nie", "Tak", "Okazyjnie")
                                options.forEach { option -> FilterChip(selected = smoking == option, onClick = { smoking = if (smoking == option) "" else option }, label = { Text(option) }, modifier = Modifier.padding(horizontal = 2.dp)) }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Alkohol:", color = Color.White, fontSize = 14.sp)
                            Row {
                                val options = listOf("Nie", "Tak", "Okazyjnie")
                                options.forEach { option -> FilterChip(selected = drinking == option, onClick = { drinking = if (drinking == option) "" else option }, label = { Text(option) }, modifier = Modifier.padding(horizontal = 2.dp)) }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Typ osobowości", color = Color.White, fontWeight = FontWeight.Bold)
                        val personalityTypes = listOf("Introwertyk", "Ekstrawertyk", "Ambiwertyk", "Nie chcę podawać")
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                             Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                                 personalityTypes.take(2).forEach { type -> Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) { RadioButton(selected = personalityType == type, onClick = { personalityType = type }); Text(type, color = Color.White, fontSize = 12.sp) } }
                             }
                             Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                                 personalityTypes.drop(2).forEach { type -> Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) { RadioButton(selected = personalityType == type, onClick = { personalityType = type }); Text(type, color = Color.White, fontSize = 12.sp) } }
                             }
                        }
                    }
                }

                AnimatedVisibility(visible = selectedTab == 1) {
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
                            Text("Profil publiczny", color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.weight(1f))
                            Switch(checked = isProfilePublic, onCheckedChange = { isProfilePublic = it })
                        }
                        Text("Gdy profil jest prywatny, tylko Twoi znajomi widzą Twoje zdjęcia i sociale.", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 16.dp))
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(0.2f))
                        
                        Button(onClick = onFriendsClick) { Text("Znajomi i sugestie") }
                        Button(onClick = onChatsClick) { Text("Czaty") }
                        Button(onClick = onGroupsClick) { Text("Grupy") }
                        Button(onClick = onEditSocialsClick) { Text("Edytuj Social Media") }
                        Button(onClick = onEditPreferencesClick) { Text("Edytuj preferencje matcha") }
                        Button(onClick = onBlockedUsersClick) { Text("Zablokowani użytkownicy") }
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(0.2f))
                        
                        Button(onClick = { showPasswordDialog = true }) { Text("Zmień hasło") }
                        OutlinedButton(onClick = onLogoutClick, colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) { Text("Wyloguj") }
                        TextButton(onClick = { showDeleteDialog = true }) { Text("Usuń konto", color = Color.Red.copy(0.8f)) }
                    }
                }
            }
        }
        
        // Pasek powiadomień o stanie zapisu
        if (message.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(bottom = 100.dp), contentAlignment = Alignment.BottomCenter) {
                Surface(color = Color.Black.copy(0.6f), shape = RoundedCornerShape(16.dp)) {
                    Text(message, color = Color.White, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), fontSize = 12.sp)
                }
            }
        }
        
        if (isLoading) Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color.White) }
    }

    // DIALOG ZMIANY HASŁA
    if (showPasswordDialog) {
        ChangePasswordDialog(onDismiss = { showPasswordDialog = false }) { old, new ->
            FirebaseService.updatePassword(new, old, 
                onSuccess = { Toast.makeText(context, "Hasło zmienione", Toast.LENGTH_SHORT).show(); showPasswordDialog = false },
                onError = { Toast.makeText(context, "Błąd: $it", Toast.LENGTH_SHORT).show() }
            )
        }
    }

    // DIALOG USUNIĘCIA KONTA
    if (showDeleteDialog) {
        DeleteAccountDialog(onDismiss = { showDeleteDialog = false }) { password ->
            FirebaseService.deleteAccount(password, 
                onSuccess = { Toast.makeText(context, "Konto usunięte", Toast.LENGTH_SHORT).show(); onLogoutClick() },
                onError = { Toast.makeText(context, "Błąd: $it", Toast.LENGTH_SHORT).show() }
            )
        }
    }
}

// Funkcja pomocnicza: Wyświetla dialog zmiany hasła
@Composable
fun ChangePasswordDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Zmień hasło") }, text = { Column { TextField(value = oldPassword, onValueChange = { oldPassword = it }, label = { Text("Obecne hasło") }, visualTransformation = PasswordVisualTransformation()); Spacer(modifier = Modifier.height(8.dp)); TextField(value = newPassword, onValueChange = { newPassword = it }, label = { Text("Nowe hasło") }, visualTransformation = PasswordVisualTransformation()) } }, confirmButton = { Button(onClick = { onConfirm(oldPassword, newPassword) }) { Text("Zmień") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } })
}

// Funkcja pomocnicza: Wyświetla dialog usunięcia konta
@Composable
fun DeleteAccountDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var password by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Usuń konto") }, text = { Column { Text("Aby usunąć konto, wpisz swoje hasło. Tej operacji nie można cofnąć."); Spacer(modifier = Modifier.height(8.dp)); TextField(value = password, onValueChange = { password = it }, label = { Text("Hasło") }, visualTransformation = PasswordVisualTransformation()) } }, confirmButton = { Button(onClick = { onConfirm(password) }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Usuń konto") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } })
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ConnectNearTheme {
        ProfileScreen(
            onLogoutClick = {}, onEditPreferencesClick = {}, onBlockedUsersClick = {},
            onEditSocialsClick = {}, onFriendsClick = {}, onChatsClick = {}, onGroupsClick = {}
        )
    }
}
