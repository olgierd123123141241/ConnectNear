package pl.example.connectnear

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

object GroupRepo {
    private val db = FirebaseFirestore.getInstance()

    // Tworzenie grupy (teraz z opcją Publiczna/Prywatna)
    fun createGroup(groupName: String, ownerId: String, memberIds: List<String>, isPublic: Boolean, onSuccess: () -> Unit) {
        val groupId = UUID.randomUUID().toString()
        val allMembers = memberIds + ownerId

        val groupData = hashMapOf(
            "groupId" to groupId,
            "name" to groupName,
            "ownerId" to ownerId,
            "memberIds" to allMembers,
            "lastMessage" to "Utworzono grupę",
            "timestamp" to System.currentTimeMillis(),
            "isPublic" to isPublic,
            "pendingRequests" to emptyList<String>()
        )

        db.collection("groups").document(groupId).set(groupData)
            .addOnSuccessListener { onSuccess() }
    }

    // Pobieranie MOICH grup
    fun getMyGroups(myUserId: String, onSuccess: (List<Group>) -> Unit) {
        db.collection("groups")
            .whereArrayContains("memberIds", myUserId)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                val groups = value?.toObjects(Group::class.java) ?: emptyList()
                onSuccess(groups.sortedByDescending { it.timestamp })
            }
    }

    // Pobieranie PUBLICZNYCH grup (których nie jestem członkiem)
    fun getPublicGroups(onSuccess: (List<Group>) -> Unit) {
        db.collection("groups")
            .whereEqualTo("isPublic", true)
            .get()
            .addOnSuccessListener { documents ->
                val groups = documents.toObjects(Group::class.java)
                onSuccess(groups)
            }
    }

    // Wyślij prośbę o dołączenie
    fun requestJoinGroup(groupId: String, myUserId: String, onSuccess: () -> Unit) {
        db.collection("groups").document(groupId)
            .update("pendingRequests", FieldValue.arrayUnion(myUserId))
            .addOnSuccessListener { onSuccess() }
    }

    // Zaakceptuj prośbę (Tylko Lider)
    fun approveRequest(groupId: String, userIdToApprove: String, onSuccess: () -> Unit) {
        db.collection("groups").document(groupId)
            .update(
                "pendingRequests", FieldValue.arrayRemove(userIdToApprove),
                "memberIds", FieldValue.arrayUnion(userIdToApprove)
            )
            .addOnSuccessListener { onSuccess() }
    }

    // Odrzuć prośbę (Tylko Lider)
    fun rejectRequest(groupId: String, userIdToReject: String, onSuccess: () -> Unit) {
        db.collection("groups").document(groupId)
            .update("pendingRequests", FieldValue.arrayRemove(userIdToReject))
            .addOnSuccessListener { onSuccess() }
    }
}
