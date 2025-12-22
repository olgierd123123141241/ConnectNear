package pl.example.connectnear

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

object IndexFixer {

    // Ta funkcja służy TYLKO do wygenerowania linku w Logcat
    fun wymusLinkDoIndeksu() {
        val db = FirebaseFirestore.getInstance()

        Log.e("IndexFixer", ">>> ROZPOCZYNAM WYMUSZANIE BŁĘDU INDEKSU... <<<")

        // 1. Symulujemy najtrudniejsze zapytanie (Czas + Lokalizacja + Kategoria)
        db.collection("users")
            .whereEqualTo("shareLocation", true)
            .whereEqualTo("category", "Sport") // Przykładowa kategoria
            .whereGreaterThan("timestamp", 1000L) // Przykładowy czas
            .get()
            .addOnSuccessListener {
                Log.e("IndexFixer", "⚠️ O dziwo zadziałało bez błędu! Spróbuj zmienić kategorię w kodzie.")
            }
            .addOnFailureListener { e ->
                Log.e("IndexFixer", "!!! OTO TWÓJ LINK DO KLIKNIĘCIA PONIŻEJ !!!")
                Log.e("IndexFixer", e.message ?: "Brak komunikatu")
                Log.e("IndexFixer", "------------------------------------------------")
            }
    }
}
