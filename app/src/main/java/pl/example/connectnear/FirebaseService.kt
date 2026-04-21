package pl.example.connectnear

import android.net.Uri
import android.os.Bundle
import com.facebook.AccessToken
import com.facebook.GraphRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.GeoPoint
import java.util.UUID
import kotlinx.coroutines.tasks.await

// FINALNA, KOMPLETNA WERSJA
object FirebaseService {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // --- AUTORYZACJA ---
    fun signUp(email: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) =
        AuthRepo.signUp(email, pass, onSuccess, onError)

    fun signIn(email: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) =
        AuthRepo.signIn(email, pass, onSuccess, onError)

    fun signInWithGoogle(idToken: String, onSuccess: () -> Unit, onError: (String) -> Unit) =
        AuthRepo.signInWithGoogle(idToken, onSuccess, onError)

    fun signInWithFacebook(accessToken: String, onSuccess: () -> Unit, onError: (String) -> Unit) =
        AuthRepo.signInWithFacebook(accessToken, onSuccess, onError)

    fun signOut() = AuthRepo.signOut()

    fun sendPasswordResetEmail(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) =
        AuthRepo.sendPasswordResetEmail(email, onSuccess, onError)

    fun updateUserEmail(newEmail: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) =
        AuthRepo.updateUserEmail(newEmail, pass, onSuccess, onError)

    fun updatePassword(newPass: String, oldPass: String, onSuccess: () -> Unit, onError: (String) -> Unit) =
        AuthRepo.updatePassword(newPass, oldPass, onSuccess, onError)

    fun deleteAccount(pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) =
        AuthRepo.deleteAccount(pass, onSuccess, onError)
        
    fun clearProfileData(onSuccess: () -> Unit, onError: (String) -> Unit) =
        AuthRepo.clearProfileData(onSuccess, onError)

    // --- PROFIL ---
    fun saveCurrentUser(userSelection: UserSelection, onSuccess: () -> Unit, onError: (String) -> Unit) {
        ProfileRepo.saveCurrentUser(userSelection, onSuccess, onError)
    }

    fun updateFullProfile(user: UserSelection, onSuccess: () -> Unit, onError: (String) -> Unit) =
        ProfileRepo.updateFullProfile(user, onSuccess, onError)

    fun updateProfileName(newName: String, onSuccess: () -> Unit, onError: (String) -> Unit) =
        ProfileRepo.updateProfileName(newName, onSuccess, onError)

    fun getCurrentUserProfile(onSuccess: (UserSelection?) -> Unit) =
        ProfileRepo.getCurrentUserProfile(onSuccess)

    fun getUserDetails(userId: String, onSuccess: (UserSelection) -> Unit) =
        ProfileRepo.getUserDetails(userId, onSuccess)

    fun checkProfileAndGetName(onResult: (String?) -> Unit) =
        ProfileRepo.checkProfileAndGetName(onResult)

    fun checkIfNameExists(name: String, onResult: (Boolean) -> Unit) =
        ProfileRepo.checkIfNameExists(name, onResult)

    // --- ZNAJOMI ---
    fun checkIfFriends(myUserId: String, targetUserId: String, onResult: (Boolean) -> Unit) =
        FriendsRepo.checkIfFriends(myUserId, targetUserId, onResult)

    fun sendFriendRequest(myUserId: String, myName: String, targetUserId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        BlockedUserRepo.isUserBlocked(myUserId, targetUserId) { isBlocked ->
            if (!isBlocked) {
                FriendsRepo.sendFriendRequest(myUserId, myName, targetUserId, onSuccess, onError)
            } else {
                onError("Nie można wysłać zaproszenia do zablokowanego użytkownika.")
            }
        }
    }

    fun acceptFriendRequest(myUserId: String, senderId: String, senderName: String, onSuccess: () -> Unit, onError: (String) -> Unit) =
        FriendsRepo.acceptFriendRequest(myUserId, senderId, senderName, onSuccess, onError)

    fun rejectFriendRequest(myUserId: String, senderId: String, onSuccess: () -> Unit, onError: (String) -> Unit) =
        FriendsRepo.rejectFriendRequest(myUserId, senderId, onSuccess, onError)

    fun getFriends(myUserId: String, onSuccess: (List<FoundUser>) -> Unit) =
        FriendsRepo.getFriends(myUserId, onSuccess)

    fun removeFriend(myUserId: String, friendId: String, onSuccess: () -> Unit) =
        FriendsRepo.removeFriend(myUserId, friendId, onSuccess)

