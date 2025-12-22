package pl.example.connectnear

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import pl.example.connectnear.ui.theme.getCategoryGradient

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserProfileScreen(
    targetUserId: String,
    onBackClick: () -> Unit,
    onChatClick: (String, String, String) -> Unit // Dodano trzeci parametr na startową wiadomość
) {
    val context = LocalContext.current
    val myUserId = FirebaseService.auth.currentUser?.uid ?: ""

    var userData by remember { mutableStateOf<UserSelection?>(null) }
    var currentUserData by remember { mutableStateOf<UserSelection?>(null) }
    var isFriend by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var showBlockDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    
    // Lodołamacz (Icebreaker)
    var showIcebreakerDialog by remember { mutableStateOf(false) }
    var commonInterests by remember { mutableStateOf<List<String>>(emptyList()) }
    
    var isBlocked by remember { mutableStateOf(false) }

    LaunchedEffect(targetUserId) {
        FirebaseService.getUserDetails(targetUserId) { user ->
            userData = user
            
            if (myUserId.isNotEmpty()) {
                ProfileRepo.getCurrentUserProfile { myProfile ->
                    currentUserData = myProfile
                }

                FirebaseService.checkIfFriends(myUserId, targetUserId) { areFriends ->
                    isFriend = areFriends
                    isLoading = false
                }
                
                FirebaseService.getBlockedUsers(myUserId) { blocked ->
                    isBlocked = blocked.contains(targetUserId)
                }
            } else {
                isLoading = false
            }
        }
    }

    fun openSocialMedia(username: String, platform: String) {
        if (username.isBlank()) return
        val url = when (platform) {
            "instagram" -> "https://www.instagram.com/$username"
            "facebook" -> "https://www.facebook.com/$username"
            "tiktok" -> "https://www.tiktok.com/@${username.removePrefix("@")}"
            "messenger" -> "https://m.me/$username"
            "spotify" -> if (username.startsWith("http")) username else "https://open.spotify.com/user/$username"
            "steam" -> if (username.startsWith("http")) username else "https://steamcommunity.com/id/$username"
            else -> username
        }
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) { 
            Toast.makeText(context, "Nie udało się otworzyć linku", Toast.LENGTH_SHORT).show()
        }
    }

    val backgroundBrush = if (userData != null) {
        getCategoryGradient(userData!!.category)
    } else {
        Brush.verticalGradient(colors = listOf(Color(0xFF0AA4F4), Color(0xFF1CD9C3)))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onBackClick, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)) {
                Text("< Wróć")
            }
            Spacer(modifier = Modifier.weight(1f))
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Opcje", tint = Color.White)
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text(if (isBlocked) "Odblokuj użytkownika" else "Zablokuj użytkownika", color = if (isBlocked) Color.Black else Color.Red) }, 
                        onClick = {
                            showMenu = false
                            if (isBlocked) {
                                FirebaseService.unblockUser(myUserId, targetUserId, {
                                    isBlocked = false
                                    Toast.makeText(context, "Odblokowano", Toast.LENGTH_SHORT).show()
                                }, { Toast.makeText(context, "Błąd: $it", Toast.LENGTH_SHORT).show() })
                            } else {
                                showBlockDialog = true
                            }
                        }
                    )
                }
            }
        }

        if (isLoading) {
            CircularProgressIndicator(color = Color.White)
        } else if (userData != null) {
            val user = userData!!

            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(3.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (user.profileImageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = user.profileImageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("Brak\nZdjęcia", color = Color.Gray, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Zmiana: Status obok nazwy użytkownika
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Text(user.name, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
                
                if (user.userStatus.isNotEmpty()) {
                    val statusConfig = when (user.userStatus) {
                        "available" -> Triple("🟢 Wolny (Czas)", Color(0xFF4CAF50), Color.White)
                        "relationship" -> Triple("❤️ Wolny (Związek)", Color(0xFF4CAF50), Color.White)
                        "limited" -> Triple("🟡 Ograniczony (Czas)", Color(0xFFFFC107), Color.Black)
                        "chat_only" -> Triple("🖤 Zajęty", Color(0xFF212121), Color.White) 
                        else -> Triple("", Color.Transparent, Color.Transparent)
                    }
                    
                    if (statusConfig.first.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Surface(color = statusConfig.second, shape = RoundedCornerShape(16.dp)) {
                            Text(
                                text = statusConfig.first,
                                color = statusConfig.third,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
            
            if(user.personalityType.isNotEmpty() && user.personalityType != "Nie chcę podawać") {
                Text("(${user.personalityType})", fontSize = 16.sp, color = Color.White.copy(0.8f))
                Spacer(modifier = Modifier.height(8.dp))
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(user.category, fontSize = 18.sp, color = Color.White.copy(0.9f), fontWeight = FontWeight.SemiBold)
                    
                    val subCategoryText = when (user.category) {
                        "Sport" -> user.subCategory
                        "Impreza" -> user.partyType
                        else -> ""
                    }
                    if (subCategoryText.isNotEmpty()) {
                        Text(" • $subCategoryText", fontSize = 18.sp, color = Color.White.copy(0.8f))
                    }
                }
                
                if (user.isPersonalTrainer) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Trener Personalny", color = Color.Yellow, fontWeight = FontWeight.Bold)
                }
                if (user.isTutor) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Korepetytor", color = Color.Yellow, fontWeight = FontWeight.Bold)
                }
                
                if (user.isStudyBuddy) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Partner do Nauki (Study Buddy)", color = Color(0xFFADD8E6), fontWeight = FontWeight.Bold)
                }
            }

            if (currentUserData != null) {
                val me = currentUserData!!
                
                val myInterestsSet = me.interests.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }.toSet()
                val userInterestsSet = user.interests.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }.toSet()
                val commonInterestsList = myInterestsSet.intersect(userInterestsSet).toList()

                // Obliczanie zgodności...
                val samePersonality = user.personalityType.isNotBlank() && user.personalityType != "Nie chcę podawać" && user.personalityType == me.personalityType
                var compatibilityScore = (commonInterestsList.size * 10).coerceAtMost(50)
                if (samePersonality) compatibilityScore += 20
                if (me.category == user.category) compatibilityScore += 10
                if (me.isStudyBuddy && user.isStudyBuddy) compatibilityScore += 20
                
                if (compatibilityScore >= 40) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(color = if (compatibilityScore >= 70) Color(0xFFFF4081) else Color(0xFF9C27B0), shape = RoundedCornerShape(20.dp), shadowElevation = 6.dp) {
                        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Zgodność: $compatibilityScore%", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        }
                    }
                }
                
                 // Dialog Lodołamacza
                if (showIcebreakerDialog) {
                    IcebreakerDialog(
                        commonInterests = commonInterestsList,
                        onDismiss = { showIcebreakerDialog = false },
                        onSend = { message ->
                            showIcebreakerDialog = false
                            onChatClick(user.userId, user.name, message)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            if (isFriend && !isBlocked) { 
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("O mnie", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = if (user.description.isNotEmpty()) user.description else "Brak opisu.", color = Color.White.copy(0.9f), fontSize = 14.sp, lineHeight = 20.sp)

                    if (user.smoking.isNotEmpty() || user.drinking.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Styl Życia / Używki", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            if (user.smoking.isNotEmpty()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🚬 Palenie: ", color = Color.White.copy(0.7f), fontSize = 14.sp)
                                    Text(user.smoking, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                            if (user.drinking.isNotEmpty()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🍺 Alkohol: ", color = Color.White.copy(0.7f), fontSize = 14.sp)
                                    Text(user.drinking, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                    
                    if (user.isPersonalTrainer && user.trainerDescription.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Oferta trenerska:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Yellow)
                        Text(user.trainerDescription, color = Color.White.copy(0.9f), fontSize = 14.sp)
                    }
                    
                    if (user.isTutor && user.tutorDescription.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Oferta korepetycji:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Yellow)
                        Text(user.tutorDescription, color = Color.White.copy(0.9f), fontSize = 14.sp)
                    }
                    
                    if (user.interests.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Zainteresowania", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            user.interests.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { tag ->
                                Surface(color = Color.White.copy(0.2f), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Color.White.copy(0.5f))) {
                                    Text(text = tag, color = Color.White, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                var isSocialsExpanded by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(Color.White.copy(0.1f)).clickable { isSocialsExpanded = !isSocialsExpanded }.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Link, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isSocialsExpanded) "Ukryj linki" else "Linki (Social Media)", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(if (isSocialsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null, tint = Color.White.copy(0.7f), modifier = Modifier.size(18.dp))
                }

                if (isSocialsExpanded) {
                    Spacer(modifier = Modifier.height(16.dp))
                    FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (user.instagramLink.isNotEmpty()) {
                            SocialIcon(Icons.Default.PhotoCamera, Color(0xFFE1306C)) { openSocialMedia(user.instagramLink, "instagram") }
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                        if (user.facebookLink.isNotEmpty()) {
                            SocialIcon(Icons.Default.Public, Color(0xFF1877F2)) { openSocialMedia(user.facebookLink, "facebook") }
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                        if (user.tiktokLink.isNotEmpty()) {
                            SocialIcon(Icons.Default.MusicNote, Color.Black) { openSocialMedia(user.tiktokLink, "tiktok") }
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                        if (user.messengerLink.isNotEmpty()) {
                            SocialIcon(Icons.Default.ChatBubble, Color(0xFF0084FF)) { openSocialMedia(user.messengerLink, "messenger") }
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                        if (user.spotifyLink.isNotEmpty()) {
                            SocialIcon(Icons.Default.Audiotrack, Color(0xFF1DB954)) { openSocialMedia(user.spotifyLink, "spotify") }
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                        if (user.steamLink.isNotEmpty()) {
                            SocialIcon(Icons.Default.Gamepad, Color(0xFF171A21)) { openSocialMedia(user.steamLink, "steam") }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
                Button(
                    onClick = { 
                        if(currentUserData != null) {
                            val myInterests = currentUserData!!.interests.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }.toSet()
                            val userInterests = user.interests.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }.toSet()
                            val common = myInterests.intersect(userInterests).toList()

                            if (common.isNotEmpty()) {
                                commonInterests = common
                                showIcebreakerDialog = true
                            } else {
                                onChatClick(user.userId, user.name, "")
                            }
                        } else {
                            onChatClick(user.userId, user.name, "")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006400))
                ) {
                    Text("Napisz Wiadomość", fontSize = 18.sp)
                }

            } else {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) {
                    Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Prywatny", tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (isBlocked) {
                            Text("Użytkownik zablokowany", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Odblokuj użytkownika, aby zobaczyć jego profil.", color = Color.Gray, fontSize = 14.sp, textAlign = TextAlign.Center)
                        } else {
                            Text("To konto jest prywatne", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Dodaj tego użytkownika do znajomych, aby zobaczyć jego opis i zainteresowania.", color = Color.Gray, fontSize = 14.sp, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = {
                                    FirebaseService.sendFriendRequest(myUserId = myUserId, myName = "Ja", targetUserId = user.userId,
                                        onSuccess = { Toast.makeText(context, "Wysłano zaproszenie!", Toast.LENGTH_SHORT).show() },
                                        onError = { Toast.makeText(context, "Błąd: $it", Toast.LENGTH_SHORT).show() }
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Wyślij zaproszenie") }
                        }
                    }
                }
            }
        }

        if (showBlockDialog) {
            AlertDialog(
                onDismissRequest = { showBlockDialog = false },
                title = { Text("Zablokuj użytkownika") },
                text = { Text("Czy na pewno chcesz zablokować ${userData?.name}? Nie będziesz mógł/mogła wysyłać wiadomości ani zaproszeń do tej osoby, a także zostanie ona usunięta z Twoich znajomych.") },
                confirmButton = {
                    Button(
                        onClick = {
                            FirebaseService.blockUser(myUserId, targetUserId, {
                                Toast.makeText(context, "Zablokowano ${userData?.name}", Toast.LENGTH_SHORT).show()
                                isBlocked = true
                                isFriend = false
                                showBlockDialog = false
                            }, { error ->
                                Toast.makeText(context, "Błąd: $error", Toast.LENGTH_SHORT).show()
                                showBlockDialog = false
                            })
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) { Text("Zablokuj") }
                },
                dismissButton = { Button(onClick = { showBlockDialog = false }) { Text("Anuluj") } }
            )
        }
    }
}

@Composable
fun IcebreakerDialog(
    commonInterests: List<String>,
    onDismiss: () -> Unit,
    onSend: (String) -> Unit
) {
    val suggestions = remember(commonInterests) {
        listOfNotNull(
            commonInterests.firstOrNull()?.let { "Cześć! Widzę, że też lubisz '$it'. Co o tym sądzisz?" },
            commonInterests.getOrNull(1)?.let { "Hej, wspólne zainteresowanie: '$it'! Może o tym pogadamy?" },
            if (commonInterests.size > 1) "Wow, mamy kilka wspólnych pasji! Od czego zaczynamy rozmowę?" else null
        ).shuffled()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Zacznij rozmowę! (Lodołamacz)") },
        text = {
            Column {
                Text("Macie wspólne zainteresowania! Wybierz gotową wiadomość lub napisz własną.", fontSize = 14.sp, lineHeight = 20.sp)
                Spacer(modifier = Modifier.height(16.dp))
                suggestions.forEach { suggestion ->
                    TextButton(onClick = { onSend(suggestion) }) {
                        Text(suggestion, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSend("") }) { Text("Napiszę sam/a") }
        },
        dismissButton = { 
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) { Text("Anuluj") }
        }
    )
}

@Composable
fun SocialIcon(icon: ImageVector, color: Color, onClick: () -> Unit) {
    Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(color).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
    }
}
