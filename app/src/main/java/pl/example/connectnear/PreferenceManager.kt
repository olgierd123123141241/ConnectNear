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

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferenceManager(private val context: Context) {

    companion object {
        val TERMS_ACCEPTED = booleanPreferencesKey("terms_accepted")
        val SAVED_EMAIL = stringPreferencesKey("saved_email")
    }

    suspend fun setTermsAccepted() {
        context.dataStore.edit {
            it[TERMS_ACCEPTED] = true
        }
    }

    val termsAccepted: Flow<Boolean> = context.dataStore.data.map {
        it[TERMS_ACCEPTED] ?: false
    }

    val savedEmail: Flow<String> = context.dataStore.data.map {
        it[SAVED_EMAIL] ?: ""
    }

    suspend fun saveEmail(email: String) {
        context.dataStore.edit {
            it[SAVED_EMAIL] = email
        }
    }
}
