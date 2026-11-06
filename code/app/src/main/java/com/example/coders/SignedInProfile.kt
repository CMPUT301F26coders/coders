/**
 * Provides access to the currently signed-in user's profile.
 *
 * Uses AuthManager to retrieve the locally saved profile ID and ProfileRepo
 * to load the corresponding profile from Firestore.
 *
 * Outstanding issues: None known.
 */

package com.example.coders

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Loads and remembers the profile associated with the locally signed-in user.
 */
class SignedInProfile {

    /**
     * Reads the saved profile ID, loads its profile document, and exposes the result as Compose state.
     * The returned state is `null` while loading or when no profile could be loaded.
     *
     * @return Compose state containing the loaded profile, or `null` until one is available.
     */
    @Composable
    fun rememberSignedInProfile(): State<Profile?> {
        val context = LocalContext.current
        val profileState = remember { mutableStateOf<Profile?>(null) }

        LaunchedEffect(Unit) {
            val profileId = AuthManager(context).getCurrentUserId()
                ?: return@LaunchedEffect

            ProfileRepo().getProfileById(profileId) { profile, error ->
                if (error == null) {
                    profileState.value = profile
                }
            }
        }

        return profileState
    }
}
