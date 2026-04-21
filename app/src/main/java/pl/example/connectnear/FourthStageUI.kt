package pl.example.connectnear

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Scale
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.compose.*
import java.util.Locale
import pl.example.connectnear.ui.theme.getCategoryPrimaryColor

@Composable
fun FourthStageUI(
    uiState: MapUiState,
    userSelection: UserSelection,
    onBackClick: () -> Unit,
    onChatClick: (String, String, String) -> Unit,
    onProfileClick: () -> Unit,
    onOtherUserProfileClick: (String) -> Unit,
    onUserSelected: (FoundUser) -> Unit,
    onUserDeselected: () -> Unit
) {
    val context = LocalContext.current
    var showRadarPanel by remember { mutableStateOf(false) }
    var selectedFlashEvent by remember { mutableStateOf<FlashEvent?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        FourthStageMap(
            myLocation = uiState.myLocation,
            myProfileImageUrl = userSelection.profileImageUrl,
            userCategory = userSelection.category, // Można by też trzymać w uiState
            otherUsers = uiState.otherUsers,
            flashEvents = uiState.flashEvents,
            onMapClick = { onUserDeselected() },
            onUserMarkerClick = { user: FoundUser -> onUserSelected(user) },
            onFlashEventClick = { event: FlashEvent ->
                selectedFlashEvent = event
                if (event.isOneTime && !event.viewedBy.contains(userSelection.userId)) {
                    FirebaseService.markFlashEventAsViewed(event.id, userSelection.userId)
                }
            },
            onMapLongClick = {},
            hasPermission = uiState.hasPermission,
            onPermissionRequest = { /* Logika uprawnień w ViewModel */ },
            selectedUser = uiState.selectedUser
        )

        FourthStageSettingsFab(onClick = { /* Logika w ViewModel */ })

        if (uiState.myLocation != null) {
            RadarPanel(
                usersNearby = uiState.otherUsers,
                myLocation = uiState.myLocation,
                isVisible = showRadarPanel,
                onUserClick = { user: FoundUser -> onUserSelected(user) },
                onAddFlashEvent = { event: FlashEvent -> 
                    FirebaseService.addFlashEvent(event, 
                        onSuccess = { Toast.makeText(context, "Wydarzenie dodane!", Toast.LENGTH_SHORT).show() }, 
                        onError = { error: String -> Toast.makeText(context, "Błąd: $error", Toast.LENGTH_SHORT).show() })
                },
                onClose = { showRadarPanel = false }
            )
        }

        FourthStageControls(
            selectedUser = uiState.selectedUser,
            isFriend = uiState.isFriend,
            onChatClick = { uiState.selectedUser?.let { onChatClick(it.userId, it.name, "") } },
            onSendRequestClick = {
                uiState.selectedUser?.let { user ->
                    FirebaseService.sendFriendRequest(
                        userSelection.userId, userSelection.name, user.userId,
                        onSuccess = { Toast.makeText(context, "Wysłano zaproszenie!", Toast.LENGTH_SHORT).show() },
                        onError = { err: String -> Toast.makeText(context, "Błąd: $err", Toast.LENGTH_SHORT).show() }
                    )
                }
            },
            onOtherProfileClick = { uiState.selectedUser?.let { onOtherUserProfileClick(it.userId) } },
            onCloseUserClick = { onUserDeselected() },
            onExitClick = onBackClick,
            onMyProfileClick = onProfileClick,
            onRadarClick = { showRadarPanel = !showRadarPanel }
        )
    }
}

@Composable
fun FourthStageMap(
    myLocation: LatLng?,
    myProfileImageUrl: String?,
    userCategory: String,
    otherUsers: List<FoundUser>,
    flashEvents: List<FlashEvent>,
    onMapClick: () -> Unit,
    onUserMarkerClick: (FoundUser) -> Unit,
    onFlashEventClick: (FlashEvent) -> Unit,
    onMapLongClick: (LatLng) -> Unit,
    hasPermission: Boolean,
    onPermissionRequest: () -> Unit,
    selectedUser: FoundUser? = null
) {
    if (myLocation != null) {
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(myLocation, 14f)
        }

        val myMarkerColor = getCategoryPrimaryColor(userCategory)

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = true),
            onMapClick = { onMapClick() },
            onMapLongClick = { onMapLongClick(it) }
        ) {
            // --- ZNACZNIK UŻYTKOWNIKA (JA) ---
            MyMarker(
                location = myLocation,
                profileImageUrl = myProfileImageUrl,
                borderColor = myMarkerColor
            )

            // --- ZNACZNIKI INNYCH UŻYTKOWNIKÓW ---
            otherUsers.forEach { user ->
                MapUserMarker(
                    user = user,
                    isSelected = selectedUser?.userId == user.userId,
                    onClick = onUserMarkerClick
                )
            }

            // --- ZNACZNIKI FLASH EVENTS (ZDJĘCIA) ---
            flashEvents.forEach { event ->
                FlashEventMarker(
                    event = event,
                    onClick = onFlashEventClick
                )
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (!hasPermission) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Brak uprawnień GPS", color = Color.Red)
                    Button(onClick = onPermissionRequest) { Text("Nadaj uprawnienia") }
                }
            } else {
                CircularProgressIndicator()
                Text("Szukam GPS...", modifier = Modifier.padding(top = 40.dp))
            }
        }
    }
}

