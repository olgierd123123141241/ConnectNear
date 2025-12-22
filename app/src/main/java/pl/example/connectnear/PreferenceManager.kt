package pl.example.connectnear

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PreferenceManager(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
        val SESSION_TOKEN = stringPreferencesKey("session_token")
        val TERMS_ACCEPTED = booleanPreferencesKey("terms_accepted")
        val SAVED_EMAIL = stringPreferencesKey("saved_email") // POPRAWKA: Przywrócono klucz
    }

    val sessionToken: Flow<String?> = context.dataStore.data.map {
        it[SESSION_TOKEN]
    }
    
    val termsAccepted: Flow<Boolean> = context.dataStore.data.map {
        it[TERMS_ACCEPTED] ?: false
    }

    // POPRAWKA: Przywrócono odczyt zapisanego e-maila
    val savedEmail: Flow<String> = context.dataStore.data.map {
        it[SAVED_EMAIL] ?: ""
    }

    suspend fun saveSessionToken(token: String) {
        context.dataStore.edit {
            it[SESSION_TOKEN] = token
        }
    }

    suspend fun clearSessionToken() {
        context.dataStore.edit {
            it.remove(SESSION_TOKEN)
        }
    }
    
    suspend fun setTermsAccepted() {
        context.dataStore.edit {
            it[TERMS_ACCEPTED] = true
        }
    }

    // POPRAWKA: Przywrócono zapisywanie e-maila
    suspend fun saveEmail(email: String) {
        context.dataStore.edit {
            it[SAVED_EMAIL] = email
        }
    }
}