    // --- GRUPY ---
    fun createGroup(groupName: String, ownerId: String, memberIds: List<String>, isPublic: Boolean, onSuccess: () -> Unit) =
        GroupRepo.createGroup(groupName, ownerId, memberIds, isPublic, onSuccess)

    fun getMyGroups(myUserId: String, onSuccess: (List<Group>) -> Unit) =
        GroupRepo.getMyGroups(myUserId, onSuccess)

    fun getPublicGroups(onSuccess: (List<Group>) -> Unit) =
        GroupRepo.getPublicGroups(onSuccess)

    fun requestJoinGroup(groupId: String, myUserId: String, onSuccess: () -> Unit) =
        GroupRepo.requestJoinGroup(groupId, myUserId, onSuccess)

    fun approveRequest(groupId: String, userIdToApprove: String, onSuccess: () -> Unit) =
        GroupRepo.approveRequest(groupId, userIdToApprove, onSuccess)

    fun rejectRequest(groupId: String, userIdToReject: String, onSuccess: () -> Unit) =
        GroupRepo.rejectRequest(groupId, userIdToReject, onSuccess)

    // --- CZAT ---
    fun getChatId(user1: String, user2: String): String =
        ChatRepo.getChatId(user1, user2)

    fun sendMessage(chatId: String, message: ChatMessage, receiverId: String, senderName: String, isGroup: Boolean = false) {
         BlockedUserRepo.isUserBlocked(receiverId, auth.currentUser?.uid ?: "") { isBlocked ->
            if (!isBlocked) {
                ChatRepo.sendMessage(chatId, message, receiverId, senderName, isGroup)
            }
        }
    }

    fun getMessages(chatId: String, onUpdate: (List<ChatMessage>) -> Unit) =
        ChatRepo.getMessages(chatId, onUpdate)

    fun deleteMessage(chatId: String, messageId: String) =
        ChatRepo.deleteMessage(chatId, messageId)

    fun markMessagesAsRead(chatId: String, myUserId: String) =
        ChatRepo.markMessagesAsRead(chatId, myUserId)

    fun markMessageAsViewed(chatId: String, messageId: String, userId: String) =
        ChatRepo.markMessageAsViewed(chatId, messageId, userId)

    fun pinMessage(chatId: String, messageId: String, isPinned: Boolean) =
        ChatRepo.pinMessage(chatId, messageId, isPinned)

    fun updateGameMessage(chatId: String, messageId: String, updates: Map<String, Any>) {
        db.collection("chats").document(chatId).collection("messages").document(messageId).update(updates)
    }

    // --- TYPING ---
    fun setTypingStatus(chatId: String, userId: String, isTyping: Boolean) =
        ChatRepo.setTypingStatus(chatId, userId, isTyping)

    fun listenForTypingStatus(chatId: String, myUserId: String, onTypingChange: (List<String>) -> Unit): ListenerRegistration =
        ChatRepo.listenForTypingStatus(chatId, myUserId, onTypingChange)

    // --- PLIKI ---
    fun uploadFileToStorage(uri: Uri, folder: String, userId: String, fileName: String? = null, onSuccess: (String) -> Unit, onError: (String) -> Unit) =
        ChatRepo.uploadFileToStorage(uri, folder, userId, fileName, onSuccess, onError)

    fun deleteFileFromStorage(fileUrl: String, onSuccess: () -> Unit, onError: (String) -> Unit) =
        ChatRepo.deleteFileFromStorage(fileUrl, onSuccess, onError)

    // --- POWIADOMIENIA ---
    fun listenForNotifications(myUserId: String, onNewNotification: (Map<String, Any>) -> Unit): ListenerRegistration =
        ChatRepo.listenForNotifications(myUserId, onNewNotification)

    // --- MAPA ---
    fun findMatches(mySelection: UserSelection, onResult: (List<FoundUser>) -> Unit) {
        BlockedUserRepo.getBlockedUsers(mySelection.userId) { blockedUsers ->
            NearbyUsersFinder.find(mySelection) { foundUsers ->
                onResult(foundUsers.filter { it.userId !in blockedUsers })
            }
        }
    }

