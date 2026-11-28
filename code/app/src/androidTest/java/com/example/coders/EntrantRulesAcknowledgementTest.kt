/** Exercises US 02.11.01: acknowledging campsite rules before confirming a reservation. */
package com.example.coders

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
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
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

// The following test class was added using Anthropic, Claude Code, "Implement US 02.11.01:
// require entrants to acknowledge campsite rules and policies before confirming a reservation",
// 2026-11-28.
/**
 * End-to-end checks against Firestore for confirming a pending reservation from My Stuff →
 * My Bookings: the campsite rules are shown, Confirm Reservation stays disabled until the
 * entrant checks the agreement box, and confirming saves the acknowledgement and its time.
 */
@RunWith(AndroidJUnit4::class)
class EntrantRulesAcknowledgementTest {

    private val composeTestRule = createAndroidComposeRule<MainActivity>()
    private val testEmail = "rules-ack-${UUID.randomUUID()}@example.com"
    private val firestore = Firebase.firestore
    private val createdDocuments = mutableListOf<DocumentReference>()
    private val campsiteRules = "No campfires after 10pm"

    @get:Rule
    val testRule: TestRule = freshSessionRule(composeTestRule)

    /** Removes the Firestore records and local session created for the current test. */
    @After
    fun cleanUpFirebaseTestRecords() {
        runCatching {
            val entrantProfiles = await(
                firestore.collection("profiles").whereEqualTo("email", testEmail).get()
            )
            entrantProfiles.documents.forEach { document -> await(document.reference.delete()) }
        }
        createdDocuments.forEach { document -> runCatching { await(document.delete()) } }

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        runBlocking { AuthManager(context).clearUserId() }
    }

    /** Waits for a Firebase task or fails after the test timeout. */
    private fun <T> await(task: Task<T>): T = Tasks.await(task, 20, TimeUnit.SECONDS)

    /** Signs up a fresh entrant, waits for the Home screen, and returns the profile ID. */
    private fun signUpEntrant(): String {
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithTag("firstNameInput")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("firstNameInput").performTextInput("Rules")
        composeTestRule.onNodeWithTag("lastNameInput").performTextInput("Entrant")
        composeTestRule.onNodeWithTag("emailInput").performTextInput(testEmail)
        composeTestRule.onNodeWithTag("phoneInput").performTextInput("7801234567")
        composeTestRule.onNodeWithText(UserRole.NORMAL.name).performClick()
        composeTestRule.onNodeWithText("Sign Up").performClick()

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText("Home").fetchSemanticsNodes().isNotEmpty()
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        return runBlocking { AuthManager(context).getCurrentUserId() }
            ?: error("The sign-up flow did not save the entrant profile ID")
    }

    /** Creates a Pending offer at a campsite with [campsiteRules] and returns its ID. */
    private fun seedPendingOffer(entrantId: String, campsiteName: String): String {
        val ownerId = UUID.randomUUID().toString()
        val campsiteId = UUID.randomUUID().toString()
        val reservationId = UUID.randomUUID().toString()
        val ownerDocument = firestore.collection("profiles").document(ownerId)
        val campsiteDocument = firestore.collection("campsites").document(campsiteId)
        val reservationDocument = firestore.collection("reservation").document(reservationId)
        createdDocuments += listOf(ownerDocument, campsiteDocument, reservationDocument)

        await(
            ownerDocument.set(
                mapOf(
                    "id" to ownerId,
                    "name" to "Rules Owner",
                    "email" to "rules-owner-${UUID.randomUUID()}@example.com",
                    "phone" to "7805550199",
                    "type" to "User"
                )
            )
        )
        await(
            campsiteDocument.set(
                mapOf(
                    "id" to campsiteId,
                    "name" to campsiteName,
                    "location" to "Jasper, Alberta",
                    "description" to "A campsite offered through a lottery.$CAMPSITE_RULES_MARKER$campsiteRules",
                    "capacity" to 4,
                    "mapLocation" to "Jasper, Alberta",
                    "campOwnerId" to ownerId
                )
            )
        )

        val completion = CountDownLatch(1)
        var creationError: Exception? = null
        ProfileRepo().addPendingLotteryOffer(
            entrantId,
            Reservation(
                dates = "2026-12-10 - 2026-12-12",
                guestNumber = 2,
                id = reservationId,
                campsiteId = campsiteId,
                campOwnerId = ownerId
            )
        ) { error ->
            creationError = error
            completion.countDown()
        }
        check(completion.await(20, TimeUnit.SECONDS)) { "Timed out creating a pending offer." }
        check(creationError == null) { creationError?.message ?: "Unable to create a pending offer." }

        return reservationId
    }

