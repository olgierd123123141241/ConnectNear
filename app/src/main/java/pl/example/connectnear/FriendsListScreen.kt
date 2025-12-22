package pl.example.connectnear

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import pl.example.connectnear.ui.theme.getCategoryGradient

@Composable
fun FriendsListScreen(
    userSelection: UserSelection, // ZMIANA: Przekazujemy cały obiekt UserSelection, aby znać kategorię
    onBackClick: () -> Unit,
    onChatClick: (String, String, String) -> Unit // Zaktualizowana sygnatura
) {
    var friends by remember { mutableStateOf<List<FoundUser>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Pobieramy znajomych przy starcie
    LaunchedEffect(Unit) {
        FirebaseService.getFriends(userSelection.userId) { list ->
            friends = list
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) { // Zmieniono na Box, aby pozycjonować przycisk
        Column(
            modifier = Modifier
                .fillMaxSize()
                // ZMIANA: Użycie gradientu zależnego od kategorii
                .background(getCategoryGradient(userSelection.category))
                .padding(16.dp)
        ) {
            // Nagłówek
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
                // Usunięto przycisk "Wróć" stąd
                Text("Moi Znajomi", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else if (friends.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nie masz jeszcze znajomych. Dodaj kogoś z mapy!", color = Color.White)
                }
            } else {
                LazyColumn(modifier = Modifier.padding(bottom = 60.dp)) { // Padding na dole dla przycisku
                    items(friends) { friend ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                // Kliknięcie w całą kartę też otworzy czat (dla wygody)
                                .clickable { onChatClick(friend.userId, friend.name, "") },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Zdjęcie
                                if (friend.profileImageUrl.isNotEmpty()) {
                                    AsyncImage(
                                        model = friend.profileImageUrl,
                                        contentDescription = null,
                                        modifier = Modifier.size(50.dp).clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.Gray), contentAlignment = Alignment.Center) {
                                        Text(friend.name.take(1), color = Color.White)
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Nazwa i kategoria
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(friend.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                                    if (friend.category.isNotEmpty()) {
                                        Text(friend.category, color = Color.Gray, fontSize = 14.sp)
                                    }
                                }

                                // --- PRZYCISK: NAPISZ (NOWOŚĆ) ---
                                IconButton(onClick = {
                                    onChatClick(friend.userId, friend.name, "")
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Email, // Ikonka koperty
                                        contentDescription = "Napisz",
                                        tint = Color(0xFF00C853) // Zielony kolor
                                    )
                                }

                                // --- PRZYCISK: USUŃ ---
                                IconButton(onClick = {
                                    FirebaseService.removeFriend(userSelection.userId, friend.userId) {
                                        // Odśwież listę lokalnie po usunięciu
                                        friends = friends.filter { it.userId != friend.userId }
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Usuń",
                                        tint = Color.Red
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Przycisk "Wróć" na dole
        Button(
            onClick = onBackClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text("< Wróć")
        }
    }
}
