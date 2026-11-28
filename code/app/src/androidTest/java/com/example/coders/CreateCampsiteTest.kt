/**
 * Exercises campsite registration against the connected Firestore project.
 *
 * Outstanding issues: The system photo picker is deliberately not automated; image-record
 * construction is covered by JVM tests and the production picker uses the shared encoder.
 */

package com.example.coders

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.center
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
import java.util.UUID
import java.util.concurrent.TimeUnit

// The following test was modified using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
/** Verifies the campsite registration acceptance criteria through the running application. */
@RunWith(AndroidJUnit4::class)
class CreateCampsiteTest {

    private val composeTestRule = createAndroidComposeRule<MainActivity>()
    private val testEmail = "campsite-registration-${UUID.randomUUID()}@example.com"
    private val firestore = Firebase.firestore
    private val campsiteNames = mutableSetOf<String>()
    private var profileId: String? = null

    @get:Rule
    val testRule: TestRule = freshSessionRule(composeTestRule)

    // The following function was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
    /** Deletes Firestore records created by this test instance and clears the local test session. */
    @After
    fun cleanUpFirebaseTestRecords() {
        campsiteNames.forEach { campsiteName ->
            await(
                firestore.collection("campsites")
                    .whereEqualTo("name", campsiteName)
                    .get()
            ).documents.forEach { campsite ->
                await(
                    firestore.collection("images")
                        .document(campsiteImageId(campsite.id))
                        .delete()
                )
                await(campsite.reference.delete())
            }
        }
        await(
            firestore.collection("profiles")
                .whereEqualTo("email", testEmail)
                .get()
        ).documents.forEach { profile ->
            await(profile.reference.delete())
        }
        profileId?.let { id ->
            await(firestore.collection("profiles").document(id).delete())
        }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        runBlocking { AuthManager(context).clearUserId() }
    }

    // The following function was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
    /** Awaits a Firebase task with the test's bounded timeout. */
    private fun <T> await(task: Task<T>): T = Tasks.await(task, 20, TimeUnit.SECONDS)