@Composable
fun MyMarker(location: LatLng, profileImageUrl: String?, borderColor: Color) {
    val context = LocalContext.current
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(profileImageUrl) {
        if (!profileImageUrl.isNullOrEmpty()) {
            val request = ImageRequest.Builder(context)
                .data(profileImageUrl)
                .size(200, 200)
                .scale(Scale.FILL)
                .allowHardware(false)
                .target(
                    onSuccess = { result -> imageBitmap = (result as? BitmapDrawable)?.bitmap },
                    onError = { imageBitmap = null }
                )
                .build()
            context.imageLoader.enqueue(request)
        } else {
            imageBitmap = null
        }
    }

    val markerState = rememberMarkerState(position = location)
    // Aktualizacja pozycji znacznika, gdy lokalizacja się zmienia
    LaunchedEffect(location) {
        markerState.position = location
    }

    MarkerComposable(
        state = markerState,
        zIndex = 2f
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(Color.White, shape = CircleShape)
                .border(3.dp, borderColor, CircleShape)
                .padding(2.dp)
        ) {
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap!!.asImageBitmap(),
                    contentDescription = "JA",
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    Modifier.fillMaxSize().clip(CircleShape).background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("JA", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun MapUserMarker(
    user: FoundUser,
    isSelected: Boolean,
    onClick: (FoundUser) -> Unit
) {
    user.location?.let { userLocation ->
        val context = LocalContext.current
        var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }

        LaunchedEffect(user.profileImageUrl) {
            if (user.profileImageUrl.isNotEmpty()) {
                val request = ImageRequest.Builder(context)
                    .data(user.profileImageUrl)
                    .size(200, 200)
                    .scale(Scale.FILL)
                    .allowHardware(false)
                    .target(
                        onSuccess = { result -> imageBitmap = (result as? BitmapDrawable)?.bitmap },
                        onError = { imageBitmap = null }
                    )
                    .build()
                context.imageLoader.enqueue(request)
            } else {
                imageBitmap = null
            }
        }

        val targetPosition = LatLng(userLocation.latitude, userLocation.longitude)
        val markerState = rememberMarkerState(
            key = user.userId,
            position = targetPosition
        )
        LaunchedEffect(targetPosition) {
            markerState.position = targetPosition
        }

        val snippetText = if (isSelected && !user.aiSuggestion.isNullOrBlank()) {
            user.aiSuggestion
        } else {
            user.category
        }

        MarkerComposable(
            state = markerState,
            title = user.name,
            snippet = snippetText,
            onClick = { onClick(user); false },
            zIndex = if (isSelected) 1f else 0f
        ) {
            val borderColor = if (isSelected) Color.Red else getCategoryPrimaryColor(user.category)
            val size = if (isSelected) 60.dp else 48.dp

            Box(
                modifier = Modifier
                    .size(size)
                    .background(Color.White, shape = CircleShape)
                    .border(3.dp, borderColor, CircleShape)
                    .padding(2.dp)
            ) {
                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap!!.asImageBitmap(),
                        contentDescription = user.name,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        Modifier.fillMaxSize().clip(CircleShape).background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(user.name.take(1).uppercase(), fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun FlashEventMarker(
    event: FlashEvent,
    onClick: (FlashEvent) -> Unit
) {
    val context = LocalContext.current
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(event.imageUrl) {
        if (event.imageUrl.isNotEmpty()) {
            val request = ImageRequest.Builder(context)
                .data(event.imageUrl)
                .size(200, 200)
                .scale(Scale.FILL)
                .allowHardware(false)
                .target(
                    onSuccess = { result -> imageBitmap = (result as? BitmapDrawable)?.bitmap },
                    onError = { imageBitmap = null }
                )
                .build()
            context.imageLoader.enqueue(request)
        } else {
            imageBitmap = null
        }
    }

    val markerState = rememberMarkerState(
        key = event.id,
        position = LatLng(event.location.latitude, event.location.longitude)
    )

    MarkerComposable(
        state = markerState,
        title = event.description,
        onClick = { onClick(event); false }
    ) {
        val borderColor = if (event.isOneTime) Color(0xFFE91E63) else getCategoryPrimaryColor(event.category)
        
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(Color.White, CircleShape)
                .border(3.dp, borderColor, CircleShape)
                .padding(4.dp)
        ) {
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap!!.asImageBitmap(),
                    contentDescription = event.description,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = event.category.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

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
    var isCategoryMenuExpanded by remember { mutableStateOf(false) }

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
                    .fillMaxHeight(0.4f)
                    .padding(bottom = 80.dp)
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
                            if (user.location != null) {
                                val distance = calculateDistance(myLocation, LatLng(user.location.latitude, user.location.longitude))
                                RadarUserItem(user, distance, onUserClick)
                                HorizontalDivider()
                            }
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
                    ExposedDropdownMenuBox(
                        expanded = isCategoryMenuExpanded, 
                        onExpandedChange = { isCategoryMenuExpanded = !isCategoryMenuExpanded }
                    ) {
                        TextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Kategoria") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryMenuExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = isCategoryMenuExpanded, 
                            onDismissRequest = { isCategoryMenuExpanded = false }
                        ) {
                            eventCategories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) }, 
                                    onClick = { 
                                        selectedCategory = category 
                                        isCategoryMenuExpanded = false
                                    }
                                )
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
