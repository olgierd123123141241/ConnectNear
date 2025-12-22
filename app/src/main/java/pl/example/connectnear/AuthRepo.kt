package pl.example.connectnear

import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

object AuthRepo {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance() // Potrzebne do usuwania danych
    val currentUser get() = auth.currentUser

    fun signInWithGoogle(idToken: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { 
                // Sprawdzamy, czy to nowy użytkownik
                val isNewUser = it.additionalUserInfo?.isNewUser ?: false
                if (isNewUser) {
                    // TODO: Można tu dodać logikę zapisu początkowych danych dla nowego użytkownika
                }
                onSuccess()
             }
            .addOnFailureListener { onError(it.message ?: "Błąd logowania Google") }
    }

    fun signInWithFacebook(accessToken: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val credential = FacebookAuthProvider.getCredential(accessToken)
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                val isNewUser = it.additionalUserInfo?.isNewUser ?: false
                if (isNewUser) {
                    // TODO: Logika dla nowego użytkownika
                }
                onSuccess()
            }
            .addOnFailureListener { onError(it.message ?: "Błąd logowania przez Facebooka") }
    }

    fun signUp(email: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                // Po sukcesie wysyłamy e-mail weryfikacyjny (opcjonalnie, ale dobra praktyka)
                it.user?.sendEmailVerification()
                    ?.addOnSuccessListener {
                        // WAŻNE: Wylogowujemy użytkownika od razu po rejestracji.
                        // Musi on zalogować się ponownie, aby rozpocząć sesję.
                        auth.signOut()
                        onSuccess()
                    }
                    ?.addOnFailureListener { e ->
                        onError("Konto utworzone, ale błąd wysyłania e-maila: ${e.message}")
                    }
            }
            .addOnFailureListener {
                // Zwracamy czytelny komunikat błędu
                val message = when {
                    "email address is already in use" in (it.message ?: "") -> "Ten adres e-mail jest już zajęty!"
                    "weak password" in (it.message ?: "") -> "Hasło jest zbyt słabe!"
                    else -> it.message ?: "Nieznany błąd rejestracji."
                }
                onError(message)
            }
    }

    fun signIn(email: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                // Po udanym zalogowaniu, logika w MainActivity/AppNavigation pobierze profil
                onSuccess()
            }
            .addOnFailureListener {
                val message = when {
                    "INVALID_LOGIN_CREDENTIALS" in (it.message ?: "") -> "Nieprawidłowy e-mail lub hasło."
                    else -> it.message ?: "Błąd logowania."
                }
                onError(message)
            }
    }

    fun signOut() {
        auth.signOut()
    }

    fun sendPasswordResetEmail(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (email.isBlank()) {
            onError("Wpisz e-mail.")
            return
        }

        // UWAGA: Ta domena musi być dodana do "Authorised domains" w Firebase Console
        // (Authentication -> Settings -> Authorised domains)
        // Musisz również skonfigurować aplikację do obsługi tego deep linku w pliku AndroidManifest.xml.
        val actionCodeSettings = ActionCodeSettings.newBuilder()
            .setUrl("https://connectnear.pl/reset") // Zmień na swoją domenę
            .setHandleCodeInApp(true)
            .setAndroidPackageName(
                "pl.example.connectnear",
                true, /* installIfNotAvailable */
                null  /* minimumVersion */
            )
            .build()

        auth.sendPasswordResetEmail(email, actionCodeSettings)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Błąd wysyłania e-maila.") }
    }

    // --- FUNKCJE ZARZĄDZANIA KONTEM ---

    fun updateUserEmail(newEmail: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = currentUser ?: return
        // Musimy sprawdzić czy user.email nie jest null
        val currentEmail = user.email
        if (currentEmail == null) {
            onError("Błąd: Brak adresu email zalogowanego użytkownika.")
            return
        }
        val credential = EmailAuthProvider.getCredential(currentEmail, pass)

        // Najpierw musimy się ponownie zalogować (re-auth) dla bezpieczeństwa
        user.reauthenticate(credential)
            .addOnSuccessListener {
                // Wysyła e-mail weryfikacyjny na nowy adres. E-mail zmieni się dopiero po kliknięciu w link.
                user.verifyBeforeUpdateEmail(newEmail)
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        val message = when {
                            "EMAIL_EXISTS" in (e.message ?: "") -> "Ten e-mail jest już zajęty."
                            else -> e.message ?: "Błąd zmiany e-maila."
                        }
                        onError(message)
                    }
            }
            .addOnFailureListener { onError("Nieprawidłowe hasło.") }
    }

    fun updatePassword(newPass: String, oldPass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = currentUser ?: return
        val currentEmail = user.email
        if (currentEmail == null) {
            onError("Błąd: Brak adresu email.")
            return
        }
        val credential = EmailAuthProvider.getCredential(currentEmail, oldPass)
        
        user.reauthenticate(credential)
            .addOnSuccessListener {
                user.updatePassword(newPass)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e -> onError(e.message ?: "Błąd zmiany hasła") }
            }
            .addOnFailureListener { onError("Nieprawidłowe obecne hasło.") }
    }

    fun deleteAccount(pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = currentUser ?: return
        val userId = user.uid
        val currentEmail = user.email ?: return
        val credential = EmailAuthProvider.getCredential(currentEmail, pass)

        // Re-autoryzacja dla bezpieczeństwa
        user.reauthenticate(credential)
            .addOnSuccessListener {
                // KROK 1: Usuń dane z Firestore (profil użytkownika)
                db.collection("users").document(userId).delete()
                    .addOnSuccessListener {
                        // KROK 2: Usuń konto z Authentication
                        user.delete()
                            .addOnSuccessListener {
                                onSuccess()
                            }
                            .addOnFailureListener { e -> onError("Błąd usuwania konta: ${e.message}") }
                    }
                    .addOnFailureListener { e -> onError("Błąd usuwania danych: ${e.message}") }
            }
            .addOnFailureListener { onError("Nieprawidłowe hasło.") }
    }
    
    fun clearProfileData(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userId = currentUser?.uid ?: return
        val updates = mapOf<String, Any>(
            "description" to "",
            "profileImageUrl" to "",
            "instagram" to "",
            "facebook" to "",
            "tiktok" to "",
            "messenger" to "",
            "youtube" to "",
            "youtubeStartTime" to 0
            // Nie czyścimy name, category itp. bo to kluczowe dane, chyba że użytkownik chce total reset?
            // "sunuwanie profilu" -> zazwyczaj reset opisów/zdjęć.
        )
        db.collection("users").document(userId).update(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Błąd czyszczenia profilu") }
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid
}
