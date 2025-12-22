package pl.example.connectnear

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

object ChatRepo {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun getChatId(user1: String, user2: String): String = if (user1 < user2) "${user1}_$user2" else "${user2}_$user1"

    fun sendMessage(chatId: String, message: ChatMessage, receiverId: String, senderName: String, isGroup: Boolean = false) {
        db.collection("chats").document(chatId).collection("messages").add(message)

        // Reset typing status on send
        setTypingStatus(chatId, message.senderId, false)

        if (isGroup) {
            db.collection("groups").document(chatId).update(
                "lastMessage", "${senderName}: ${if(message.text.isNotEmpty()) message.text else "Plik"}",
                "timestamp", System.currentTimeMillis()
            )

            db.collection("groups").document(chatId).get().addOnSuccessListener { doc ->
                val members = doc.get("memberIds") as? List<String> ?: emptyList()
                val notif = hashMapOf(
                    "type" to "message",
                    "senderName" to (if(isGroup) "Grupa" else senderName),
                    "text" to "$senderName: ${if(message.text.isNotEmpty()) message.text else "Wysłano plik"}",
                    "read" to false,
                    "timestamp" to System.currentTimeMillis()
                )

                members.forEach { memberId ->
                    if (memberId != message.senderId) { 
                        db.collection("users").document(memberId).collection("notifications").add(notif)
                    }
                }
            }
        } else {
            val notif = hashMapOf(
                "type" to "message",
                "senderName" to senderName,
                "senderId" to message.senderId,
                "text" to (if(message.text.isNotEmpty()) message.text else "Plik"),
                "read" to false,
                "timestamp" to System.currentTimeMillis()
            )
            db.collection("users").document(receiverId).collection("notifications").add(notif)
        }
    }

    // BEZPIECZNA WERSJA
    fun getMessages(chatId: String, onUpdate: (List<ChatMessage>) -> Unit) {
        db.collection("chats").document(chatId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { v, e ->
                if (e != null) {
                    Log.e("ChatRepo", "Listen failed.", e)
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }
                
                val list = v?.documents?.mapNotNull { doc ->
                    try {
                        ChatMessage(
                            id = doc.id,
                            senderId = doc.getString("senderId") ?: "",
                            text = doc.getString("text") ?: "",
                            imageUrl = doc.getString("imageUrl") ?: "",
                            fileUrl = doc.getString("fileUrl") ?: "",
                            fileName = doc.getString("fileName") ?: "",
                            voiceUrl = doc.getString("voiceUrl") ?: "",
                            voiceDuration = doc.getLong("voiceDuration") ?: 0L,
                            timestamp = doc.getLong("timestamp") ?: 0L,
                            read = doc.getBoolean("read") ?: false,
                            isPinned = doc.getBoolean("isPinned") ?: false,
                            replyToId = doc.getString("replyToId") ?: "",
                            replyToText = doc.getString("replyToText") ?: "",
                            replyToSenderName = doc.getString("replyToSenderName") ?: "",
                            isEphemeral = doc.getBoolean("isEphemeral") ?: false,
                            ephemeralType = doc.getString("ephemeralType") ?: "standard",
                            viewedBy = doc.get("viewedBy") as? List<String> ?: emptyList(),
                            isGame = doc.getBoolean("isGame") ?: false,
                            gameBoard = doc.get("gameBoard") as? List<String> ?: emptyList(),
                            gameTurn = doc.getString("gameTurn") ?: "",
                            gameWinner = doc.getString("gameWinner") ?: "",
                            playerX = doc.getString("playerX") ?: "",
                            playerO = doc.getString("playerO") ?: "",
                            isGameOver = doc.getBoolean("isGameOver") ?: false
                        )
                    } catch (ex: Exception) {
                        Log.e("ChatRepo", "Error parsing message", ex)
                        null
                    }
                } ?: emptyList()
                onUpdate(list)
            }
    }

    fun deleteMessage(chatId: String, messageId: String) {
        db.collection("chats").document(chatId).collection("messages").document(messageId).delete()
    }

    fun markMessagesAsRead(chatId: String, myUserId: String) {
        db.collection("chats").document(chatId).collection("messages")
            .whereEqualTo("read", false)
            .get()
            .addOnSuccessListener { snapshot ->
                for (doc in snapshot.documents) {
                    if (doc.getString("senderId") != myUserId) {
                        doc.reference.update("read", true)
                    }
                }
            }
    }

    fun markMessageAsViewed(chatId: String, messageId: String, userId: String) {
        if (messageId.isEmpty()) return
        db.collection("chats").document(chatId).collection("messages").document(messageId)
            .update("viewedBy", FieldValue.arrayUnion(userId))
            .addOnFailureListener { ex -> Log.e("ChatRepo", "Error updating viewedBy", ex) }
    }

    fun pinMessage(chatId: String, messageId: String, isPinned: Boolean) {
        if (messageId.isNotEmpty()) {
            db.collection("chats").document(chatId).collection("messages").document(messageId)
                .update("isPinned", isPinned)
        }
    }

    // --- TYPING STATUS ---
    fun setTypingStatus(chatId: String, userId: String, isTyping: Boolean) {
        val data = mapOf(
            "isTyping" to isTyping,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("chats").document(chatId).collection("typing_status")
            .document(userId)
            .set(data, SetOptions.merge())
    }

    fun listenForTypingStatus(chatId: String, myUserId: String, onTypingChange: (List<String>) -> Unit): ListenerRegistration {
        return db.collection("chats").document(chatId).collection("typing_status")
            .whereEqualTo("isTyping", true)
            .addSnapshotListener { snapshots, _ ->
                if (snapshots != null) {
                    val typingUsers = snapshots.documents.mapNotNull { doc ->
                        if (doc.id != myUserId) doc.id else null
                    }
                    onTypingChange(typingUsers)
                } else {
                    onTypingChange(emptyList())
                }
            }
    }

    fun uploadFileToStorage(uri: Uri, folder: String, userId: String, fileName: String? = null, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val finalFileName = fileName ?: UUID.randomUUID().toString()
        val ref = storage.reference.child("$folder/$userId/$finalFileName")
        ref.putFile(uri).continueWithTask { task ->
            if (!task.isSuccessful) task.exception?.let { throw it }
            ref.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) onSuccess(task.result.toString()) else onError(task.exception?.message ?: "")
        }
    }

    fun deleteFileFromStorage(fileUrl: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (fileUrl.isBlank()) {
            onSuccess()
            return
        }
        val ref = storage.getReferenceFromUrl(fileUrl)
        ref.delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Błąd usuwania pliku") }
    }

    fun listenForNotifications(myUserId: String, onNewNotification: (Map<String, Any>) -> Unit): ListenerRegistration {
        return db.collection("users").document(myUserId).collection("notifications")
            .whereEqualTo("read", false)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                if (snapshots != null) {
                    for (dc in snapshots.documentChanges) {
                        if (dc.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                            val data = dc.document.data.toMutableMap()
                            data["id"] = dc.document.id
                            onNewNotification(data)
                            
                            db.collection("users").document(myUserId).collection("notifications")
                                .document(dc.document.id)
                                .update("read", true)
                                .addOnFailureListener { 
                                    // Ignorujemy błąd
                                }
                        }
                    }
                }
            }
    }
}
