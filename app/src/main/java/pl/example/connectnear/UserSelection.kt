package pl.example.connectnear

import com.google.firebase.firestore.GeoPoint

// Ostateczna, kompletna i POPRAWNA wersja modelu danych użytkownika
data class UserSelection(
    // Usunięto zbędne, mylące pole "userSelection"
    var userId: String = "",
    var name: String = "",
    var customId: String = "",
    var description: String = "",
    var profileImageUrl: String = "",
    var location: GeoPoint? = null,
    var timestamp: Long = 0L,
    var shareLocation: Boolean = true,
    var category: String = "",
    var visibilityMode: String = "public",
    var locationUpdateInterval: Long = 5000L,
    var searchRadiusKm: Double = 25.0,
    var myAge: String = "",
    var preferredAge: String = "",
    var mySex: String = "",
    var preferredSex: String = "",

    // Sport
    var sportMode: String = "",
    var sportLevel: String = "",
    var isPersonalTrainer: Boolean = false,
    var trainerDescription: String = "",

    // Nauka
    var learningMode: String = "",
    var subject: String = "",
    var learningLevel: String = "",
    var isTutor: Boolean = false,
    var tutorDescription: String = "",
    var isStudyBuddy: Boolean = false,

    // Randka
    var dateMode: String = "",
    var datePartnerGender: String = "",
    var dateCoupleGender: String = "",
    var dateAnimalType: String = "",
    var dateOtherAnimal: String = "",

    // Impreza
    var partyType: String = "",

    // Szczegóły profilu
    var interests: String = "",
    var userStatus: String = "",
    var smoking: String = "",
    var drinking: String = "",
    var personalityType: String = "",
    var isProfilePublic: Boolean = true,

    // Social Media
    var instagramLink: String = "",
    var facebookLink: String = "",
    var tiktokLink: String = "",
    var youtubeLink: String = "",
    var messengerLink: String = "",
    var spotifyLink: String = "",
    var steamLink: String = "",
    var twitterLink: String = "",
    var snapchatLink: String = "",

    // Znajomi z Facebooka
    var facebookFriends: List<String> = emptyList(),

    // Podkategoria
    var subCategory: String = ""
)
