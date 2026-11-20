/**
 * Stores the selected interface for the signed-in user.
 *
 * Allows administrators to switch between entrant,
 * campsite owner, and admin views.
 *
 * Outstanding issues: None known.
 */

package com.example.coders

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Represents the interface currently displayed to the user.
 */
enum class ViewMode {
    ENTRANT,
    CAMP_OWNER,
    ADMIN
}

/**
 * Maintains the currently selected interface.
 *
 * @property viewMode The interface currently displayed to the user.
 */
object UserSession {

    var viewMode by mutableStateOf(ViewMode.ENTRANT)
        private set


    private var currentUserId: String? = null
    /**
     * Changes the selected interface if the user is an administrator.
     *
     * @param profile The signed-in user's profile.
     * @param newMode The interface to switch to.
     */
    fun switchView(profile: Profile, newMode: ViewMode) {
        if (profile is Admin) {
            viewMode = newMode
        }
    }

    /**
     * Initializes the interface based on the signed-in user's profile.
     *
     * @param profile The signed-in user's profile.
     */
    fun initializeView(profile: Profile) {
        if(currentUserId == profile.id) return

        currentUserId = profile.id
        viewMode = when (profile) {
            is Entrant -> ViewMode.ENTRANT
            is CampOwner -> ViewMode.CAMP_OWNER
            is Admin -> ViewMode.ADMIN
            else -> ViewMode.ENTRANT
        }
    }

    /**
     * Clears data from the current session
     */
    fun clearSession() {
        currentUserId = null
        viewMode = ViewMode.ENTRANT
    }
}