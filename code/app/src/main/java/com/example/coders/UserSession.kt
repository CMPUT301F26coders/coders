/**
 * Stores the selected interface for the signed-in user.
 *
 * Allows normal users to switch between entrant and campsite
 * owner views. Administrators can also access the admin view.
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
 * Represents the permission level of the signed-in user.
 */
enum class UserRole {
    NORMAL,
    ADMIN
}

/**
 * Maintains the currently selected interface and account role.
 *
 * @property viewMode The interface currently displayed.
 * @property userRole The permission level of the signed-in user.
 */
object UserSession {

    var viewMode by mutableStateOf(ViewMode.ENTRANT)
        private set

    var userRole by mutableStateOf(UserRole.NORMAL)
        private set

    private var currentUserId: String? = null

    /**
     * Changes the selected interface.
     * Only administrators can access the admin view.
     *
     * @param newMode The interface to switch to.
     */
    fun switchView(newMode: ViewMode) {
        if (newMode == ViewMode.ADMIN && userRole != UserRole.ADMIN) {
            return
        }

        viewMode = newMode
    }

    /**
     * Initializes the interface and role based on the
     * signed-in user's profile.
     *
     * @param profile The signed-in user's profile.
     */
    fun initializeView(profile: Profile) {
        if (currentUserId == profile.id) return

        currentUserId = profile.id

        userRole = if (profile.type == "Admin") {
            UserRole.ADMIN
        } else {
            UserRole.NORMAL
        }

        viewMode = when (userRole) {
            UserRole.ADMIN -> ViewMode.ADMIN
            UserRole.NORMAL -> ViewMode.ENTRANT
        }
    }

    /**
     * Clears data from the current session.
     */
    fun clearSession() {
        currentUserId = null
        viewMode = ViewMode.ENTRANT
        userRole = UserRole.NORMAL
    }
}