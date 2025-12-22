const functions = require("firebase-functions");
const { GoogleGenerativeAI } = require("@google/generative-ai");

// UWAGA: Inicjalizację robimy wewnątrz funkcji, 
// aby uniknąć błędu "missing API key" podczas wdrażania (deploy).

exports.getAiSuggestion = functions
    .region("europe-west3") 
    .https.onCall(async (data, context) => {

    // 0. Sprawdź czy klucz API jest dostępny
    if (!process.env.GEMINI_API_KEY) {
        console.error("Błąd: Brak GEMINI_API_KEY w zmiennych środowiskowych.");
        throw new functions.https.HttpsError(
            "internal",
            "Serwer nie jest poprawnie skonfigurowany (brak klucza API)."
        );
    }

    // Inicjalizacja Gemini przy każdym wywołaniu
    const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY);
    // Używamy modelu gemini-1.5-flash (szybki i tani/darmowy)
    const model = genAI.getGenerativeModel({ model: "gemini-1.5-flash" });

    // 1. Sprawdź czy użytkownik jest zalogowany
    if (!context.auth) {
        throw new functions.https.HttpsError(
            "unauthenticated",
            "Musisz być zalogowany."
        );
    }

    const prompt = data.prompt;
    if (!prompt) {
         throw new functions.https.HttpsError(
            "invalid-argument",
            "Brak promptu."
        );
    }

    try {
        // Generowanie treści
        const result = await model.generateContent(prompt);
        const response = await result.response;
        const suggestion = response.text();
        
        return { suggestion: suggestion };

    } catch (error) {
        console.error("Błąd Gemini:", error);
        throw new functions.https.HttpsError(
            "internal",
            "Błąd serwera AI: " + error.message
        );
    }
});
