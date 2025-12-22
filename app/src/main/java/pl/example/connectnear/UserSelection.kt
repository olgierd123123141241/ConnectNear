package pl.example.connectnear

import com.google.firebase.firestore.GeoPoint

data class UserSelection(
    var userId: String = "",
    var customId: String = "",
    var name: String = "",
    var category: String = "",

    var subCategory: String = "",
    var interests: String = "",

    // Pola dla Sportu
    var sportMode: String = "",
    var sportLevel: String = "",
    var trainerDescription: String = "",

    // Pola dla Nauki
    var learningMode: String = "",
    var subject: String = "",
    var learningLevel: String = "",
    var tutorDescription: String = "",
    
    // Pola dla Randki
    var dateMode: String = "",
    var datePartnerGender: String = "",
    var dateCoupleGender: String = "",
    var dateAnimalType: String = "",
    var dateOtherAnimal: String = "",
    
    // Pola dla Imprezy
    var partyType: String = "",

    // Pole dla Osobowości
    var personalityType: String = "",

    // Nowe pola: używki
    var smoking: String = "",
    var drinking: String = "",

    // Flagi usług
    var isPersonalTrainer: Boolean = false,
    var isTutor: Boolean = false,
    var isStudyBuddy: Boolean = false,

    // Status użytkownika
    var userStatus: String = "",
    var vibe: String = "", // Nowe pole: "Vibe Check" / Nastrój

    // Preferencje
    var myAge: String = "",
    var preferredAge: String = "",
    var mySex: String = "",
    var preferredSex: String = "",

    // Dane lokalizacji i profilu
    var location: GeoPoint? = null,
    var profileImageUrl: String = "",
    var description: String = "",
    var locationUpdateInterval: Long = 60000L,

    // Social Media & Integracje
    var instagramLink: String = "",
    var facebookLink: String = "",
    var tiktokLink: String = "",
    var messengerLink: String = "",
    var youtubeLink: String = "",
    var twitterLink: String = "",
    var snapchatLink: String = "",
    var spotifyLink: String = "",
    var steamLink: String = "",
    var filmwebLink: String = "", // Nowe pole: Integracja z Filmweb
    var letterboxdLink: String = "", // Nowe pole: Integracja z Letterboxd
    
    // Tagi
    var tags: List<String> = emptyList(), // Nowe pole: Tagi zainteresowań

    // Znajomi z Facebooka
    var facebookFriends: List<String> = emptyList(),
    var facebookId: String = "",

    // Ustawienia
    var visibilityMode: String = "public",
    var searchRadiusKm: Double = 50.0,
    var shareLocation: Boolean = true,

    var timestamp: Long = 0L // DODANO BRAKUJĄCE POLE
) {
    fun updateWith(other: UserSelection) {
        this.userId = other.userId
        this.customId = other.customId
        this.name = other.name
        this.category = other.category
        this.subCategory = other.subCategory
        this.interests = other.interests
        this.sportMode = other.sportMode
        this.sportLevel = other.sportLevel
        this.trainerDescription = other.trainerDescription
        this.learningMode = other.learningMode
        this.subject = other.subject
        this.learningLevel = other.learningLevel
        this.tutorDescription = other.tutorDescription
        this.isPersonalTrainer = other.isPersonalTrainer
        this.isTutor = other.isTutor
        this.isStudyBuddy = other.isStudyBuddy
        this.userStatus = other.userStatus
        this.vibe = other.vibe
        this.myAge = other.myAge
        this.preferredAge = other.preferredAge
        this.mySex = other.mySex
        this.preferredSex = other.preferredSex
        this.location = other.location
        this.profileImageUrl = other.profileImageUrl
        this.description = other.description
        this.locationUpdateInterval = other.locationUpdateInterval
        this.instagramLink = other.instagramLink
        this.facebookLink = other.facebookLink
        this.tiktokLink = other.tiktokLink
        this.messengerLink = other.messengerLink
        this.youtubeLink = other.youtubeLink
        this.twitterLink = other.twitterLink
        this.snapchatLink = other.snapchatLink
        this.spotifyLink = other.spotifyLink
        this.steamLink = other.steamLink
        this.filmwebLink = other.filmwebLink
        this.letterboxdLink = other.letterboxdLink
        this.tags = other.tags
        this.facebookFriends = other.facebookFriends
        this.facebookId = other.facebookId
        this.visibilityMode = other.visibilityMode
        this.searchRadiusKm = other.searchRadiusKm
        this.shareLocation = other.shareLocation
        
        this.dateMode = other.dateMode
        this.datePartnerGender = other.datePartnerGender
        this.dateCoupleGender = other.dateCoupleGender
        this.dateAnimalType = other.dateAnimalType
        this.dateOtherAnimal = other.dateOtherAnimal

        this.partyType = other.partyType
        this.personalityType = other.personalityType
        
        this.smoking = other.smoking
        this.drinking = other.drinking

        this.timestamp = other.timestamp // DODANO AKTUALIZACJĘ
    }
}
