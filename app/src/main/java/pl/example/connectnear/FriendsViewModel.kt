package pl.example.connectnear

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FriendsViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _friends = MutableStateFlow<List<FoundUser>>(emptyList())
    val friends: StateFlow<List<FoundUser>> = _friends

    private val _friendSuggestions = MutableStateFlow<List<FoundUser>>(emptyList())
    val friendSuggestions: StateFlow<List<FoundUser>> = _friendSuggestions

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _currentUserName = MutableStateFlow("")
    val currentUserName: StateFlow<String> = _currentUserName

    init {
        loadFriendsAndSuggestions()
    }

    private fun loadFriendsAndSuggestions() {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = auth.currentUser?.uid
            if (userId == null) {
                _isLoading.value = false
                return@launch
            }

            // Load current user's name
            FirebaseService.checkProfileAndGetName { name ->
                _currentUserName.value = name ?: ""
            }

            // Load friends
            FirebaseService.getFriends(userId) {
                _friends.value = it
            }

            // Load friend suggestions
            val userProfile = try {
                db.collection("users").document(userId).get().await().toObject(UserSelection::class.java)
            } catch (e: Exception) {
                null
            }

            if (userProfile != null && userProfile.facebookFriends.isNotEmpty()) {
                val suggestions = mutableListOf<FoundUser>()
                val friendsQuery = db.collection("users")
                    .whereIn("facebookId", userProfile.facebookFriends)
                    .get()
                    .await()

                for (document in friendsQuery.documents) {
                    val suggestedFriend = document.toObject(FoundUser::class.java)
                    if (suggestedFriend != null && suggestedFriend.userId != userId) {
                        // Check if they are already friends
                        val areFriends = _friends.value.any { it.userId == suggestedFriend.userId }
                        if (!areFriends) {
                            suggestions.add(suggestedFriend)
                        }
                    }
                }
                _friendSuggestions.value = suggestions
            }
            _isLoading.value = false
        }
    }
}
