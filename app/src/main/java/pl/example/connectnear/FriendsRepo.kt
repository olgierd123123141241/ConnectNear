package pl.example.connectnear

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.FieldPath // <-- DODANY IMPORT
import kotlinx.coroutines.tasks.await

object FriendsRepo {
    private val db = FirebaseFirestore.getInstance()

    fun checkIfFriends(myUserId: String, targetUserId: String, onResult: (Boolean) -> Unit) {
        db.collection("users").document(myUserId).collection("friends").document(targetUserId).get()
            .addOnSuccessListener { onResult(it.exists()) }
            .addOnFailureListener { onResult(false) }
    }

    fun sendFriendRequest(myUserId: String, myName: String, targetUserId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val notification = mapOf(
            "type" to "friend_request",
            "senderId" to myUserId,
            "senderName" to myName,
            "timestamp" to FieldValue.serverTimestamp()
        )
        db.collection("users").document(targetUserId).collection("notifications").add(notification)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "") }
    }

    fun acceptFriendRequest(myUserId: String, senderId: String, senderName: String, onSuccess: () -> Unit) {
        val myFriendData = mapOf("name" to senderName, "since" to System.currentTimeMillis())
        db.collection("users").document(myUserId).collection("friends").document(senderId).set(myFriendData, SetOptions.merge())
            .addOnSuccessListener {
                db.collection("users").document(myUserId).get().addOnSuccessListener { myProfile ->
                    val myName = myProfile.getString("name") ?: "Nowy znajomy"
                    val theirFriendData = mapOf("name" to myName, "since" to System.currentTimeMillis())
                    db.collection("users").document(senderId).collection("friends").document(myUserId).set(theirFriendData, SetOptions.merge())
                        .addOnSuccessListener { onSuccess() }
                }
            }
    }

    // ZMODYFIKOWANA FUNKCJA
    fun getFriends(myUserId: String, onSuccess: (List<FoundUser>) -> Unit) {
        db.collection("users").document(myUserId).collection("friends").get()
            .addOnSuccessListener { friendsSnapshot ->
                val friendIds = friendsSnapshot.documents.map { it.id }
                if (friendIds.isEmpty()) {
                    onSuccess(emptyList())
                    return@addOnSuccessListener
                }

                // Pobieramy profile znajomych na podstawie ich ID
                db.collection("users").whereIn(FieldPath.documentId(), friendIds).get()
                    .addOnSuccessListener { usersSnapshot ->
                        val friendsList = usersSnapshot.documents.mapNotNull { doc ->
                            // RĘCZNE I BEZPIECZNE MAPOWANIE
                            try {
                                FoundUser(
                                    userId = doc.id, // Używamy ID dokumentu
                                    customId = doc.getString("customId") ?: "",
                                    location = doc.getGeoPoint("location") ?: GeoPoint(0.0, 0.0),
                                    name = doc.getString("name") ?: "[brak nazwy]",
                                    category = doc.getString("category") ?: "",
                                    sex = doc.getString("sex") ?: "",
                                    description = doc.getString("description") ?: "",
                                    profileImageUrl = doc.getString("profileImageUrl") ?: "",
                                    instagram = doc.getString("instagram") ?: "",
                                    facebook = doc.getString("facebook") ?: "",
                                    tiktok = doc.getString("tiktok") ?: "",
                                    messenger = doc.getString("messenger") ?: "",
                                    youtube = doc.getString("youtube") ?: "",
                                    youtubeStartTime = (doc.getLong("youtubeStartTime") ?: 0).toInt(),
                                    visibilityMode = doc.getString("visibilityMode") ?: "public",
                                    aiSuggestion = doc.getString("aiSuggestion")
                                )
                            } catch (e: Exception) {
                                // Jeśli wystąpi jakikolwiek błąd konwersji, pomijamy tego znajomego
                                null
                            }
                        }
                        onSuccess(friendsList)
                    }
                    .addOnFailureListener { 
                        onSuccess(emptyList()) // Zwracamy pustą listę w razie błędu
                    }
            }
            .addOnFailureListener { 
                onSuccess(emptyList()) // Zwracamy pustą listę w razie błędu
            }
    }

    fun removeFriend(myUserId: String, friendId: String, onSuccess: () -> Unit) {
        db.collection("users").document(myUserId).collection("friends").document(friendId).delete()
            .addOnSuccessListener { 
                db.collection("users").document(friendId).collection("friends").document(myUserId).delete()
                    .addOnSuccessListener { onSuccess() }
            }
    }
}
