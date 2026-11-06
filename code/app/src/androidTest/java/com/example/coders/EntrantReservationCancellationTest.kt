/** Exercises an entrant's ID-based Firestore reservation-cancellation workflow. */
package com.example.coders

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
import java.util.concurrent.TimeUnit

// The following test class was added using OpenAI, Codex, "Add a separate instrumented test for
// US 01.07.01: cancelling an entrant reservation", 2026-11-06.
/** Verifies cancellation synchronizes profile and canonical reservation records. */
@RunWith(AndroidJUnit4::class)
class EntrantReservationCancellationTest {

    private val composeTestRule = createAndroidComposeRule<MainActivity>()
    private val testEmail = "reservation-cancellation-${UUID.randomUUID()}@example.com"
    private val firestore = Firebase.firestore
    private val createdDocuments = mutableListOf<DocumentReference>()
    private val campsiteName = "Cancellation Test Campsite"

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
        composeTestRule.onNodeWithTag("firstNameInput").performTextInput("Cancel")
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

    /** Creates ID-only owner, campsite, profile, and Active reservation documents. */
    private fun seedActiveReservationForSignedInEntrant(): String {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val entrantId = runBlocking { AuthManager(context).getCurrentUserId() }
            ?: error("The sign-up flow did not save the entrant profile ID")
        val entrantDocument = firestore.collection("profiles").document(entrantId)
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
                    "name" to "Cancellation Camp Owner",
                    "email" to "cancellation-owner-${UUID.randomUUID()}@example.com",
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
                    "description" to "A campsite used to test reservation cancellation.",
                    "capacity" to 4,
                    "mapLocation" to "Jasper, Alberta",
                    "campOwnerId" to ownerId
                )
            )
        )
        val reservationData = mapOf(
            "id" to reservationId,
            "campsiteId" to campsiteId,
            "campOwnerId" to ownerId,
            "entrantIds" to listOf(entrantId),
            "dates" to "2026-11-15 - 2026-11-17",
            "guestNumber" to 2,
            "status" to ReservationStatus.Active.name
        )

        await(reservationDocument.set(reservationData))
        await(
            entrantDocument.update(
                "reservations",
                listOf(reservationData)
            )
        )
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

    /** Confirms cancellation persists in Firestore and removes the booking from Active. */
    @Test
    fun test20_CancelReservationUpdatesFirestoreAndRemovesItFromActiveReservations() {
        openHome()
        val reservationId = seedActiveReservationForSignedInEntrant()
        openEntrantReservations()

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText(campsiteName)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithContentDescription(
            "Cancel reservation for $campsiteName"
        ).performScrollTo().performClick()
        composeTestRule.onNodeWithText("Cancel reservation?").assertExists()
        composeTestRule.onNodeWithText(
            "Confirm cancellation",
            useUnmergedTree = true
        ).performClick()

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            reservationStatusOnSignedInEntrant(reservationId) ==
                ReservationStatus.Cancelled &&
                reservationStatusInFirestore(reservationId) ==
                    ReservationStatus.Cancelled &&
                composeTestRule.onAllNodesWithText(campsiteName)
                    .fetchSemanticsNodes().isEmpty()
        }

        composeTestRule.onNodeWithText(campsiteName).assertDoesNotExist()
    }
}
