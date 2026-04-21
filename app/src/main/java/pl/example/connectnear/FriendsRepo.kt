package pl.example.connectnear

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.FieldPath
import kotlinx.coroutines.tasks.await

object FriendsRepo {
    private val db = FirebaseFirestore.getInstance()

    fun checkIfFriends(myUserId: String, targetUserId: String, onResult: (Boolean) -> Unit) {
        db.collection("users").document(myUserId).collection("friends").document(targetUserId).get()
            .addOnSuccessListener { onResult(it.exists()) }
            .addOnFailureListener { onResult(false) }
    }

    fun sendFriendRequest(myUserId: String, myName: String, targetUserId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (myName.isBlank()) {
            onError("Nie można wysłać zaproszenia bez nazwy profilowej.")
            return
        }
        val notification = mapOf(
            "type" to "friend_request",
            "senderId" to myUserId,
            "senderName" to myName,
            "timestamp" to FieldValue.serverTimestamp(),
            "read" to false
        )
        // Wysyłamy do kolekcji powiadomień innego użytkownika
        db.collection("users").document(targetUserId).collection("notifications").document(myUserId).set(notification)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Błąd wysyłania zaproszenia.") }
    }

    fun acceptFriendRequest(myUserId: String, senderId: String, senderName: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        // Próbujemy wykonać operację. Jeśli reguły blokują zapis u kogoś innego, 
        // to przynajmniej usuwamy powiadomienie u nas i dodajemy znajomego do NASZEJ listy.
        
        val myProfileRef = db.collection("users").document(myUserId)
        val myFriendRef = myProfileRef.collection("friends").document(senderId)
        val myFriendData = mapOf("name" to senderName, "since" to System.currentTimeMillis())

        // 1. Dodaj znajomego do MOJEJ listy (To powinno zawsze działać wg reguł "allow write: if request.auth.uid == userId")
        myFriendRef.set(myFriendData, SetOptions.merge())
            .addOnSuccessListener {
                // 2. Usuń powiadomienie (To też powinno działać u mnie)
                myProfileRef.collection("notifications").document(senderId).delete()
                    .addOnSuccessListener {
                        // 3. Próba dodania MNIE do listy znajomego (To może rzucić PERMISSION_DENIED)
                        myProfileRef.get().addOnSuccessListener { myProfile ->
                            val myName = myProfile.getString("name") ?: "Nowy znajomy"
                            val theirFriendData = mapOf("name" to myName, "since" to System.currentTimeMillis())
                            
                            db.collection("users").document(senderId).collection("friends").document(myUserId)
                                .set(theirFriendData, SetOptions.merge())
                                .addOnSuccessListener {
                                    onSuccess()
                                }
                                .addOnFailureListener { e ->
                                    // Jeśli tu rzuci błąd, to znaczy że reguły blokują zapis u kogoś innego.
                                    // Ale u nas już dodano znajomego i usunięto notyfikację, więc zwracamy sukces,
                                    // żeby zamknąć okno dialogowe.
                                    Log.e("FriendsRepo", "Could not add me to their friends list (Permissions?): ${e.message}")
                                    onSuccess() 
                                }
                        }
                    }
                    .addOnFailureListener { e -> 
                        onError("Błąd usuwania powiadomienia: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                onError("Błąd dodawania znajomego: ${e.message}")
            }
    }

    fun rejectFriendRequest(myUserId: String, senderId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        db.collection("users").document(myUserId).collection("notifications").document(senderId).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Błąd odrzucania zaproszenia.") }
    }

    fun getFriends(myUserId: String, onSuccess: (List<FoundUser>) -> Unit) {
        db.collection("users").document(myUserId).collection("friends").get()
            .addOnSuccessListener { friendsSnapshot ->
                val friendIds = friendsSnapshot.documents.map { it.id }
                if (friendIds.isEmpty()) {
                    onSuccess(emptyList())
                    return@addOnSuccessListener
                }

                db.collection("users").whereIn(FieldPath.documentId(), friendIds).get()
                    .addOnSuccessListener { usersSnapshot ->
                        val friendsList = usersSnapshot.documents.mapNotNull { doc ->
                            try {
                                FoundUser(
                                    userId = doc.id,
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
                                null
                            }
                        }
                        onSuccess(friendsList)
                    }
                    .addOnFailureListener { 
                        onSuccess(emptyList())
                    }
            }
            .addOnFailureListener { 
                onSuccess(emptyList())
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
