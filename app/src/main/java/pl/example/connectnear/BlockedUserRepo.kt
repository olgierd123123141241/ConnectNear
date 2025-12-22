package pl.example.connectnear

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

object BlockedUserRepo {
    private val db = FirebaseFirestore.getInstance()

    fun blockUser(myUserId: String, targetUserId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        db.collection("users").document(myUserId).update("blockedUsers", FieldValue.arrayUnion(targetUserId))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "") }
    }

    fun unblockUser(myUserId: String, targetUserId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        db.collection("users").document(myUserId).update("blockedUsers", FieldValue.arrayRemove(targetUserId))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "") }
    }

    fun getBlockedUsers(myUserId: String, onResult: (List<String>) -> Unit) {
        db.collection("users").document(myUserId).get()
            .addOnSuccessListener { document ->
                val blockedUsers = document.get("blockedUsers") as? List<String> ?: emptyList()
                onResult(blockedUsers)
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    fun isUserBlocked(myUserId: String, targetUserId: String, onResult: (Boolean) -> Unit) {
        getBlockedUsers(myUserId) { blockedUsers ->
            onResult(blockedUsers.contains(targetUserId))
        }
    }
}
