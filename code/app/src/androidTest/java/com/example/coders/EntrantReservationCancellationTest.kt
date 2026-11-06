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
@RunWith(AndroidJUnit4::class)
class EntrantReservationCancellationTest {

    private val composeTestRule = createAndroidComposeRule<MainActivity>()
    private val testEmail = "reservation-cancellation-${UUID.randomUUID()}@example.com"
    private val firestore = Firebase.firestore

    @get:Rule
    val testRule: TestRule = freshSessionRule(composeTestRule)

    @After
    fun cleanUpFirebaseTestRecords() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val entrantProfiles = await(
            firestore.collection("profiles").whereEqualTo("email", testEmail).get()
        )
        entrantProfiles.documents.forEach { document -> await(document.reference.delete()) }
        runBlocking { AuthManager(context).clearUserId() }
    }

    private fun <T> await(task: Task<T>): T = Tasks.await(task, 20, TimeUnit.SECONDS)

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

    private fun openEntrantReservations() {
        composeTestRule.onNodeWithContentDescription("My Stuff").performClick()
        composeTestRule.onNodeWithText("My Bookings").performClick()
        composeTestRule.onNodeWithText("Your Reservations").assertExists()
    }

    private fun seedActiveReservationForSignedInEntrant(): Reservation {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val entrantId = runBlocking { AuthManager(context).getCurrentUserId() }
            ?: error("The sign-up flow did not save the entrant profile ID")
        val entrantDocument = firestore.collection("profiles").document(entrantId)
        val entrant = await(entrantDocument.get()).toObject(Entrant::class.java)
            ?: error("The signed-in entrant profile was not saved to Firestore")
        val owner = CampOwner(
            name = "Cancellation Camp Owner",
            email = "cancellation-owner-${UUID.randomUUID()}@example.com",
            phone = "7805550199"
        )
        val campsite = Campsite(
            name = "Cancellation Test Campsite",
            location = "Jasper, Alberta",
            description = "A campsite used to test reservation cancellation.",
            capacity = 4,
            mapLocation = "Jasper, Alberta",
            campOwner = owner,
            id = UUID.randomUUID().toString()
        )
        val reservation = Reservation(
            campsite = campsite,
            dates = "2026-11-15 - 2026-11-17",
            guestNumber = 2,
            status = ReservationStatus.Active,
            campOwner = owner,
            entrants = listOf(entrant),
            entrantIds = listOf(entrantId)
        )

        await(entrantDocument.set(entrant.copy(reservations = entrant.reservations + reservation)))
        return reservation
    }

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

    @Test
    fun test20_CancelReservationUpdatesFirestoreAndRemovesItFromActiveReservations() {
        openHome()
        val reservationToCancel = seedActiveReservationForSignedInEntrant()
        openEntrantReservations()

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText(reservationToCancel.campsite.name)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithContentDescription(
            "Cancel reservation for ${reservationToCancel.campsite.name}"
        ).performScrollTo().performClick()
        composeTestRule.onNodeWithText("Cancel reservation?").assertExists()
        composeTestRule.onNodeWithText(
            "Confirm cancellation",
            useUnmergedTree = true
        ).performClick()

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            reservationStatusOnSignedInEntrant(reservationToCancel.id) ==
                ReservationStatus.Cancelled &&
                composeTestRule.onAllNodesWithText(reservationToCancel.campsite.name)
                    .fetchSemanticsNodes().isEmpty()
        }

        composeTestRule.onNodeWithText(reservationToCancel.campsite.name).assertDoesNotExist()
    }
}
