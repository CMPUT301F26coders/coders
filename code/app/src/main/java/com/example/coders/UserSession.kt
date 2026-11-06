/**
 * Stores the current user's role for use throughout the application.
 *
 * Tracks whether the signed-in user is a campsite owner.
 *
 * Outstanding issues: None known.
 */

package com.example.coders


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Maintains shared session state for the currently signed-in user.
 *
 * @property isOwner Whether the current user has the campsite owner role.
 */
object UserSession {
    var isOwner by mutableStateOf(false)
}
