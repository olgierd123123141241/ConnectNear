package pl.example.connectnear

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

object ProfileRepo {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseService.auth

    fun saveCurrentUser(userSelection: UserSelection, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onError("Użytkownik niezalogowany, nie można zapisać lokalizacji.")
            return
        }

        Log.d("ProfileRepo", "Próba zapisu lokalizacji dla userId: $userId")

        val locationData = mapOf(
            "location" to userSelection.location,
            "timestamp" to userSelection.timestamp
        )

        db.collection("users").document(userId)
            .set(locationData, SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Błąd zapisu lokalizacji") }
    }

    fun updateFullProfile(user: UserSelection, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).set(user, SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Błąd zapisu") }
    }

    fun updateProfileName(newName: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).update("name", newName)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Błąd zapisu") }
    }

    fun getCurrentUserProfile(onSuccess: (UserSelection?) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                onSuccess(document.toObject(UserSelection::class.java))
            }
            .addOnFailureListener { onSuccess(null) }
    }

    fun getUserDetails(userId: String, onSuccess: (UserSelection) -> Unit) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                document.toObject(UserSelection::class.java)?.let { onSuccess(it) }
            }
    }

    fun checkProfileAndGetName(onResult: (String?) -> Unit) {
        getCurrentUserProfile { user ->
            if (user != null && user.name.isNotBlank()) {
                onResult(user.name)
            } else {
                onResult(null)
            }
        }
    }

    fun checkIfNameExists(name: String, onResult: (Boolean) -> Unit) {
        db.collection("users").whereEqualTo("name", name).limit(1).get()
            .addOnSuccessListener { result -> onResult(!result.isEmpty) }
            .addOnFailureListener { onResult(false) }
    }
}
