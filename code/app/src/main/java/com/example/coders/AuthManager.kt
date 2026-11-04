package com.example.coders

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.io.IOException

/** The app-wide Preferences DataStore used for the local sign-in session. */
private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

/**
 * Reads and updates the locally saved profile ID for this device.
 *
 * @param context The app context.
 * @property USER_ID_KEY The key used to save and retrieve the profile ID.
 * @property userIdFlow A flow of the profile ID.
 * */
class AuthManager(private val context: Context) {

    companion object {
        private val USER_ID_KEY = stringPreferencesKey("user_id")
    }

    val userIdFlow: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[USER_ID_KEY]
        }

    /**
     * Saves a profile ID after its Firestore profile has been created successfully.
     *
     * @param userId The profile ID.
     * */
    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
        }
    }

    /**
     * Removes the locally saved profile ID.
     */
    suspend fun clearUserId() {
        context.dataStore.edit { preferences ->
            preferences.remove(USER_ID_KEY)
        }
    }

    /**
     * Returns the current saved profile ID, or `null` if none is stored.
     *
     * @return The profile ID, or `null`.
     */
    suspend fun getCurrentUserId(): String? {
        return userIdFlow.firstOrNull()
    }
}
