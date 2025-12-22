package pl.example.connectnear

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class FriendRequestData(val senderId: String, val senderName: String)

class SettingsState {
    var isSharingLocation by mutableStateOf(false)
    var tempCategory by mutableStateOf("")
    var tempVisibilityMode by mutableStateOf("public")
    var tempInterval by mutableStateOf(5000L)
    var tempRadius by mutableStateOf(25.0)
    var tempPreferredAge by mutableStateOf("")
    var tempPreferredSex by mutableStateOf("")

    // Sport
    var tempSportMode by mutableStateOf("")
    var tempSportLevel by mutableStateOf("")
    var tempSubCategory by mutableStateOf("")
    
    // Nauka
    var tempLearningMode by mutableStateOf("")
    var tempSubject by mutableStateOf("")
    var tempLearningLevel by mutableStateOf("")

    // Randka
    var tempDateMode by mutableStateOf("")
    var tempDatePartnerGender by mutableStateOf("")
    var tempDateCoupleGender by mutableStateOf("")
    var tempDateAnimalType by mutableStateOf("")
    var tempDateOtherAnimal by mutableStateOf("")

    // Impreza (NOWE)
    var tempPartyType by mutableStateOf("")

    var expandedCategory by mutableStateOf(false)
    var expandedIntervals by mutableStateOf(false)
    var expandedRadius by mutableStateOf(false)
    var expandedPreferredAge by mutableStateOf(false)
    var expandedPreferredSex by mutableStateOf(false)

    val intervals = mapOf("5s" to 5000L, "30s" to 30000L, "1m" to 60000L, "5m" to 300000L)
    val intervalNames = intervals.entries.associate { (k, v) -> v to k }
    val radiuses = listOf(5.0, 10.0, 25.0, 50.0, 100.0)

    fun load(userSelection: UserSelection) {
        isSharingLocation = userSelection.shareLocation
        tempCategory = userSelection.category
        tempVisibilityMode = userSelection.visibilityMode
        tempInterval = userSelection.locationUpdateInterval
        tempRadius = userSelection.searchRadiusKm
        tempPreferredAge = userSelection.preferredAge
        tempPreferredSex = userSelection.preferredSex

        tempSportMode = userSelection.sportMode
        tempSportLevel = userSelection.sportLevel
        tempSubCategory = userSelection.subCategory
        tempLearningMode = userSelection.learningMode
        tempSubject = userSelection.subject
        tempLearningLevel = userSelection.learningLevel
        
        tempDateMode = userSelection.dateMode
        tempDatePartnerGender = userSelection.datePartnerGender
        tempDateCoupleGender = userSelection.dateCoupleGender
        tempDateAnimalType = userSelection.dateAnimalType
        tempDateOtherAnimal = userSelection.dateOtherAnimal

        tempPartyType = userSelection.partyType
    }

    fun applyToUserSelection(userSelection: UserSelection) {
        userSelection.shareLocation = isSharingLocation
        userSelection.category = tempCategory
        userSelection.visibilityMode = tempVisibilityMode
        userSelection.locationUpdateInterval = tempInterval
        userSelection.searchRadiusKm = tempRadius
        userSelection.preferredAge = tempPreferredAge
        userSelection.preferredSex = tempPreferredSex

        userSelection.sportMode = tempSportMode
        userSelection.sportLevel = tempSportLevel
        userSelection.subCategory = tempSubCategory
        userSelection.learningMode = tempLearningMode
        userSelection.subject = tempSubject
        userSelection.learningLevel = tempLearningLevel
        
        userSelection.dateMode = tempDateMode
        userSelection.datePartnerGender = tempDatePartnerGender
        userSelection.dateCoupleGender = tempDateCoupleGender
        userSelection.dateAnimalType = userSelection.dateAnimalType
        userSelection.dateOtherAnimal = userSelection.dateOtherAnimal

        userSelection.partyType = tempPartyType
    }
}

