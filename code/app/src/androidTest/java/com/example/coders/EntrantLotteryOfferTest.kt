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
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import java.util.UUID
import java.util.concurrent.TimeUnit

// The following test class was added using OpenAI, Codex, "Add separate instrumented tests for
// US 01.04.03: accepting and declining an entrant lottery offer", 2026-11-06.
@RunWith(AndroidJUnit4::class)
class EntrantLotteryOfferTest {

    private val composeTestRule = createAndroidComposeRule<MainActivity>()
    private val testEmail = "lottery-offer-${UUID.randomUUID()}@example.com"
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

    private fun openEntrantReservations() {
        composeTestRule.onNodeWithContentDescription("My Stuff").performClick()
        composeTestRule.onNodeWithText("My Bookings").performClick()
        composeTestRule.onNodeWithText("Your Reservations").assertExists()
    }

    private fun seedPendingOfferForSignedInEntrant(campsiteName: String): Reservation {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val entrantId = runBlocking { AuthManager(context).getCurrentUserId() }
            ?: error("The sign-up flow did not save the entrant profile ID")
        val entrantDocument = firestore.collection("profiles").document(entrantId)
        val entrant = await(entrantDocument.get()).toObject(Entrant::class.java)
            ?: error("The signed-in entrant profile was not saved to Firestore")
        val owner = CampOwner(
            name = "Lottery Owner",
            email = "lottery-owner-${UUID.randomUUID()}@example.com",
            phone = "7805550199"
        )
        val campsite = Campsite(
            name = campsiteName,
            location = "Jasper, Alberta",
            description = "A campsite offered through a lottery.",
            capacity = 4,
            mapLocation = "Jasper, Alberta",
            campOwner = owner,
            id = UUID.randomUUID().toString()
        )
        val offer = Reservation(
            campsite = campsite,
            dates = "2026-11-15 - 2026-11-17",
            guestNumber = 2,
            status = ReservationStatus.Pending,
            campOwner = owner,
            entrants = listOf(entrant),
            entrantIds = listOf(entrantId)
        )

        await(entrantDocument.set(entrant.copy(reservations = entrant.reservations + offer)))
        return offer
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
    fun acceptLotteryOfferMovesItToActiveReservations() {
        openHome()
        val offer = seedPendingOfferForSignedInEntrant("Accept Offer Camp")
        openEntrantReservations()

        composeTestRule.onNodeWithText("Pending").performClick()
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText("Accept Offer Camp")
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Accept Offer").performClick()

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            reservationStatusOnSignedInEntrant(offer.id) == ReservationStatus.Active
        }

        composeTestRule.onNodeWithText("Accept Offer Camp").assertExists()
        composeTestRule.onNodeWithText("Accept Offer").assertDoesNotExist()
    }

    @Test
    fun declineLotteryOfferRejectsAndRemovesItFromPending() {
        openHome()
        val offer = seedPendingOfferForSignedInEntrant("Decline Offer Camp")
        openEntrantReservations()

        composeTestRule.onNodeWithText("Pending").performClick()
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText("Decline Offer Camp")
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Decline Offer").performClick()
        composeTestRule.onNodeWithText("Decline lottery offer?").assertExists()
        composeTestRule.onNodeWithText("Decline offer", useUnmergedTree = true).performClick()

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            reservationStatusOnSignedInEntrant(offer.id) == ReservationStatus.Rejected
        }

        composeTestRule.onNodeWithText("No pending reservations.").assertExists()
        composeTestRule.onNodeWithText("Decline Offer Camp").assertDoesNotExist()
    }
}
