package pl.example.connectnear

import com.google.firebase.Firebase
//import com.google.firebase.ai.vertexAI
import com.google.firebase.vertexai.vertexAI

object AIRepo {
    private val generativeModel = Firebase.vertexAI.generativeModel("gemini-pro")

    fun isAiAvailable(): Boolean {
        // Na razie zwracamy true, docelowo można dodać logikę sprawdzania np. subskrypcji
        return true
    }

    suspend fun generateContent(prompt: String): String {
        return try {
            val response = generativeModel.generateContent(prompt)
            response.text ?: ""
        } catch (e: Exception) {
            // Obsługa błędów, np. logowanie lub zwracanie komunikatu
            e.printStackTrace()
            "Przepraszamy, wystąpił błąd podczas generowania odpowiedzi."
        }
    }
}
