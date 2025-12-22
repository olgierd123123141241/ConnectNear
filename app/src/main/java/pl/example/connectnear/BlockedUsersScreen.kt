package pl.example.connectnear

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockedUsersScreen(
    myUserId: String,
    onBackClick: () -> Unit
) {
    var blockedUsers by remember { mutableStateOf<List<String>>(emptyList()) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        FirebaseService.getBlockedUsers(myUserId) { blockedUsers = it }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Zablokowani użytkownicy") }, navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                }
            })
        }
    ) {
        LazyColumn(modifier = Modifier.padding(it).padding(16.dp)) {
            items(blockedUsers) { userId ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Text(userId, modifier = Modifier.weight(1f))
                    Button(onClick = {
                        FirebaseService.unblockUser(myUserId, userId, {
                            Toast.makeText(context, "Odblokowano", Toast.LENGTH_SHORT).show()
                            FirebaseService.getBlockedUsers(myUserId) { blockedUsers = it }
                        }, { error ->
                            Toast.makeText(context, "Błąd: $error", Toast.LENGTH_SHORT).show()
                        })
                    }) { Text("Odblokuj") }
                }
            }
        }
    }
}