class MapState(
    private val scope: CoroutineScope,
    private val snackbarHostState: SnackbarHostState,
    val permissionLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>
) {
    var myLocation by mutableStateOf<LatLng?>(null)
    var otherUsers by mutableStateOf<List<FoundUser>>(emptyList())
    var flashEvents by mutableStateOf<List<FlashEvent>>(emptyList())
    var selectedUser by mutableStateOf<FoundUser?>(null)
    var hasPermission by mutableStateOf(false)
    var showSettingsDialog by mutableStateOf(false)

    var userCategorySnapshot by mutableStateOf("")

    val friendRequest = mutableStateOf<FriendRequestData?>(null)
    val settingsState = SettingsState()

    fun requestPermissions() {
        permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    fun onUserSelected(user: FoundUser) {
        selectedUser = user
    }

    fun onUserDeselected() {
        selectedUser = null
    }

    fun onFriendRequestHandled() {
        friendRequest.value = null
    }

    fun onSettingsClicked(userSelection: UserSelection) {
        settingsState.load(userSelection)
        showSettingsDialog = true
    }

    fun onSettingsClosed(userSelection: UserSelection) {
        showSettingsDialog = false
        saveSettings(userSelection)
    }

    private fun saveSettings(userSelection: UserSelection) {
        settingsState.applyToUserSelection(userSelection)
        userCategorySnapshot = userSelection.category
        scope.launch { FirebaseService.updateFullProfile(userSelection, {}, {}) }
    }
    
    fun createFlashEvent(userSelection: UserSelection, description: String, durationMs: Long, imageUrl: String? = null, isOneTime: Boolean = false) {
        if (myLocation != null) {
            val event = FlashEvent(
                id = "",
                creatorId = userSelection.userId,
                category = userSelection.category,
                location = GeoPoint(myLocation!!.latitude, myLocation!!.longitude),
                description = description,
                timestamp = System.currentTimeMillis(),
                expiresAt = System.currentTimeMillis() + durationMs,
                imageUrl = imageUrl ?: "",
                isOneTime = isOneTime
            )
            FirebaseService.addFlashEvent(event, {}, {})
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun rememberMapState(
    userSelection: UserSelection,
    snackbarHostState: SnackbarHostState,
    permissionLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
    scope: CoroutineScope = rememberCoroutineScope(),
): MapState {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val mapState = remember(snackbarHostState, scope, permissionLauncher) {
        MapState(scope, snackbarHostState, permissionLauncher)
    }

    // Inteligentny nasłuch na powiadomienia
    DisposableEffect(userSelection.userId) {
        var listener: ListenerRegistration? = null
        if (userSelection.userId.isNotEmpty()) {
            Log.d("NotificationListener", "Uruchamiam nasłuch dla userId: ${userSelection.userId}")
            listener = FirebaseService.listenForNotifications(userSelection.userId) { data ->
                val type = data["type"] as? String
                if (type == "friend_request") {
                    Log.d("NotificationListener", "Otrzymano zaproszenie do znajomych!")
                    mapState.friendRequest.value = FriendRequestData(
                        senderId = data["senderId"] as? String ?: "",
                        senderName = data["senderName"] as? String ?: "Ktoś"
                    )
                } else {
                    scope.launch {
                        val senderName = data["senderName"] as? String ?: "Ktoś"
                        val text = data["text"] as? String ?: ""
                        snackbarHostState.showSnackbar("$senderName: $text", duration = SnackbarDuration.Short)
                    }
                }
            }
        }
        onDispose {
            Log.d("NotificationListener", "Zatrzymuję nasłuch.")
            listener?.remove()
        }
    }

    mapState.hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    LaunchedEffect(Unit) {
        if (!mapState.hasPermission) {
            mapState.requestPermissions()
        }
    }

    // Strażnik odświeżania mapy
    LaunchedEffect(userSelection.locationUpdateInterval, mapState.hasPermission, userSelection.shareLocation, userSelection.category, userSelection.preferredAge, userSelection.preferredSex) { 
        if (mapState.hasPermission) {
            while (isActive) {
                try {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            val currentLatLng = LatLng(location.latitude, location.longitude)
                            mapState.myLocation = currentLatLng
                            userSelection.location = GeoPoint(location.latitude, location.longitude)
                            userSelection.timestamp = System.currentTimeMillis()

                            if (userSelection.shareLocation) {
                                FirebaseService.saveCurrentUser(userSelection, 
                                    onSuccess = {
                                        NearbyUsersFinder.find(userSelection) { users ->
                                            if(mapState.otherUsers != users) mapState.otherUsers = users
                                        }
                                    }, 
                                    onError = {}
                                )
                            } else {
                                mapState.otherUsers = emptyList()
                            }

                            if (userSelection.location != null) {
                                FirebaseService.getActiveFlashEvents(userSelection.location!!, userSelection.searchRadiusKm) { events ->
                                    mapState.flashEvents = events.filter { event ->
                                        val isCategoryMatch = (event.category == userSelection.category || userSelection.category.isEmpty())
                                        val isViewedAndOneTime = event.isOneTime && event.viewedBy.contains(userSelection.userId)
                                        isCategoryMatch && !isViewedAndOneTime
                                    }
                                }
                            }
                        }
                    }
                } catch (e: SecurityException) { }
                delay(userSelection.locationUpdateInterval)
            }
        }
    }

    return mapState
}
