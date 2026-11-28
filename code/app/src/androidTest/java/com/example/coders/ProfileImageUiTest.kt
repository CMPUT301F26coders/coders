/**
 * Exercises profile-image display and removal against the connected Firestore project.
 *
 * Outstanding issues: The system photo picker is intentionally not automated; selection and
 * encoding are covered by the production launcher and JVM helper tests.
 */

package com.example.coders

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream
import java.util.UUID
import java.util.concurrent.TimeUnit

// The following test was added using OpenAI, Codex, "Implement US 01.08.02: add, update,
// and remove profile images stored as Firestore data URLs", 2026-11-22.
/** Verifies profile images render and can be removed through the profile screen. */
@RunWith(AndroidJUnit4::class)
class ProfileImageUiTest {

    private val composeTestRule = createAndroidComposeRule<MainActivity>()
    private val testEmail = "profile-image-${UUID.randomUUID()}@example.com"
    private val firestore = Firebase.firestore
    private var profileId: String? = null

    @get:Rule
    val testRule: TestRule = freshSessionRule(composeTestRule)

    // The following function was added using OpenAI, Codex, "Implement US 01.08.02: add, update,
    // and remove profile images stored as Firestore data URLs", 2026-11-22.
    /** Removes every Firestore profile and profile-image document created by this test instance. */
    @After
    fun cleanUpFirebaseTestRecords() {
        val profileDocuments = await(
            firestore.collection("profiles").whereEqualTo("email", testEmail).get()
        ).documents
        profileDocuments.forEach { document ->
            await(
                firestore.collection("images")
                    .document(ProfileImageRepo.profileImageId(document.id))
                    .delete()
            )
            await(document.reference.delete())
        }
        profileId?.let { id ->
            await(firestore.collection("images").document(ProfileImageRepo.profileImageId(id)).delete())
        }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        runBlocking { AuthManager(context).clearUserId() }
    }

    // The following function was added using OpenAI, Codex, "Implement US 01.08.02: add, update,
    // and remove profile images stored as Firestore data URLs", 2026-11-22.
    /**
     * Waits for a Firebase task or fails after the test timeout.
     *
     * @param task The Firebase task to await.
     * @return The completed task result.
     */
    private fun <T> await(task: Task<T>): T = Tasks.await(task, 20, TimeUnit.SECONDS)

    // The following function was added using OpenAI, Codex, "Implement US 01.08.02: add, update,
    // and remove profile images stored as Firestore data URLs", 2026-11-22.
    /**
     * Creates and signs in a new normal profile, stopping on the Home screen.
     *
     * @return The newly created profile's ID.
     */
    private fun signUpEntrant(): String {
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithTag("firstNameInput")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("firstNameInput").performTextInput("Profile")
        composeTestRule.onNodeWithTag("lastNameInput").performTextInput("Image")
        composeTestRule.onNodeWithTag("emailInput").performTextInput(testEmail)
        composeTestRule.onNodeWithTag("phoneInput").performTextInput("7801234567")
        composeTestRule.onNodeWithText(UserRole.NORMAL.name).performClick()
        composeTestRule.onNodeWithText("Sign Up").performClick()

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText("Home")
                .fetchSemanticsNodes().isNotEmpty()
        }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        return runBlocking { AuthManager(context).getCurrentUserId() }
            ?.also { profileId = it }
            ?: error("The sign-up flow did not save the entrant profile ID")
    }

    // The following function was added using OpenAI, Codex, "Implement US 01.08.02: add, update,
    // and remove profile images stored as Firestore data URLs", 2026-11-22.
    /** Opens the signed-in entrant's profile screen. */
    private fun openProfile() {
        composeTestRule.onNodeWithContentDescription("Profile").performClick()
    }

    // The following function was added using OpenAI, Codex, "Implement US 01.08.02: add, update,
    // and remove profile images stored as Firestore data URLs", 2026-11-22.
    /** Verifies a profile with no image document renders the shared default avatar and edit control. */
    @Test
    fun entrantWithoutProfileImage_showsDefaultAvatarAndEditControl() {
        signUpEntrant()
        openProfile()

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithTag("profileAvatarDefault")
                .fetchSemanticsNodes().isNotEmpty() &&
                composeTestRule.onAllNodesWithTag("editProfileImageButton")
                    .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("profileAvatarDefault").assertExists()
        composeTestRule.onNodeWithTag("editProfileImageButton").assertExists()
    }

    // The following function was added using OpenAI, Codex, "Implement US 01.08.02: add, update,
    // and remove profile images stored as Firestore data URLs", 2026-11-22.
    /** Verifies removing a seeded image restores the default avatar and deletes its Firestore doc. */
    @Test
    fun removingProfileImage_restoresDefaultAvatarAndDeletesDocument() {
        val entrantId = signUpEntrant()
        val imageDocument = firestore.collection("images")
            .document(ProfileImageRepo.profileImageId(entrantId))
        await(
            imageDocument.set(
                ProfileImageRepo.profileImageRecord(entrantId, tinyJpegDataUrl())
            )
        )
        openProfile()

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithTag("profileAvatarImage")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("editProfileImageButton").performClick()
        composeTestRule.onNodeWithTag("removeProfileImageOption").performClick()

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithTag("profileAvatarDefault")
                .fetchSemanticsNodes().isNotEmpty() && !await(imageDocument.get()).exists()
        }

        composeTestRule.onNodeWithTag("profileAvatarDefault").assertExists()
        check(!await(imageDocument.get()).exists()) { "Profile image document was not removed." }
    }

    // The following function was added using OpenAI, Codex, "Implement US 01.08.02: add, update,
    // and remove profile images stored as Firestore data URLs", 2026-11-22.
    /**
     * Creates a tiny valid JPEG data URL for the Firestore-backed UI test.
     *
     * @return A decodable one-pixel JPEG data URL.
     */
    private fun tinyJpegDataUrl(): String {
        val bytes = ByteArrayOutputStream()
        val bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        try {
            bitmap.eraseColor(Color.CYAN)
            check(bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bytes)) {
                "Unable to encode the test JPEG."
            }
        } finally {
            bitmap.recycle()
        }
        return jpegBytesToDataUrl(bytes.toByteArray())
    }
}
