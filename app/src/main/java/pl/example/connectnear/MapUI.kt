package pl.example.connectnear

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.google.maps.android.compose.*
import pl.example.connectnear.ui.theme.getCategoryPrimaryColor

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

// --- KOMPONENTY POMOCNICZE DLA ZNACZNIKÓW ---

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

    val targetPosition = LatLng(user.location.latitude, user.location.longitude)
    val markerState = rememberMarkerState(
        key = user.userId,
        position = targetPosition
    )
    // Aktualizacja pozycji znacznika, gdy lokalizacja użytkownika się zmienia
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
        // Styl a'la Instagram Story
        // Jeśli wydarzenie jednorazowe, może inny kolor ramki?
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
                    // Wyświetlamy np. pierwszą literę kategorii
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
