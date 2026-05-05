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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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
import pl.example.connectnear.ui.theme.ConnectNearTheme
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
    val isPreview = LocalInspectionMode.current
    var showRadarPanel by remember { mutableStateOf(uiState.isRadarVisible) }
    var selectedFlashEvent by remember { mutableStateOf<FlashEvent?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        FourthStageMap(
            myLocation = uiState.myLocation,
            myProfileImageUrl = userSelection.profileImageUrl,
            userCategory = userSelection.category,
            otherUsers = uiState.otherUsers,
            flashEvents = uiState.flashEvents,
            onMapClick = { onUserDeselected() },
            onUserMarkerClick = { user: FoundUser -> onUserSelected(user) },
            onFlashEventClick = { event: FlashEvent ->
                selectedFlashEvent = event
            },
            onMapLongClick = {},
            hasPermission = uiState.hasPermission,
            onPermissionRequest = { },
            selectedUser = uiState.selectedUser
        )

        FourthStageSettingsFab(onClick = { })

        if (uiState.myLocation != null || isPreview) {
            RadarPanel(
                usersNearby = uiState.otherUsers,
                myLocation = uiState.myLocation ?: LatLng(0.0, 0.0),
                isVisible = uiState.isRadarVisible,
                onUserClick = { user: FoundUser -> onUserSelected(user) },
                onAddFlashEvent = { },
                onClose = { }
            )
        }

        FourthStageControls(
            selectedUser = uiState.selectedUser,
            isFriend = uiState.isFriend,
            onChatClick = { uiState.selectedUser?.let { onChatClick(it.userId, it.name, "") } },
            onSendRequestClick = { },
            onOtherProfileClick = { uiState.selectedUser?.let { onOtherUserProfileClick(it.userId) } },
            onCloseUserClick = { onUserDeselected() },
            onExitClick = onBackClick,
            onMyProfileClick = onProfileClick,
            onRadarClick = { }
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
    val isPreview = LocalInspectionMode.current

    if (isPreview) {
        // Podgląd zastępczy zamiast prawdziwej mapy w edytorze
        Box(modifier = Modifier.fillMaxSize().background(Color.LightGray), contentAlignment = Alignment.Center) {
            Text("MAPA (Podgląd)", color = Color.DarkGray, fontWeight = FontWeight.Bold)
        }
        return
    }

    if (myLocation != null) {
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(myLocation, 14f)
        }

        val myMarkerColor = getCategoryPrimaryColor(userCategory)

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = true),
            uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false),
            onMapClick = { onMapClick() },
            onMapLongClick = { onMapLongClick(it) }
        ) {
            MyMarker(location = myLocation, profileImageUrl = myProfileImageUrl, borderColor = myMarkerColor)
            otherUsers.forEach { user ->
                MapUserMarker(user = user, isSelected = selectedUser?.userId == user.userId, onClick = onUserMarkerClick)
            }
            flashEvents.forEach { event ->
                FlashEventMarker(event = event, onClick = onFlashEventClick)
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
        }
    }

    val markerState = rememberMarkerState(position = location)
    LaunchedEffect(location) { markerState.position = location }

    MarkerComposable(state = markerState, zIndex = 2f) {
        Box(modifier = Modifier.size(56.dp).background(Color.White, CircleShape).border(3.dp, borderColor, CircleShape).padding(2.dp)) {
            if (imageBitmap != null) {
                Image(bitmap = imageBitmap!!.asImageBitmap(), contentDescription = "JA", modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
            } else {
                Box(Modifier.fillMaxSize().clip(CircleShape).background(Color.Gray), contentAlignment = Alignment.Center) { Text("JA", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp) }
            }
        }
    }
}

@Composable
fun MapUserMarker(user: FoundUser, isSelected: Boolean, onClick: (FoundUser) -> Unit) {
    user.location?.let { userLocation ->
        val context = LocalContext.current
        var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }

        LaunchedEffect(user.profileImageUrl) {
            if (user.profileImageUrl.isNotEmpty()) {
                val request = ImageRequest.Builder(context).data(user.profileImageUrl).size(200, 200).scale(Scale.FILL).allowHardware(false).target(
                        onSuccess = { result -> imageBitmap = (result as? BitmapDrawable)?.bitmap },
                        onError = { imageBitmap = null }
                    ).build()
                context.imageLoader.enqueue(request)
            }
        }

        val targetPosition = LatLng(userLocation.latitude, userLocation.longitude)
        val markerState = rememberMarkerState(key = user.userId, position = targetPosition)
        LaunchedEffect(targetPosition) { markerState.position = targetPosition }

        MarkerComposable(state = markerState, title = user.name, onClick = { onClick(user); false }, zIndex = if (isSelected) 1f else 0f) {
            val borderColor = if (isSelected) Color.Red else getCategoryPrimaryColor(user.category)
            val size = if (isSelected) 60.dp else 48.dp
            Box(modifier = Modifier.size(size).background(Color.White, CircleShape).border(3.dp, borderColor, CircleShape).padding(2.dp)) {
                if (imageBitmap != null) {
                    Image(bitmap = imageBitmap!!.asImageBitmap(), contentDescription = user.name, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                } else {
                    Box(Modifier.fillMaxSize().clip(CircleShape).background(Color.LightGray), contentAlignment = Alignment.Center) { Text(user.name.take(1).uppercase(), fontWeight = FontWeight.Bold, color = Color.White) }
                }
            }
        }
    }
}

