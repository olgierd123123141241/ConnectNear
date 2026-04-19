package pl.example.connectnear

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@Composable
fun FourthStage(
    userSelection: UserSelection,
    onBackClick: () -> Unit,
    onChatClick: (String, String, String) -> Unit,
    onProfileClick: () -> Unit,
    onOtherUserProfileClick: (String) -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            // Ta logika jest teraz w rememberMapState
        }
    )

    val mapState = rememberMapState(userSelection, snackbarHostState, permissionLauncher)
    var isFriend by remember { mutableStateOf(false) }
    var showRadarPanel by remember { mutableStateOf(false) }
    var selectedFlashEvent by remember { mutableStateOf<FlashEvent?>(null) }

    LaunchedEffect(mapState.selectedUser) {
        val selected = mapState.selectedUser
        if (selected != null) {
            FirebaseService.checkIfFriends(userSelection.userId, selected.userId) { result ->
                isFriend = result
            }
        } else {
            isFriend = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FourthStageMap(
            myLocation = mapState.myLocation,
            myProfileImageUrl = userSelection.profileImageUrl,
            userCategory = mapState.userCategorySnapshot, 
            otherUsers = mapState.otherUsers,
            flashEvents = mapState.flashEvents, 
            onMapClick = { mapState.onUserDeselected() },
            onUserMarkerClick = { user -> mapState.onUserSelected(user) },
            onFlashEventClick = { event ->
                selectedFlashEvent = event
                if (event.isOneTime && !event.viewedBy.contains(userSelection.userId)) {
                    FirebaseService.markFlashEventAsViewed(event.id, userSelection.userId)
                }
            },
            onMapLongClick = {},
            hasPermission = mapState.hasPermission,
            onPermissionRequest = { mapState.requestPermissions() },
            selectedUser = mapState.selectedUser
        )

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.TopCenter).padding(top = 50.dp))
        FourthStageSettingsFab(onClick = { mapState.onSettingsClicked(userSelection) }) // POPRAWKA

        if (mapState.myLocation != null) {
            RadarPanel(
                usersNearby = mapState.otherUsers,
                myLocation = mapState.myLocation!!,
                isVisible = showRadarPanel,
                onUserClick = { user -> mapState.onUserSelected(user) },
                onAddFlashEvent = { event -> 
                    FirebaseService.addFlashEvent(event, 
                        onSuccess = { Toast.makeText(context, "Wydarzenie dodane!", Toast.LENGTH_SHORT).show() }, 
                        onError = { Toast.makeText(context, "Błąd: $it", Toast.LENGTH_SHORT).show() })
                },
                onClose = { showRadarPanel = false }
            )
        }
        
        if (selectedFlashEvent != null) {
            val event = selectedFlashEvent!!
            Dialog(onDismissRequest = { selectedFlashEvent = null }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .background(Color.White, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(event.title, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (event.imageUrl.isNotEmpty()) {
                             Image(
                                painter = rememberAsyncImagePainter(event.imageUrl),
                                contentDescription = "Zdjęcie wydarzenia",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .padding(vertical = 8.dp),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Text(event.description)
                        Spacer(Modifier.height(8.dp))
                        
                        val timeLeftMinutes = (event.expiresAt - System.currentTimeMillis()) / 60000
                        Text("Wygasa za: ${if (timeLeftMinutes > 0) timeLeftMinutes else 0} min", color = Color.Gray)
                        
                        if (event.isOneTime) {
                            Text("To zdjęcie zniknie po zamknięciu!", color = Color.Red, style = MaterialTheme.typography.labelSmall)
                        }

                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { selectedFlashEvent = null }) {
                            Text("Zamknij")
                        }
                    }
                }
            }
        }

        FourthStageControls(
            selectedUser = mapState.selectedUser,
            isFriend = isFriend,
            onChatClick = { mapState.selectedUser?.let { onChatClick(it.userId, it.name, "") } },
            onSendRequestClick = {
                mapState.selectedUser?.let { user ->
                    FirebaseService.sendFriendRequest(
                        userSelection.userId, userSelection.name, user.userId,
                        onSuccess = { Toast.makeText(context, "Wysłano zaproszenie!", Toast.LENGTH_SHORT).show() },
                        onError = { err -> Toast.makeText(context, "Błąd: $err", Toast.LENGTH_SHORT).show() }
                    )
                }
            },
            onOtherProfileClick = { mapState.selectedUser?.let { onOtherUserProfileClick(it.userId) } },
            onCloseUserClick = { mapState.onUserDeselected() },
            onExitClick = onBackClick,
            onMyProfileClick = onProfileClick,
            onRadarClick = { showRadarPanel = !showRadarPanel }
        )

        if (mapState.friendRequest.value != null) {
            val request = mapState.friendRequest.value!!
            FourthStageFriendRequestDialog(
                senderName = request.senderName,
                onAccept = {
                    FirebaseService.acceptFriendRequest(
                        myUserId = userSelection.userId,
                        senderId = request.senderId,
                        senderName = request.senderName,
                        onSuccess = {
                            Toast.makeText(context, "Dodano ${request.senderName} do znajomych!", Toast.LENGTH_SHORT).show()
                            mapState.onFriendRequestHandled()
                        },
                        onError = { error ->
                            Toast.makeText(context, "Błąd: $error", Toast.LENGTH_SHORT).show()
                            mapState.onFriendRequestHandled()
                        }
                    )
                },
                onReject = {
                    FirebaseService.rejectFriendRequest(
                        myUserId = userSelection.userId,
                        senderId = request.senderId,
                        onSuccess = {
                            Toast.makeText(context, "Odrzucono zaproszenie.", Toast.LENGTH_SHORT).show()
                            mapState.onFriendRequestHandled()
                        },
                        onError = { error ->
                            Toast.makeText(context, "Błąd: $error", Toast.LENGTH_SHORT).show()
                            mapState.onFriendRequestHandled()
                        }
                    )
                }
            )
        }

        if (mapState.showSettingsDialog) {
            val settings = mapState.settingsState
            FourthStageSettingsDialog(
                isSharingLocation = settings.isSharingLocation,
                onSharingChange = { settings.isSharingLocation = it },
                tempCategory = settings.tempCategory,
                onCategoryChange = { settings.tempCategory = it },
                expandedCategory = settings.expandedCategory,
                onExpandedCategoryChange = { settings.expandedCategory = it },
                tempVisibilityMode = settings.tempVisibilityMode,
                onVisibilityChange = { settings.tempVisibilityMode = it },
                tempInterval = settings.tempInterval,
                onIntervalChange = { settings.tempInterval = it },
                expandedIntervals = settings.expandedIntervals,
                onExpandedChange = { settings.expandedIntervals = it },
                intervalNames = settings.intervalNames,
                intervals = settings.intervals,
                tempRadius = settings.tempRadius,
                onRadiusChange = { settings.tempRadius = it },
                expandedRadius = settings.expandedRadius,
                onExpandedRadiusChange = { settings.expandedRadius = it },
                radiuses = settings.radiuses,

                tempPreferredAge = settings.tempPreferredAge,
                onPreferredAgeChange = { settings.tempPreferredAge = it },
                expandedPreferredAge = settings.expandedPreferredAge,
                onExpandedPreferredAgeChange = { settings.expandedPreferredAge = it },
                tempPreferredSex = settings.tempPreferredSex,
                onPreferredSexChange = { settings.tempPreferredSex = it },
                expandedPreferredSex = settings.expandedPreferredSex,
                onExpandedPreferredSexChange = { settings.expandedPreferredSex = it },

                tempSportMode = settings.tempSportMode,
                onSportModeChange = { settings.tempSportMode = it },
                tempSportLevel = settings.tempSportLevel,
                onSportLevelChange = { settings.tempSportLevel = it },
                tempSubCategory = settings.tempSubCategory,
                onSubCategoryChange = { settings.tempSubCategory = it },

                tempLearningMode = settings.tempLearningMode,
                onLearningModeChange = { settings.tempLearningMode = it },
                tempSubject = settings.tempSubject,
                onSubjectChange = { settings.tempSubject = it },
                tempLearningLevel = settings.tempLearningLevel,
                onLearningLevelChange = { settings.tempLearningLevel = it },

                tempDateMode = settings.tempDateMode,
                onDateModeChange = { settings.tempDateMode = it },
                tempDatePartnerGender = settings.tempDatePartnerGender,
                onDatePartnerGenderChange = { settings.tempDatePartnerGender = it },
                tempDateCoupleGender = settings.tempDateCoupleGender,
                onDateCoupleGenderChange = { settings.tempDateCoupleGender = it },
                tempDateAnimalType = settings.tempDateAnimalType,
                onDateAnimalTypeChange = { settings.tempDateAnimalType = it },
                tempDateOtherAnimal = settings.tempDateOtherAnimal,
                onDateOtherAnimalChange = { settings.tempDateOtherAnimal = it },

                tempPartyType = settings.tempPartyType,
                onPartyTypeChange = { settings.tempPartyType = it },

                onClose = { mapState.onSettingsClosed(userSelection) } // POPRAWKA
            )
        }
    }
}
