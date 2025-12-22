package pl.example.connectnear

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import java.util.Locale

// --- 1. PRZYCISK USTAWIEŃ (FAB) ---
@Composable
fun FourthStageSettingsFab(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 80.dp, end = 16.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        FloatingActionButton(
            onClick = onClick,
            containerColor = Color.White,
            contentColor = Color.Black
        ) {
            Icon(Icons.Default.Settings, contentDescription = "Ustawienia mapy")
        }
    }
}

// --- 2. PANEL DOLNY (STEROWANIE) ---
@Composable
fun FourthStageControls(
    selectedUser: FoundUser?,
    isFriend: Boolean,
    onChatClick: () -> Unit,
    onSendRequestClick: () -> Unit,
    onOtherProfileClick: () -> Unit,
    onCloseUserClick: () -> Unit,
    onExitClick: () -> Unit,
    onRadarClick: () -> Unit,
    onMyProfileClick: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize().padding(bottom = 20.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        if (selectedUser != null) {
            // Widok, gdy użytkownik jest wybrany
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = selectedUser.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = onCloseUserClick) {
                            Icon(Icons.Default.Close, contentDescription = "Zamknij")
                        }
                    }
                    Text(
                        text = "Kategoria: ${selectedUser.category}",
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(onClick = onChatClick) {
                            Icon(Icons.AutoMirrored.Filled.Message, null)
                            Spacer(Modifier.width(4.dp))
                            Text("Czat")
                        }
                        if (!isFriend) {
                            Button(onClick = onSendRequestClick) {
                                Icon(Icons.Default.PersonAdd, null)
                                Spacer(Modifier.width(4.dp))
                                Text("Dodaj")
                            }
                        }
                        Button(onClick = onOtherProfileClick) {
                            Icon(Icons.Default.Person, null)
                            Spacer(Modifier.width(4.dp))
                            Text("Profil")
                        }
                    }
                }
            }
        } else {
            // Domyślny dolny pasek nawigacyjny
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable(onClick = onExitClick)
                            .padding(8.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Wyjdź", tint = Color.Red)
                        Text("Wyjdź", fontSize = 10.sp)
                    }

                    // --- IKONA BLISKO ---
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable(onClick = onRadarClick)
                            .padding(8.dp)
                    ) {
                        Icon(Icons.Default.Radar, contentDescription = "Blisko", tint = Color.Blue)
                        Text("Blisko", fontSize = 10.sp)
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable(onClick = onMyProfileClick)
                            .padding(8.dp)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Profil", tint = Color(0xFF0AA4F4))
                        Text("Profil", fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

// --- 3. PANEL RADAR / BLISKO CIEBIE ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadarPanel(
    usersNearby: List<FoundUser>,
    myLocation: LatLng,
    isVisible: Boolean,
    onUserClick: (FoundUser) -> Unit,
    onAddFlashEvent: (FlashEvent) -> Unit,
    onClose: () -> Unit
) {
    var showAddFlashDialog by remember { mutableStateOf(false) }
    var flashTitle by remember { mutableStateOf("") }
    var flashDescription by remember { mutableStateOf("") }
    var flashDurationMinutes by remember { mutableStateOf(60f) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isOneTime by remember { mutableStateOf(false) }
    var isRecurring by remember { mutableStateOf(false) }
    var recurringDetails by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }

    val eventCategories = listOf("Sport", "Planszówki", "Kino/Teatr", "Spacer z psem", "Nauka/Warsztaty")

    val context = LocalContext.current
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> selectedImageUri = uri }
    )

    if (isVisible) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.3f)
                    .padding(bottom = 120.dp)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Blisko Ciebie", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 8.dp))
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { showAddFlashDialog = true }) {
                            Icon(Icons.Default.AddLocation, contentDescription = "Dodaj", tint = Color.Blue)
                        }
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Zwiń")
                        }
                    }
                    HorizontalDivider()
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(usersNearby) { user ->
                            val distance = calculateDistance(myLocation, LatLng(user.location.latitude, user.location.longitude))
                            RadarUserItem(user, distance, onUserClick)
                            HorizontalDivider()
                        }
                        if (usersNearby.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                                    Text("Nikogo w pobliżu.", color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddFlashDialog) {
        AlertDialog(
            onDismissRequest = { showAddFlashDialog = false },
            title = { Text("Dodaj nowe wydarzenie") },
            text = {
                Column {
                    TextField(
                        value = flashTitle,
                        onValueChange = { flashTitle = it },
                        placeholder = { Text("Tytuł wydarzenia") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    ExposedDropdownMenuBox(expanded = false, onExpandedChange = {}) {
                        TextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Kategoria") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = false, onDismissRequest = {}) {
                            eventCategories.forEach { category ->
                                DropdownMenuItem(text = { Text(category) }, onClick = { selectedCategory = category })
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    TextField(
                        value = flashDescription,
                        onValueChange = { flashDescription = it },
                        placeholder = { Text("Opis wydarzenia") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { galleryLauncher.launch("image/*") }) {
                        Text(if (selectedImageUri != null) "Zdjęcie wybrane" else "Dodaj zdjęcie")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isOneTime, onCheckedChange = { isOneTime = it })
                        Text("Zdjęcie jednorazowe")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isRecurring, onCheckedChange = { isRecurring = it })
                        Text("Wydarzenie cykliczne")
                    }
                    if (isRecurring) {
                        TextField(
                            value = recurringDetails,
                            onValueChange = { recurringDetails = it },
                            placeholder = { Text("np. Co piątek o 20:00") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Czas trwania: ${flashDurationMinutes.toInt()} min")
                    Slider(
                        value = flashDurationMinutes,
                        onValueChange = { flashDurationMinutes = it },
                        valueRange = 15f..120f,
                        steps = 6
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val creatorId = AuthRepo.getCurrentUserId() ?: ""
                    val newEvent = FlashEvent(
                        title = flashTitle,
                        creatorId = creatorId,
                        category = selectedCategory,
                        location = GeoPoint(myLocation.latitude, myLocation.longitude),
                        description = flashDescription,
                        timestamp = System.currentTimeMillis(),
                        expiresAt = System.currentTimeMillis() + (flashDurationMinutes.toLong() * 60 * 1000),
                        isOneTime = isOneTime,
                        isRecurring = isRecurring,
                        recurringDetails = recurringDetails
                    )
                    
                    if (selectedImageUri != null) {
                        FirebaseService.uploadFileToStorage(
                            selectedImageUri!!, 
                            "flash_events", 
                            creatorId,
                            onSuccess = {
                                onAddFlashEvent(newEvent.copy(imageUrl = it))
                            },
                            onError = { /* Handle error */ }
                        )
                    } else {
                        onAddFlashEvent(newEvent)
                    }
                    showAddFlashDialog = false
                }) { Text("Dodaj") }
            },
            dismissButton = {
                Button(onClick = { showAddFlashDialog = false }) { Text("Anuluj") }
            }
        )
    }
}

@Composable
fun RadarUserItem(user: FoundUser, distanceKm: Double, onClick: (FoundUser) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(user) }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.LightGray, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(user.name.take(1), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Spacer(Modifier.width(12.dp))
        Text(user.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(Modifier.weight(1f))
        Text(
            "${String.format(Locale.US, "%.1f", distanceKm)} km",
            style = MaterialTheme.typography.bodySmall, 
            color = Color.Gray,
            fontWeight = FontWeight.Bold
        )
    }
}

fun calculateDistance(loc1: LatLng, loc2: LatLng): Double {
    val r = 6371.0
    val dLat = Math.toRadians(loc2.latitude - loc1.latitude)
    val dLon = Math.toRadians(loc2.longitude - loc1.longitude)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(loc1.latitude)) * Math.cos(Math.toRadians(loc2.latitude)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return r * c
}
