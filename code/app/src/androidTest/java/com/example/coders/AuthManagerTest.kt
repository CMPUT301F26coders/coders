package com.example.coders

import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

/** Verifies that the local DataStore session ID can be saved, restored, and cleared. */
@RunWith(AndroidJUnit4::class)
class AuthManagerTest {
    private val context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    private val authManager
        get() = AuthManager(context)

    /** Clears persisted state so each test starts signed out. */
    @Before
    fun clearSavedIdBeforeTest() = runBlocking {
        authManager.clearUserId()
    }

    /** Leaves DataStore signed out so other instrumentation tests start at sign-up. */
    @After
    fun clearSavedIdAfterTest() = runBlocking {
        authManager.clearUserId()
    }

    /** Returns null when the device has no locally saved profile ID. */
    @Test
    fun initiallyHasNoSavedProfileId() = runBlocking {
        assertNull(authManager.getCurrentUserId())
    }

    /** Reads a saved ID from a newly created manager, as the app does after restarting. */
    @Test
    fun savedProfileIdPersistsAcrossManagerInstances() = runBlocking {
        val profileId = "test-profile-id"
        authManager.saveUserId(profileId)

        assertEquals(profileId, AuthManager(context).getCurrentUserId())
    }

    /** Removes the saved ID so the next app launch can show sign-up. */
    @Test
    fun clearingSessionRemovesSavedProfileId() = runBlocking {
        authManager.saveUserId("test-profile-id")

        authManager.clearUserId()

        assertNull(authManager.getCurrentUserId())
    }
}
