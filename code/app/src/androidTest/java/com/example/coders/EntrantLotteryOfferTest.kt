/** Exercises an entrant's ID-based Firestore lottery-offer workflow. */
package com.example.coders

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
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

// The following test class was added using OpenAI, Codex, "Add separate instrumented tests for
// US 01.04.03: accepting and declining an entrant lottery offer", 2026-11-06.
/** Verifies accepting and declining pending lottery offers from an entrant account. */
@RunWith(AndroidJUnit4::class)
class EntrantLotteryOfferTest {

    private val composeTestRule = createAndroidComposeRule<MainActivity>()
    private val testEmail = "lottery-offer-${UUID.randomUUID()}@example.com"
    private val firestore = Firebase.firestore
    private val createdDocuments = mutableListOf<DocumentReference>()

    @get:Rule
    val testRule: TestRule = freshSessionRule(composeTestRule)

    /** Removes the Firestore records and local session created for the current test. */
    @After
    fun cleanUpFirebaseTestRecords() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val entrantProfiles = await(
            firestore.collection("profiles").whereEqualTo("email", testEmail).get()
        )
        entrantProfiles.documents.forEach { document -> await(document.reference.delete()) }
        createdDocuments.forEach { document -> await(document.delete()) }
        runBlocking { AuthManager(context).clearUserId() }
    }

    /** Waits for a Firebase task or fails after the test timeout. */
    private fun <T> await(task: Task<T>): T = Tasks.await(task, 20, TimeUnit.SECONDS)

    /** Signs up a fresh entrant and waits until the Home screen is visible. */
    private fun openHome() {
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithTag("firstNameInput")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("firstNameInput").performTextInput("Lottery")
        composeTestRule.onNodeWithTag("lastNameInput").performTextInput("Test")
        composeTestRule.onNodeWithTag("emailInput").performTextInput(testEmail)
        composeTestRule.onNodeWithTag("phoneInput").performTextInput("7801234567")
        composeTestRule.onNodeWithText("Entrant").performClick()
        composeTestRule.onNodeWithText("Sign Up").performClick()

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText("Home").fetchSemanticsNodes().isNotEmpty()
        }
    }

    /** Opens the signed-in entrant's reservation screen. */
    private fun openEntrantReservations() {
        composeTestRule.onNodeWithContentDescription("My Stuff").performClick()
        composeTestRule.onNodeWithText("My Bookings").performClick()
        composeTestRule.onNodeWithText("Your Reservations").assertExists()
    }

    /** Creates a Pending offer and all ID-linked documents required to display [campsiteName]. */
    private fun seedPendingOfferForSignedInEntrant(campsiteName: String): String {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val entrantId = runBlocking { AuthManager(context).getCurrentUserId() }
            ?: error("The sign-up flow did not save the entrant profile ID")
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
                    "name" to "Lottery Owner",
                    "email" to "lottery-owner-${UUID.randomUUID()}@example.com",
                    "phone" to "7805550199",
                    "type" to "CampOwner"
                )
            )
        )
        await(
            campsiteDocument.set(
                mapOf(
                    "id" to campsiteId,
                    "name" to campsiteName,
                    "location" to "Jasper, Alberta",
                    "description" to "A campsite offered through a lottery.",
                    "capacity" to 4,
                    "mapLocation" to "Jasper, Alberta",
                    "campOwnerId" to ownerId
                )
            )
        )
        val offer = Reservation(
            dates = "2026-11-15 - 2026-11-17",
            guestNumber = 2,
            id = reservationId,
            campsiteId = campsiteId,
            campOwnerId = ownerId
        )

        val completion = CountDownLatch(1)
        var creationError: Exception? = null
        ProfileRepo().addPendingLotteryOffer(entrantId, offer) { error ->
            creationError = error
            completion.countDown()
        }
        check(completion.await(20, TimeUnit.SECONDS)) {
            "Timed out creating a pending lottery offer."
        }
        check(creationError == null) {
            creationError?.message ?: "Unable to create a pending lottery offer."
        }
        check(reservationStatusOnSignedInEntrant(reservationId) == ReservationStatus.Pending)
        check(reservationStatusInFirestore(reservationId) == ReservationStatus.Pending)

        return reservationId
    }

    /** Returns the signed-in entrant profile's current status for [reservationId]. */
    private fun reservationStatusOnSignedInEntrant(reservationId: String): ReservationStatus {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val entrantId = runBlocking { AuthManager(context).getCurrentUserId() }
            ?: error("The sign-up flow did not save the entrant profile ID")
        val entrant = await(
            firestore.collection("profiles").document(entrantId).get()
        ).toObject(Entrant::class.java)
            ?: error("The signed-in entrant profile was not saved to Firestore")

        return entrant.reservations.first { it.id == reservationId }.status
    }

    /** Returns the canonical reservation document's current status for [reservationId]. */
    private fun reservationStatusInFirestore(reservationId: String): ReservationStatus =
        ReservationStatus.valueOf(
            await(firestore.collection("reservation").document(reservationId).get())
                .getString("status") ?: error("Reservation status was not saved")
        )

    /** Confirms accepting a Pending offer persists and renders the Active state. */
    @Test
    fun acceptLotteryOfferMovesItToActiveReservations() {
        openHome()
        val campsiteName = "Accept Offer Camp"
        val reservationId = seedPendingOfferForSignedInEntrant(campsiteName)
        openEntrantReservations()

        composeTestRule.onNodeWithText("Pending").performClick()
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText(campsiteName)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Accept Offer").performClick()

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            reservationStatusOnSignedInEntrant(reservationId) == ReservationStatus.Active &&
                reservationStatusInFirestore(reservationId) == ReservationStatus.Active &&
                composeTestRule.onAllNodesWithText(campsiteName)
                    .fetchSemanticsNodes().isNotEmpty() &&
                composeTestRule.onAllNodesWithText("Accept Offer")
                    .fetchSemanticsNodes().isEmpty()
        }

        composeTestRule.onNodeWithText(campsiteName).assertExists()
        composeTestRule.onNodeWithText("Accept Offer").assertDoesNotExist()
    }

    /** Confirms declining a Pending offer persists Rejected and removes it from Pending. */
    @Test
    fun declineLotteryOfferRejectsAndRemovesItFromPending() {
        openHome()
        val campsiteName = "Decline Offer Camp"
        val reservationId = seedPendingOfferForSignedInEntrant(campsiteName)
        openEntrantReservations()

        composeTestRule.onNodeWithText("Pending").performClick()
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText(campsiteName)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Decline Offer").performClick()
        composeTestRule.onNodeWithText("Decline lottery offer?").assertExists()
        composeTestRule.onNodeWithText("Decline offer", useUnmergedTree = true).performClick()

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            reservationStatusOnSignedInEntrant(reservationId) == ReservationStatus.Rejected &&
                reservationStatusInFirestore(reservationId) == ReservationStatus.Rejected &&
                composeTestRule.onAllNodesWithText("No reservations in this tab.")
                    .fetchSemanticsNodes().isNotEmpty() &&
                composeTestRule.onAllNodesWithText(campsiteName)
                    .fetchSemanticsNodes().isEmpty()
        }

        composeTestRule.onNodeWithText("No reservations in this tab.").assertExists()
        composeTestRule.onNodeWithText(campsiteName).assertDoesNotExist()
    }
}