    fun addFlashEvent(event: FlashEvent, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val newEventId = db.collection("flash_events").document().id
        val newGroupId = db.collection("groups").document().id
        val userId = auth.currentUser?.uid ?: return

        // 1. Create a group for the event
        val eventGroup = Group(
            groupId = newGroupId,
            name = event.title,
            ownerId = userId,
            memberIds = listOf(userId)
        )

        db.collection("groups").document(newGroupId).set(eventGroup)
            .addOnSuccessListener {
                // 2. Create the event with the chatId
                val eventWithChat = event.copy(
                    id = newEventId,
                    chatId = newGroupId,
                    participants = listOf(userId)
                )
                db.collection("flash_events").document(newEventId).set(eventWithChat)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e -> onError("Error saving event: ${e.message}") }
            }
            .addOnFailureListener { e -> onError("Error creating group: ${e.message}") }
    }

    fun joinFlashEvent(eventId: String, userId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val eventRef = db.collection("flash_events").document(eventId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(eventRef)
            val event = snapshot.toObject(FlashEvent::class.java)
                ?: throw FirebaseFirestoreException(
                    "Event not found!",
                    FirebaseFirestoreException.Code.NOT_FOUND
                ) as Throwable

            if (event.participants.contains(userId)) {
                throw FirebaseFirestoreException("Already a participant.", FirebaseFirestoreException.Code.ALREADY_EXISTS)
            }

            // Add user to event participants and group members
            transaction.update(eventRef, "participants", FieldValue.arrayUnion(userId))
            val groupRef = db.collection("groups").document(event.chatId)
            transaction.update(groupRef, "memberIds", FieldValue.arrayUnion(userId))

            null
        }.addOnSuccessListener { onSuccess() }
         .addOnFailureListener { onError(it.message ?: "Error joining event.") }
    }

    fun getActiveFlashEvents(location: GeoPoint, radiusKm: Double, onResult: (List<FlashEvent>) -> Unit) =
        MapRepo.getActiveFlashEvents(location, radiusKm, onResult)

    fun markFlashEventAsViewed(eventId: String, userId: String) =
        MapRepo.markFlashEventAsViewed(eventId, userId)

    // --- BLOKOWANIE ---
    fun blockUser(myUserId: String, targetUserId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        BlockedUserRepo.blockUser(myUserId, targetUserId, {
            FriendsRepo.removeFriend(myUserId, targetUserId) { // Also remove from friends
                onSuccess()
            }
        }, onError)
    }

    fun unblockUser(myUserId: String, targetUserId: String, onSuccess: () -> Unit, onError: (String) -> Unit) =
        BlockedUserRepo.unblockUser(myUserId, targetUserId, onSuccess, onError)

    fun getBlockedUsers(myUserId: String, onResult: (List<String>) -> Unit) =
        BlockedUserRepo.getBlockedUsers(myUserId, onResult)

    // --- SESJA ---
    suspend fun startNewSession(userId: String): String {
        val sessionToken = UUID.randomUUID().toString()
        val updates = mapOf("currentSessionToken" to sessionToken, "lastLogin" to System.currentTimeMillis())
        db.collection("users").document(userId).set(updates, SetOptions.merge()).await()
        return sessionToken
    }

    suspend fun checkSessionValidity(userId: String, localToken: String): Boolean {
        if (localToken.isEmpty()) return false
        return try {
            val document = db.collection("users").document(userId).get().await()
            val serverToken = document.getString("currentSessionToken")
            if (serverToken == null) return false
            serverToken == localToken
        } catch (e: Exception) {
            true
        }
    }
    
    fun clearUserLocation(onSuccess: () -> Unit = {}) {
        val userId = auth.currentUser?.uid ?: return
        val updates = mapOf<String, Any?>("location" to null)
        db.collection("users").document(userId).update(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { /* Ignoruj */ }
    }

    fun fetchAndStoreFacebookFriends(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val accessToken = AccessToken.getCurrentAccessToken()
        if (accessToken == null || accessToken.isExpired) {
            onError("Not logged in to Facebook.")
            return
        }

        val request = GraphRequest.newMyFriendsRequest(accessToken) { jsonArray, response ->
            if (response?.error != null) {
                onError(response.error?.errorMessage ?: "Facebook Graph API request failed.")
                return@newMyFriendsRequest
            }
            val friendsList = mutableListOf<String>()
            // The `jsonArray` parameter directly contains the list of friends.
            if (jsonArray != null) {
                for (i in 0 until jsonArray.length()) {
                    val friendObject = jsonArray.optJSONObject(i)
                    if (friendObject != null) {
                        val friendId = friendObject.optString("id")
                        if (friendId.isNotEmpty()) {
                            friendsList.add(friendId)
                        }
                    }
                }
            }

            val userId = auth.currentUser?.uid
            if (userId != null) {
                val userFacebookId = accessToken.userId
                val updates = mapOf(
                    "facebookFriends" to friendsList,
                    "facebookId" to userFacebookId
                )
                db.collection("users").document(userId)
                    .update(updates)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onError(it.message ?: "Error saving friends list.") }
            } else {
                onError("User not logged in.")
            }
        }

        val parameters = Bundle()
        parameters.putString("fields", "id,name")
        request.parameters = parameters
        request.executeAsync()
    }
}
