package pl.example.connectnear

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Reprezentuje stan interfejsu użytkownika dla ekranu mapy.
 */
data class MapUiState(
    val myLocation: LatLng? = null,
    val otherUsers: List<FoundUser> = emptyList(),
    val flashEvents: List<FlashEvent> = emptyList(),
    val selectedUser: FoundUser? = null,
    val isFriend: Boolean = false,
    val hasPermission: Boolean = false,
    val showSettingsDialog: Boolean = false
)

/**
 * ViewModel dla ekranu FourthStage.
 * To jest "mózg" operacji dla ekranu mapy. Przechowuje cały stan i obsługuje logikę.
 */
class FourthStageViewModel : ViewModel() {

    var uiState by mutableStateOf(MapUiState())
        private set

    private var locationCallback: LocationCallback? = null

    fun handlePermissions(context: Context, userSelection: UserSelection, onPermissionNeeded: () -> Unit) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            uiState = uiState.copy(hasPermission = true)
            startLocationUpdates(context, userSelection)
        } else {
            uiState = uiState.copy(hasPermission = false)
            onPermissionNeeded()
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(context: Context, userSelection: UserSelection) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        if (locationCallback == null) {
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let { location ->
                        val newLatLng = LatLng(location.latitude, location.longitude)
                        uiState = uiState.copy(myLocation = newLatLng)
                        userSelection.location = GeoPoint(location.latitude, location.longitude)
                        userSelection.timestamp = System.currentTimeMillis()

                        if (userSelection.shareLocation) {
                            viewModelScope.launch {
                                FirebaseService.saveCurrentUser(userSelection,
                                    onSuccess = {
                                        Log.d("ViewModel", "Lokalizacja i czas zapisane. Odświeżam listę użytkowników.")
                                        findNearbyUsersAndEvents(userSelection)
                                    },
                                    onError = { Log.e("ViewModel", "Błąd zapisu lokalizacji: $it") }
                                )
                            }
                        } else {
                            uiState = uiState.copy(otherUsers = emptyList(), flashEvents = emptyList())
                        }
                    }
                }
            }

            val locationRequest = LocationRequest.create().apply {
                interval = userSelection.locationUpdateInterval
                fastestInterval = userSelection.locationUpdateInterval / 2
                priority = Priority.PRIORITY_HIGH_ACCURACY
            }
            
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback!!, Looper.getMainLooper())
        }
    }

    private fun findNearbyUsersAndEvents(userSelection: UserSelection) {
        FirebaseService.getBlockedUsers(userSelection.userId) { blockList ->
            NearbyUsersFinder.find(userSelection) { users ->
                uiState = uiState.copy(otherUsers = users.filter { it.userId !in blockList })
            }
            if (userSelection.location != null) {
                FirebaseService.getActiveFlashEvents(userSelection.location!!, userSelection.searchRadiusKm) { events ->
                    uiState = uiState.copy(flashEvents = events.filter { event ->
                        !blockList.contains(event.creatorId) &&
                        (event.category == userSelection.category || userSelection.category.isEmpty()) &&
                        !(event.isOneTime && event.viewedBy.contains(userSelection.userId))
                    })
                }
            }
        }
    }

    fun onUserSelected(user: FoundUser) {
        uiState = uiState.copy(selectedUser = user)
        FirebaseService.checkIfFriends(AuthRepo.getCurrentUserId() ?: "", user.userId) { isFriend ->
            uiState = uiState.copy(isFriend = isFriend)
        }
    }

    fun onUserDeselected() {
        uiState = uiState.copy(selectedUser = null, isFriend = false)
    }

    fun onSettingsClicked() {
        uiState = uiState.copy(showSettingsDialog = true)
    }

    fun onSettingsDismissed() {
        uiState = uiState.copy(showSettingsDialog = false)
    }
    
    override fun onCleared() {
        locationCallback?.let {
            // Należy usunąć callback, aby uniknąć wycieków pamięci
        }
        super.onCleared()
    }
}