    // The following function was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
    /** Waits for the initial sign-up form to be displayed. */
    private fun waitForSignUpScreen() {
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText("New User:").fetchSemanticsNodes().isNotEmpty()
        }
    }

    // The following function was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
    /** Creates and signs in a profile with [role], returning its persisted profile ID. */
    private fun signUpAs(role: UserRole): String {
        waitForSignUpScreen()
        composeTestRule.onNodeWithTag("firstNameInput").performTextInput("Campsite")
        composeTestRule.onNodeWithTag("lastNameInput").performTextInput("Test")
        composeTestRule.onNodeWithTag("emailInput").performTextInput(testEmail)
        composeTestRule.onNodeWithTag("phoneInput").performTextInput("7801234567")
        composeTestRule.onNodeWithText(role.name).performClick()
        composeTestRule.onNodeWithText("Sign Up").performClick()

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithContentDescription("My Stuff")
                .fetchSemanticsNodes().isNotEmpty()
        }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        return runBlocking { AuthManager(context).getCurrentUserId() }
            ?.also { profileId = it }
            ?: error("The sign-up flow did not save a profile ID")
    }

    // The following function was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
    /** Switches the current profile to the campsite-owner view. */
    private fun switchToCampsiteOwnerView() {
        composeTestRule.onNodeWithContentDescription("Profile").performClick()
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText("Switch View").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Switch View").performClick()
        composeTestRule.onNodeWithText("Campsite Owner").performClick()
    }

    /** Opens the owner registration form from the campsite-owner view's My Stuff area. */
    private fun navigateToRegistrationForm() {
        signUpAs(UserRole.NORMAL)
        switchToCampsiteOwnerView()
        composeTestRule.onNodeWithContentDescription("My Stuff").performClick()
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText("My Campsites").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("My Campsites").performClick()
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithTag("registerCampsiteButton")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("registerCampsiteButton").performClick()
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithTag("input_Name").fetchSemanticsNodes().isNotEmpty()
        }
    }

    // The following function was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
    /** Waits until the registration validation dialog has joined the Compose semantics tree. */
    private fun waitForMissingInfoDialog() {
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithTag("missingInfoDialog")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    // The following function was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
    /** Scrolls the long registration form to its confirmation button before clicking it. */
    private fun confirmCampsite() {
        composeTestRule.onNodeWithTag("confirmCampsiteButton")
            .performScrollTo()
            .performClick()
    }

    // The following function was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
    /** Scrolls to the optional tag controls and adds [tag]. */
    private fun addTag(tag: String) {
        composeTestRule.onNodeWithTag("input_Tag")
            .performScrollTo()
            .performTextInput(tag)
        composeTestRule.onNodeWithTag("addTagButton")
            .performScrollTo()
            .performClick()
    }

    // The following function was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
    /** Selects the map position required to submit the registration form. */
    private fun selectMapLocation() {
        composeTestRule.onNodeWithTag("campsiteMapPicker")
            .performScrollTo()
            .performTouchInput { click(center) }
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule.onAllNodesWithText("Selected Location", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    // The following function was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
    /** Fills all text fields that are required before the map-coordinate requirement. */
    private fun fillRequiredTextFields(campsiteName: String) {
        composeTestRule.onNodeWithTag("input_Name").performTextInput(campsiteName)
        composeTestRule.onNodeWithTag("input_Location").performTextInput("Banff, Alberta")
        composeTestRule.onNodeWithTag("input_Description")
            .performTextInput("A lovely site near the creek.")
        composeTestRule.onNodeWithTag("input_Occupancy").performTextInput("4")
    }

    // The following function was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
    /** Covers AC1: a registered campsite owner reaches the information collection screen. */
    @Test
    fun registeredCampOwner_canOpenRegistrationForm() {
        navigateToRegistrationForm()

        composeTestRule.onNodeWithTag("input_Name").assertExists()
        composeTestRule.onNodeWithTag("input_Location").assertExists()
        composeTestRule.onNodeWithTag("input_Description").assertExists()
        composeTestRule.onNodeWithTag("input_Occupancy").assertExists()
        composeTestRule.onNodeWithTag("confirmCampsiteButton")
            .performScrollTo()
            .assertIsDisplayed()
    }

    // The following function was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
    /** Covers unified-profile access: an admin in owner view can open the registration form. */
    @Test
    fun adminProfile_canOpenRegistrationForm() {
        signUpAs(UserRole.ADMIN)
        switchToCampsiteOwnerView()
        composeTestRule.onNodeWithContentDescription("My Stuff").performClick()
        composeTestRule.onNodeWithText("My Campsites").performClick()
        composeTestRule.onNodeWithTag("registerCampsiteButton").performClick()

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithTag("input_Name")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("input_Name").assertIsDisplayed()
        composeTestRule.onNodeWithTag("registrationRejectedDialog").assertDoesNotExist()
    }

    // The following function was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
    /** Covers AC5: empty submission reports every required field. */
    @Test
    fun emptySubmission_listsAllMissingFields() {
        navigateToRegistrationForm()
        confirmCampsite()

        waitForMissingInfoDialog()
        composeTestRule.onNodeWithTag("missingInfoDialog").assertExists()
        CampsiteRequiredField.entries.forEach { field ->
            composeTestRule.onNodeWithText("• ${field.label}").assertExists()
        }
    }

    // The following function was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
    /** Covers AC6: retry closes validation without clearing form input. */
    @Test
    fun missingInformationRetry_keepsEnteredText() {
        navigateToRegistrationForm()
        composeTestRule.onNodeWithTag("input_Name").performTextInput("Kept Name")
        confirmCampsite()
        waitForMissingInfoDialog()
        composeTestRule.onNodeWithTag("missingInfoRetryButton").performClick()

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithTag("missingInfoDialog")
                .fetchSemanticsNodes().isEmpty()
        }
        composeTestRule.onNodeWithTag("input_Name").assertTextContains("Kept Name")
    }

    // The following function was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
    /** Covers AC7: cancel exits validation and returns to the owner's campsite list. */
    @Test
    fun missingInformationCancel_returnsToMyCampsites() {
        navigateToRegistrationForm()
        confirmCampsite()
        waitForMissingInfoDialog()
        composeTestRule.onNodeWithTag("missingInfoCancelButton").performClick()

        composeTestRule.onNodeWithText("My Campsites:").assertIsDisplayed()
    }

    // The following function was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
    /** Covers AC4: valid registration returns to the list and confirms success with a snackbar. */
    @Test
    fun validRegistration_returnsToListWithSuccessMessage() {
        navigateToRegistrationForm()
        val campsiteName = "Bear Creek Site ${UUID.randomUUID().toString().take(8)}"
        campsiteNames += campsiteName
        fillRequiredTextFields(campsiteName)
        addTag("lakefront")
        selectMapLocation()
        confirmCampsite()

        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule.onAllNodesWithText("My Campsites:").fetchSemanticsNodes().isNotEmpty() &&
                composeTestRule.onAllNodesWithText(CAMPSITE_REGISTERED_MESSAGE)
                    .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule.onAllNodesWithText(campsiteName, substring = false)
                .fetchSemanticsNodes().size == 1
        }
        composeTestRule.onNodeWithText(CAMPSITE_REGISTERED_MESSAGE).assertIsDisplayed()
    }

    // The following function was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
    /** Verifies duplicate tags are normalized and an individual tag can be removed. */
    @Test
    fun tags_areDistinctIgnoringCaseAndCanBeRemoved() {
        navigateToRegistrationForm()
        addTag("lakefront")
        addTag(" Lakefront ")

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText("lakefront", substring = false)
                .fetchSemanticsNodes().size == 1
        }
        composeTestRule.onNodeWithTag("removeTag_lakefront")
            .performScrollTo()
            .performClick()
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText("lakefront", substring = false)
                .fetchSemanticsNodes().isEmpty()
        }
    }
}
