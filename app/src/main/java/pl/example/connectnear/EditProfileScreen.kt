package pl.example.connectnear

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
            delay(1500)
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
                        onSaveSuccess()
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edytuj Social Media") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().background(getCategoryGradient(user?.category ?: "")), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(getCategoryGradient(user?.category ?: ""))
                    .padding(it)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Połącz swoje konta", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Wpisz swoje nazwy użytkownika, aby inni mogli Cię znaleźć.", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = instagramLink,
                    onValueChange = { instagramLink = it },
                    label = { Text("Nazwa użytkownika Instagram") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = facebookLink,
                    onValueChange = { facebookLink = it },
                    label = { Text("Nazwa użytkownika Facebook") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = tiktokLink,
                    onValueChange = { tiktokLink = it },
                    label = { Text("Nazwa użytkownika TikTok") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = youtubeLink,
                    onValueChange = { youtubeLink = it },
                    label = { Text("Nazwa użytkownika YouTube") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = twitterLink,
                    onValueChange = { twitterLink = it },
                    label = { Text("Nazwa użytkownika Twitter") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = snapchatLink,
                    onValueChange = { snapchatLink = it },
                    label = { Text("Nazwa użytkownika Snapchat") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = saveState,
                        color = when (saveState) {
                            "Zapisano" -> Color.Green
                            "Zapisywanie..." -> Color.Yellow
                            else -> Color.Red
                        }
                    )
                }
            }
        }
    }
}
