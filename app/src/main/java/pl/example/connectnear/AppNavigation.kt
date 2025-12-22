package pl.example.connectnear

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun AppNavigation(
    navController: NavHostController,
    userSelection: UserSelection,
    startDestination: String
) {
    val friendsViewModel: FriendsViewModel = viewModel()
    val eventsViewModel: EventsViewModel = viewModel()

    NavHost(
        navController = navController, 
        startDestination = startDestination,
        modifier = Modifier.background(Color.Transparent) // Dodajemy przezroczyste tło
    ) {

        composable("terms_screen") {
            val context = LocalContext.current
            val prefs = remember { PreferenceManager(context) }
            val scope = rememberCoroutineScope()
            TermsScreen(onAcceptClick = {
                scope.launch {
                    prefs.setTermsAccepted()
                    navController.navigate("first_stage") { popUpTo(0) { inclusive = true } }
                }
            })
        }

        composable("first_stage") {
            FirstStage(
                modifier = Modifier.fillMaxSize(),
                onNextClick = { navController.navigate("auth_screen") }
            )
        }

        composable("auth_screen") {
            LaunchedEffect(Unit) {
                if (FirebaseService.auth.currentUser != null) {
                    navController.navigate("start_session_and_load") { popUpTo("auth_screen") { inclusive = true } }
                }
            }
            AuthScreen(
                onLoginSuccess = { navController.navigate("start_session_and_load") }
            )
        }

        composable("start_session_and_load") {
            LaunchedEffect(Unit) {
                val userId = FirebaseService.auth.currentUser?.uid
                if (userId != null) {
                    FirebaseService.getCurrentUserProfile { userProfile ->
                        if (userProfile != null) {
                            userSelection.updateWith(userProfile)
                            navController.navigate("fourth_stage") { popUpTo(0) { inclusive = true } }
                        } else {
                            userSelection.userId = userId
                            navController.navigate("login_info_screen") { popUpTo(0) { inclusive = true } }
                        }
                    }
                } else {
                    navController.navigate("auth_screen") { popUpTo(0) { inclusive = true } }
                }
            }
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        composable("login_info_screen") {
            LoginInfoScreen(
                userSelection = userSelection,
                onSaveSuccess = { navController.navigate("second_stage") },
            )
        }

        composable("second_stage") {
            SecondStage(
                modifier = Modifier.fillMaxSize(),
                initialUserSelection = userSelection,
                onBackClick = { navController.popBackStack() },
                onNextClick = { updatedUser ->
                    userSelection.updateWith(updatedUser)
                    FirebaseService.updateFullProfile(userSelection, onSuccess = { 
                        navController.navigate("third_stage")
                    }, onError = { 
                        // Show error message, do not navigate
                    })
                }
            )
        }

        composable("third_stage") {
            ThirdStage(
                modifier = Modifier.fillMaxSize(),
                initialMyAge = userSelection.myAge,
                initialPreferredAge = userSelection.preferredAge,
                initialMySex = userSelection.mySex,
                initialPreferredSex = userSelection.preferredSex,
                userCategory = userSelection.category,
                onBackClick = { navController.popBackStack() },
                onNextClick = { myAge, preferredAge, mySex, preferredSex ->
                    userSelection.myAge = myAge
                    userSelection.preferredAge = preferredAge
                    userSelection.mySex = mySex
                    userSelection.preferredSex = preferredSex
                    FirebaseService.saveCurrentUser(userSelection, 
                        onSuccess = { navController.navigate("fourth_stage") },
                        onError = { /* Możesz tu pokazać błąd */ }
                    )
                }
            )
        }
        
        composable("fourth_stage") {
            FourthStage(
                userSelection = userSelection,
                onBackClick = { navController.popBackStack() },
                onChatClick = { id, name, message -> 
                    val encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString())
                    navController.navigate("chat_screen/$id/$name?isGroup=false&initialMessage=$encodedMessage") 
                },
                onProfileClick = { navController.navigate("profile_screen") },
                onOtherUserProfileClick = { id -> navController.navigate("user_profile_screen/$id") }
            )
        }

        composable("friends_list_screen") {
            FriendsListScreen(
                userSelection = userSelection, 
                onBackClick = { navController.popBackStack() }, 
                onChatClick = { id, name, message -> 
                    val encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString())
                    navController.navigate("chat_screen/$id/$name?isGroup=false&initialMessage=$encodedMessage") 
                }
            )
        }

        composable("friends_screen") {
            FriendsScreen(friendsViewModel = friendsViewModel)
        }

        composable("events_screen") {
            EventsScreen(
                onBackClick = { navController.popBackStack() },
                onEventClick = { chatId -> navController.navigate("chat_screen/$chatId/Czat wydarzenia?isGroup=true") },
                eventsViewModel = eventsViewModel
            )
        }

        composable("groups_screen") {
            GroupsScreen(
                myUserId = userSelection.userId,
                userCategory = userSelection.category,
                onBackClick = { navController.popBackStack() },
                onGroupClick = { id, name -> navController.navigate("chat_screen/$id/$name?isGroup=true") },
                onPrivateChatClick = { id, name -> navController.navigate("chat_screen/$id/$name?isGroup=false") }
            )
        }

        composable("user_profile_screen/{targetUserId}") { backStackEntry ->
            val targetUserId = backStackEntry.arguments?.getString("targetUserId") ?: ""
            UserProfileScreen(
                targetUserId = targetUserId, 
                onBackClick = { navController.popBackStack() }, 
                onChatClick = { id, name, message ->
                    val encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString())
                    navController.navigate("chat_screen/$id/$name?isGroup=false&initialMessage=$encodedMessage")
                }
            )
        }

        composable(
            "chat_screen/{userId}/{userName}?isGroup={isGroup}&initialMessage={initialMessage}", 
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType }, 
                navArgument("userName") { type = NavType.StringType }, 
                navArgument("isGroup") { type = NavType.BoolType; defaultValue = false },
                navArgument("initialMessage") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val otherUserId = backStackEntry.arguments?.getString("userId") ?: ""
            val otherUserName = backStackEntry.arguments?.getString("userName") ?: ""
            val isGroup = backStackEntry.arguments?.getBoolean("isGroup") ?: false
            val initialMessage = backStackEntry.arguments?.getString("initialMessage") ?: ""
            ChatScreen(
                myUserId = userSelection.userId, 
                myUserName = userSelection.name, 
                otherUserId = otherUserId, 
                otherUserName = otherUserName, 
                userCategory = userSelection.category, 
                isGroup = isGroup, 
                initialMessage = initialMessage,
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable("profile_screen") {
            ProfileScreen(
                onLogoutClick = {
                    navController.navigate("auth_screen") { popUpTo(0) }
                },
                onEditPreferencesClick = { navController.navigate("edit_preferences_screen") },
                onBlockedUsersClick = { navController.navigate("blocked_users_screen") },
                onEditSocialsClick = { navController.navigate("edit_socials_screen") },
                onFriendsClick = { navController.navigate("friends_screen") },
                onChatsClick = { navController.navigate("friends_list_screen") },
                onGroupsClick = { navController.navigate("groups_screen") }
            )
        }

        composable("blocked_users_screen") {
            BlockedUsersScreen(myUserId = userSelection.userId, onBackClick = { navController.popBackStack() })
        }

        composable("edit_preferences_screen") {
            EditPreferencesScreen(
                userCategory = userSelection.category, 
                onBackClick = { navController.popBackStack() },
                onSaveSuccess = { navController.navigate("profile_screen") { popUpTo("profile_screen") { inclusive = true } } }
            )
        }

        composable("edit_socials_screen") {
            EditProfileScreen(
                onBackClick = { navController.navigate("profile_screen") { popUpTo("profile_screen") { inclusive = true } } },
                onSaveSuccess = { navController.navigate("profile_screen") { popUpTo("profile_screen") { inclusive = true } } }
            )
        }
    }
}