    /** Returns the canonical reservation document for [reservationId]. */
    private fun reservationDocument(reservationId: String): DocumentSnapshot =
        await(firestore.collection("reservation").document(reservationId).get())

    /** Opens My Bookings, switches to the Pending tab, and waits for [campsiteName]'s rules. */
    private fun openPendingOffer(campsiteName: String) {
        composeTestRule.onNodeWithContentDescription("My Stuff").performClick()
        composeTestRule.onNodeWithText("My Bookings").performClick()
        composeTestRule.onNodeWithText("Pending").performClick()
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText(campsiteName).fetchSemanticsNodes().isNotEmpty() &&
                composeTestRule.onAllNodesWithText(campsiteRules).fetchSemanticsNodes().isNotEmpty()
        }
    }

    // AC: Rules are displayed before confirming, and Confirm Reservation is disabled until the
    // entrant checks the agreement box.
    @Test
    fun rulesAreShownAndConfirmIsDisabledUntilChecked() {
        val entrantId = signUpEntrant()
        val campsiteName = "Rules Display Camp"
        seedPendingOffer(entrantId, campsiteName)
        openPendingOffer(campsiteName)

        composeTestRule.onNodeWithText("Campsite Rules & Policies").assertExists()
        composeTestRule.onNodeWithText(campsiteRules).assertExists()
        composeTestRule.onNodeWithText("Confirm Reservation").assertIsNotEnabled()

        composeTestRule.onNodeWithTag("rulesCheckbox").performClick()

        composeTestRule.onNodeWithText("Confirm Reservation").assertIsEnabled()
    }

    // AC: The reservation is completed once the entrant checks the box and confirms, and the
    // acknowledgement and its time are saved with the reservation.
    @Test
    fun checkingRulesAndConfirmingSavesAcknowledgement() {
        val entrantId = signUpEntrant()
        val campsiteName = "Rules Confirm Camp"
        val reservationId = seedPendingOffer(entrantId, campsiteName)
        val beforeConfirming = LocalDateTime.now().withNano(0).minusSeconds(5)
        openPendingOffer(campsiteName)

        composeTestRule.onNodeWithTag("rulesCheckbox").performClick()
        composeTestRule.onNodeWithText("Confirm Reservation").performClick()

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            reservationDocument(reservationId).getString("status") == ReservationStatus.Active.name
        }

        val saved = reservationDocument(reservationId)
        assertEquals(true, saved.getBoolean("rulesAcknowledged"))
        val acknowledgedAt = LocalDateTime.parse(saved.getString("rulesAcknowledgedAt"))
        assertTrue(!acknowledgedAt.isBefore(beforeConfirming))

        val entrant = await(firestore.collection("profiles").document(entrantId).get())
            .toObject(Profile::class.java) ?: error("The entrant profile was not saved")
        val embedded = entrant.reservations.first { it.id == reservationId }
        assertEquals(ReservationStatus.Active, embedded.status)
        assertTrue(embedded.rulesAcknowledged)
        assertEquals(saved.getString("rulesAcknowledgedAt"), embedded.rulesAcknowledgedAt)
    }

    // AC: The entrant cannot complete a reservation without checking the box.
    @Test
    fun uncheckingRulesDisablesConfirmAndKeepsReservationPending() {
        val entrantId = signUpEntrant()
        val campsiteName = "Rules Uncheck Camp"
        val reservationId = seedPendingOffer(entrantId, campsiteName)
        openPendingOffer(campsiteName)

        composeTestRule.onNodeWithTag("rulesCheckbox").performClick()
        composeTestRule.onNodeWithTag("rulesCheckbox").performClick()
        composeTestRule.onNodeWithText("Confirm Reservation").assertIsNotEnabled()
        composeTestRule.onNodeWithText("Confirm Reservation").performClick()
        composeTestRule.waitForIdle()

        val saved = reservationDocument(reservationId)
        assertEquals(ReservationStatus.Pending.name, saved.getString("status"))
        assertNotEquals(true, saved.getBoolean("rulesAcknowledged"))
        composeTestRule.onNodeWithText(campsiteName).assertExists()
    }
}
