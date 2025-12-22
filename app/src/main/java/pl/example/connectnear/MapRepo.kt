package pl.example.connectnear

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.GeoPoint
import kotlin.math.*

object MapRepo {
    private val db = FirebaseFirestore.getInstance()

    fun findMatches(mySelection: UserSelection, onResult: (List<FoundUser>) -> Unit) {
        if (mySelection.location == null) { onResult(emptyList()); return }

        val radiusKm = mySelection.searchRadiusKm
        val myId = AuthRepo.getCurrentUserId() ?: ""

        val activeThreshold = 3 * 60 * 1000
        val cutoffTime = System.currentTimeMillis() - activeThreshold

        db.collection("users").document(myId).collection("friends").get()
            .addOnSuccessListener { friendDocs ->
                val myFriendIds = friendDocs.map { it.id }.toSet()

                var query: Query = db.collection("users")
                    .whereGreaterThan("timestamp", cutoffTime)
                    .whereEqualTo("shareLocation", true)

                // GŁÓWNY FILTR KATEGORII
                if (mySelection.category.isNotEmpty()) {
                    query = query.whereEqualTo("category", mySelection.category)
                }

                // FILTRY PODKATEGORII
                if (mySelection.category == "Sport") {
                    if (mySelection.isPersonalTrainer) {
                        query = query.whereEqualTo("sportMode", "Trener personalny")
                    } else if (mySelection.sportMode == "Trener personalny") {
                        query = query.whereEqualTo("isPersonalTrainer", true)
                    } else if (mySelection.sportMode.isNotEmpty()) {
                        query = query.whereEqualTo("sportMode", mySelection.sportMode)
                        if (mySelection.sportLevel.isNotEmpty()) query = query.whereEqualTo("sportLevel", mySelection.sportLevel)
                    }
                    if (mySelection.subCategory.isNotEmpty()) query = query.whereEqualTo("subCategory", mySelection.subCategory)
                
                } else if (mySelection.category == "Nauka") {
                    if (mySelection.isTutor) {
                        query = query.whereEqualTo("learningMode", "Korepetycje")
                    } else if (mySelection.learningMode == "Korepetycje") {
                        query = query.whereEqualTo("isTutor", true)
                    } else if (mySelection.learningMode.isNotEmpty()) {
                        query = query.whereEqualTo("learningMode", mySelection.learningMode)
                        if (mySelection.learningLevel.isNotEmpty()) query = query.whereEqualTo("learningLevel", mySelection.learningLevel)
                        if (mySelection.subject.isNotEmpty()) query = query.whereEqualTo("subject", mySelection.subject)
                    }
                     if ((mySelection.isTutor || mySelection.learningMode == "Korepetycje") && mySelection.subject.isNotEmpty()) {
                         query = query.whereEqualTo("subject", mySelection.subject)
                     }
                } else if (mySelection.category == "Randka") {
                    if (mySelection.dateMode.isNotEmpty()) {
                        query = query.whereEqualTo("dateMode", mySelection.dateMode)
                        when (mySelection.dateMode) {
                            "Pojedyncza" -> {
                                if (mySelection.datePartnerGender.isNotEmpty() && mySelection.datePartnerGender != "Obojętnie") {
                                    query = query.whereEqualTo("mySex", mySelection.datePartnerGender)
                                }
                            }
                            "Para" -> {
                                if (mySelection.dateCoupleGender.isNotEmpty() && mySelection.dateCoupleGender != "Obojętnie") {
                                    query = query.whereEqualTo("mySex", mySelection.dateCoupleGender)
                                }
                            }
                            "Zwierzę" -> {
                                if (mySelection.datePartnerGender.isNotEmpty() && mySelection.datePartnerGender != "Obojętnie") {
                                    query = query.whereEqualTo("mySex", mySelection.datePartnerGender)
                                }
                                if (mySelection.dateAnimalType.isNotEmpty()) {
                                    query = query.whereEqualTo("dateAnimalType", mySelection.dateAnimalType)
                                }
                                 if (mySelection.dateAnimalType == "Inne" && mySelection.dateOtherAnimal.isNotEmpty()) {
                                    query = query.whereEqualTo("dateOtherAnimal", mySelection.dateOtherAnimal)
                                }
                            }
                        }
                    }
                } else if (mySelection.category == "Impreza") {
                    // NOWA LOGIKA DLA IMPREZY
                    if (mySelection.partyType.isNotEmpty()) {
                        query = query.whereEqualTo("partyType", mySelection.partyType)
                    }
                }

                val prefSex = mySelection.preferredSex.trim()
                if (prefSex.isNotEmpty() && !prefSex.equals("Wszyscy", ignoreCase = true) && mySelection.category != "Randka") { // Ignoruj jeśli kategoria to randka, bo tam mamy osobną logikę
                    query = query.whereEqualTo("mySex", prefSex)
                }

                query.get()
                    .addOnSuccessListener { documents ->
                        val foundUsers = mutableListOf<FoundUser>()

                        for (document in documents) {
                            val otherUserId = document.id
                            if (otherUserId == myId) continue

                            val loc = document.getGeoPoint("location")
                            if (loc != null) {
                                val dist = calculateDistance(mySelection.location!!.latitude, mySelection.location!!.longitude, loc.latitude, loc.longitude)

                                if (dist <= radiusKm) {
                                    val otherVisibility = document.getString("visibilityMode") ?: "public"
                                    val showUser = when (otherVisibility) {
                                        "public" -> true
                                        "friends" -> myFriendIds.contains(otherUserId)
                                        else -> true
                                    }

                                    val prefAge = mySelection.preferredAge
                                    val otherAge = document.getString("myAge") ?: ""
                                    val ageMatch = if (prefAge.equals("Bez znaczenia", ignoreCase = true) || prefAge.isEmpty()) {
                                        true
                                    } else {
                                        prefAge == otherAge
                                    }

                                    if (showUser && ageMatch) {
                                        foundUsers.add(FoundUser(
                                            userId = otherUserId,
                                            customId = document.getString("customId") ?: "",
                                            location = loc,
                                            name = document.getString("name") ?: "Nieznajomy",
                                            category = document.getString("category") ?: "",
                                            sex = document.getString("mySex") ?: "",
                                            description = document.getString("description") ?: "",
                                            profileImageUrl = document.getString("profileImageUrl") ?: "",
                                            instagram = document.getString("instagram") ?: "",
                                            facebook = document.getString("facebook") ?: "",
                                            tiktok = document.getString("tiktok") ?: "",
                                            messenger = document.getString("messenger") ?: "",
                                            youtube = document.getString("youtube") ?: "",
                                            youtubeStartTime = document.getLong("youtubeStartTime")?.toInt() ?: 0,
                                            visibilityMode = otherVisibility
                                        ))
                                    }
                                }
                            }
                        }
                        onResult(foundUsers)
                    }
                    .addOnFailureListener { e ->
                        Log.e("MapRepo", "Błąd pobierania: ${e.message}")
                        onResult(emptyList())
                    }
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

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
