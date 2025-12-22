package pl.example.connectnear

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.GeoPoint
import kotlin.math.*

/**
 * Ostateczna, przejrzysta wersja logiki wyszukiwania.
 * Pobiera szerszą grupę użytkowników i filtruje ich po stronie klienta,
 * z dokładnym logowaniem każdego kroku w panelu Logcat (szukaj tagu "FilterAudit").
 */
object NearbyUsersFinder {

    private val db = FirebaseFirestore.getInstance()

    fun find(mySelection: UserSelection, onResult: (List<FoundUser>) -> Unit) {
        val myId = AuthRepo.getCurrentUserId()
        if (myId == null || mySelection.location == null) {
            onResult(emptyList())
            return
        }

        Log.d("FilterAudit", "--- Rozpoczynam wyszukiwanie (WERSJA Z LOGOWANIEM) ---")
        Log.d("FilterAudit", "Filtry: Kat: '${mySelection.category}', Wiek: '${mySelection.preferredAge}', Płeć: '${mySelection.preferredSex}'")

        val radiusKm = mySelection.searchRadiusKm
        val activeThreshold = 24 * 60 * 60 * 1000L
        val cutoffTime = System.currentTimeMillis() - activeThreshold

        // 1. Proste zapytanie do Firebase
        db.collection("users")
            .whereEqualTo("shareLocation", true)
            .whereGreaterThan("timestamp", cutoffTime)
            .get()
            .addOnSuccessListener { documents ->
                Log.d("FilterAudit", "Zapytanie do Firebase zwróciło ${documents.size()} potencjalnych użytkowników.")
                
                // 2. BEZPIECZNE, RĘCZNE MAPOWANIE DANYCH
                val allUsers = documents.mapNotNull { document ->
                    try {
                        FoundUser(
                            userId = document.id,
                            customId = document.getString("customId") ?: "",
                            name = document.getString("name") ?: "Nieznajomy",
                            category = document.getString("category") ?: "",
                            myAge = document.getString("myAge") ?: "",
                            sex = document.getString("mySex") ?: "", // Poprawne mapowanie z bazy danych
                            description = document.getString("description") ?: "",
                            profileImageUrl = document.getString("profileImageUrl") ?: "",
                            location = document.getGeoPoint("location"), // Może być null, sprawdzamy to niżej
                            visibilityMode = document.getString("visibilityMode") ?: "public"
                        )
                    } catch (e: Exception) {
                        Log.e("FilterAudit", "Błąd mapowania dokumentu ${document.id}", e)
                        null // Pomiń dokument, jeśli wystąpi błąd
                    }
                }

                // 3. Filtrowanie po stronie klienta z DOKŁADNYM logowaniem
                val filteredUsers = allUsers.filter { user ->
                    if (user.userId == myId) return@filter false // Zawsze pomijamy siebie

                    // Filtr #1: Kategoria
                    if (mySelection.category.isNotEmpty() && user.category != mySelection.category) {
                        Log.d("FilterAudit", "ODRZUCAM '${user.name}': Zła kategoria (Szukam: '${mySelection.category}', Jest: '${user.category}')")
                        return@filter false
                    }

                    // Filtr #2: Wiek
                    if (mySelection.preferredAge.isNotEmpty() && mySelection.preferredAge != "Bez znaczenia" && user.myAge != mySelection.preferredAge) {
                        Log.d("FilterAudit", "ODRZUCAM '${user.name}': Zły wiek (Szukam: '${mySelection.preferredAge}', Jest: '${user.myAge}')")
                        return@filter false
                    }

                    // Filtr #3: Płeć (POPRAWIONY)
                    if (mySelection.preferredSex.isNotEmpty() && mySelection.preferredSex != "Wszyscy" && user.sex != mySelection.preferredSex) {
                        Log.d("FilterAudit", "ODRZUCAM '${user.name}': Zła płeć (Szukam: '${mySelection.preferredSex}', Jest: '${user.sex}')")
                        return@filter false
                    }

                    // Filtr #4: Dystans
                    val isInRadius = user.location?.let {
                        calculateDistance(mySelection.location!!.latitude, mySelection.location!!.longitude, it.latitude, it.longitude) <= radiusKm
                    } ?: false
                    if (!isInRadius) {
                        Log.d("FilterAudit", "ODRZUCAM '${user.name}': Poza zasięgiem")
                        return@filter false
                    }

                    // Jeśli użytkownik przeszedł wszystkie filtry
                    Log.d("FilterAudit", "AKCEPTUJĘ: '${user.name}' spełnia wszystkie kryteria.")
                    true
                }

                Log.d("FilterAudit", "--- Zakończono. Zwracam ${filteredUsers.size} użytkowników. ---")
                onResult(filteredUsers)
            }
            .addOnFailureListener { e ->
                Log.e("FilterAudit", "Błąd zapytania do Firebase: ${e.message}")
                onResult(emptyList())
            }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2) * sin(dLon / 2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}
