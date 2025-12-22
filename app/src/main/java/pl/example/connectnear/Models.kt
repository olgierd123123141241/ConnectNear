package pl.example.connectnear

import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Exclude

// --- MODELE DANYCH ---

data class FoundUser(
    val userId: String,
    val customId: String,
    val location: GeoPoint,
    val name: String,
    val category: String,
    val sex: String,
    val description: String = "",
    val profileImageUrl: String = "",
    // Media
    val instagram: String = "",
    val facebook: String = "",
    val tiktok: String = "",
    val messenger: String = "",
    val youtube: String = "",
    val youtubeStartTime: Int = 0,
    val visibilityMode: String = "public",
    var aiSuggestion: String? = null, // Sugestia od AI
    val facebookId: String = "" // Nowe pole
)

data class FlashEvent(
    val id: String = "",
    val title: String = "", // Nowe pole
    val creatorId: String = "",
    val category: String = "",
    val location: GeoPoint = GeoPoint(0.0, 0.0),
    val description: String = "",
    val timestamp: Long = 0, // Czas utworzenia
    val expiresAt: Long = 0, // Czas wygaśnięcia
    val imageUrl: String = "",
    val isOneTime: Boolean = false,
    val viewedBy: List<String> = emptyList(),
    val participants: List<String> = emptyList(), // Nowe pole - lista ID uczestników
    val chatId: String = "", // Nowe pole - ID czatu grupowego
    val isRecurring: Boolean = false, // Nowe pole
    val recurringDetails: String = "" // Nowe pole - np. "Co piątek o 20:00"
)

data class ChatMessage(
    @get:Exclude var id: String = "", // ID dokumentu (nie zapisywane w bazie)
    val senderId: String = "",
    val text: String = "",
    val imageUrl: String = "",
    val fileUrl: String = "",
    val fileName: String = "",
    // Wiadomości głosowe
    val voiceUrl: String = "", // URL nagrania
    val voiceDuration: Long = 0, // Czas trwania w ms
    val timestamp: Long = 0,
    // Status
    val read: Boolean = false, // Czy wiadomość została przeczytana?
    val isPinned: Boolean = false, // Czy wiadomość jest przypięta?
    
    // --- ODPOWIEDZI ---
    val replyToId: String = "",   // ID wiadomości, na którą odpowiadamy
    val replyToText: String = "", // Treść cytowanej wiadomości (lub "[Zdjęcie]", "[Plik]")
    val replyToSenderName: String = "", // Nazwa autora cytowanej wiadomości

    // --- ZNIKAJĄCE ZDJĘCIA (EPHEMERAL) ---
    val isEphemeral: Boolean = false, 
    val ephemeralType: String = "standard", // "standard", "one_time", "keep" (zabezpieczone, ale nie znika)
    val viewedBy: List<String> = emptyList(), // Lista ID użytkowników, którzy wyświetlili zdjęcie

    // --- Pola Gry Kółko i Krzyżyk ---
    val isGame: Boolean = false,
    val gameBoard: List<String> = emptyList(),
    val gameTurn: String = "", // User ID of the player whose turn it is
    val gameWinner: String = "", // User ID of the winner, or "Remis"
    val playerX: String = "", // User ID of player X
    val playerO: String = "", // User ID of player O
    val isGameOver: Boolean = false
)

data class Group(
    val groupId: String = "",
    val name: String = "",
    val ownerId: String = "",
    val memberIds: List<String> = emptyList(),
    val lastMessage: String = "",
    val timestamp: Long = 0,

    // --- NOWE POLA ---
    val isPublic: Boolean = false, // Czy grupa jest publiczna?
    val pendingRequests: List<String> = emptyList() // Lista ID osób czekających na akceptację
)
