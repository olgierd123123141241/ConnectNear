package pl.example.connectnear

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.GeoPoint
import kotlin.math.*

object MapRepo {
    private val db = FirebaseFirestore.getInstance()

    // STARA FUNKCJA findMatches ZOSTAŁA USUNIĘTA. Logika została przeniesiona do NearbyUsersFinder.kt

    fun addFlashEvent(event: FlashEvent, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val newDoc = db.collection("flash_events").document()
        val eventWithId = event.copy(id = newDoc.id)
        newDoc.set(eventWithId)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "") }
    }

    fun getActiveFlashEvents(location: GeoPoint, radiusKm: Double, onResult: (List<FlashEvent>) -> Unit) {
        val currentTime = System.currentTimeMillis()
        db.collection("flash_events")
            .whereGreaterThan("expiresAt", currentTime)
            .get()
            .addOnSuccessListener { documents ->
                val events = mutableListOf<FlashEvent>()
                for (doc in documents) {
                    val loc = doc.getGeoPoint("location") ?: GeoPoint(0.0, 0.0)
                    val viewedByList = doc.get("viewedBy") as? List<String> ?: emptyList()
                    val eventObj = FlashEvent(
                        id = doc.id,
                        creatorId = doc.getString("creatorId") ?: "",
                        location = loc,
                        category = doc.getString("category") ?: "", 
                        description = doc.getString("description") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L,
                        expiresAt = doc.getLong("expiresAt") ?: 0L,
                        imageUrl = doc.getString("imageUrl") ?: "",
                        isOneTime = doc.getBoolean("isOneTime") ?: false,
                        viewedBy = viewedByList
                    )
                    
                    val dist = calculateDistance(location.latitude, location.longitude, loc.latitude, loc.longitude)
                    if (dist <= radiusKm) {
                        events.add(eventObj)
                    }
                }
                onResult(events)
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    fun markFlashEventAsViewed(eventId: String, userId: String) {
        db.collection("flash_events").document(eventId)
            .update("viewedBy", FieldValue.arrayUnion(userId))
            .addOnFailureListener { Log.e("MapRepo", "Error updating viewedBy: ${it.message}") }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2) * sin(dLon / 2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}