@Composable
fun FlashEventMarker(event: FlashEvent, onClick: (FlashEvent) -> Unit) {
    val context = LocalContext.current
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(event.imageUrl) {
        if (event.imageUrl.isNotEmpty()) {
            val request = ImageRequest.Builder(context).data(event.imageUrl).size(200, 200).scale(Scale.FILL).allowHardware(false).target(
                    onSuccess = { result -> imageBitmap = (result as? BitmapDrawable)?.bitmap },
                    onError = { imageBitmap = null }
                ).build()
            context.imageLoader.enqueue(request)
        }
    }

    val markerState = rememberMarkerState(key = event.id, position = LatLng(event.location.latitude, event.location.longitude))
    MarkerComposable(state = markerState, title = event.description, onClick = { onClick(event); false }) {
        val borderColor = if (event.isOneTime) Color(0xFFE91E63) else getCategoryPrimaryColor(event.category)
        Box(modifier = Modifier.size(64.dp).background(Color.White, CircleShape).border(3.dp, borderColor, CircleShape).padding(4.dp)) {
            if (imageBitmap != null) {
                Image(bitmap = imageBitmap!!.asImageBitmap(), contentDescription = event.description, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
            } else {
                Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color.Gray), contentAlignment = Alignment.Center) { Text(text = event.category.take(1).uppercase(), fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp) }
            }
        }
    }
}

@Composable
fun FourthStageSettingsFab(onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(top = 16.dp, end = 16.dp), contentAlignment = Alignment.TopEnd) {
        FloatingActionButton(onClick = onClick, containerColor = Color.White.copy(alpha = 0.8f), contentColor = Color.Black, shape = RoundedCornerShape(12.dp)) {
            Icon(Icons.Default.Settings, contentDescription = "Ustawienia")
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
    Box(modifier = Modifier.fillMaxSize().padding(bottom = 20.dp), contentAlignment = Alignment.BottomCenter) {
        if (selectedUser != null) {
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 100.dp).padding(horizontal = 16.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)), elevation = CardDefaults.cardElevation(8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = selectedUser.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = onCloseUserClick) { Icon(Icons.Default.Close, contentDescription = "Zamknij") }
                    }
                    Text(text = "Kategoria: ${selectedUser.category}", color = Color.Gray)
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                        Button(onClick = onChatClick, shape = RoundedCornerShape(12.dp)) { Icon(Icons.AutoMirrored.Filled.Message, null); Spacer(Modifier.width(4.dp)); Text("Czat") }
                        if (!isFriend) { Button(onClick = onSendRequestClick, shape = RoundedCornerShape(12.dp)) { Icon(Icons.Default.PersonAdd, null); Spacer(Modifier.width(4.dp)); Text("Dodaj") } }
                        Button(onClick = onOtherProfileClick, shape = RoundedCornerShape(12.dp)) { Icon(Icons.Default.Person, null); Spacer(Modifier.width(4.dp)); Text("Profil") }
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
    if (isVisible) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Card(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.45f).padding(bottom = 100.dp).padding(horizontal = 16.dp), shape = RoundedCornerShape(28.dp), elevation = CardDefaults.cardElevation(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Blisko Ciebie", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 8.dp))
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = onClose) { Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Zwiń") }
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
                    }
                }
            }
        }
    }
}

@Composable
fun RadarUserItem(user: FoundUser, distanceKm: Double, onClick: (FoundUser) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick(user) }.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(44.dp).background(Color.LightGray, CircleShape), contentAlignment = Alignment.Center) { Text(user.name.take(1), fontWeight = FontWeight.Bold, fontSize = 16.sp) }
        Spacer(Modifier.width(12.dp))
        Text(user.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Spacer(Modifier.weight(1f))
        Text("${String.format(Locale.US, "%.1f", distanceKm)} km", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

fun calculateDistance(loc1: LatLng, loc2: LatLng): Double {
    val r = 6371.0
    val dLat = Math.toRadians(loc2.latitude - loc1.latitude)
    val dLon = Math.toRadians(loc2.longitude - loc1.longitude)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(loc1.latitude)) * Math.cos(Math.toRadians(loc2.latitude)) * Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return r * c
}

@Preview(showBackground = true)
@Composable
fun FourthStagePreview() {
    ConnectNearTheme {
        FourthStageUI(
            uiState = MapUiState(myLocation = LatLng(52.2, 21.0), isRadarVisible = true),
            userSelection = UserSelection(name = "Test", category = "Sport"),
            onBackClick = {}, onChatClick = { _, _, _ -> }, onProfileClick = {}, onOtherUserProfileClick = {}, onUserSelected = {}, onUserDeselected = {}
        )
    }
}
