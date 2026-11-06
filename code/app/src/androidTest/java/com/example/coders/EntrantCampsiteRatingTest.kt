package com.example.coders

import androidx.activity.compose.setContent
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
import androidx.navigation.compose.rememberNavController
import com.example.coders.ui.screens.RateCampsiteScreen
import com.example.coders.ui.theme.CodersTheme
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import java.util.UUID
import java.util.concurrent.TimeUnit

// The following test was added using OpenAI, Codex, "Implement US 01.13.01: verify an entrant
// can rate one past campsite reservation and update its average", 2026-11-06.
@RunWith(AndroidJUnit4::class)
class EntrantCampsiteRatingTest {

    private val composeTestRule = createAndroidComposeRule<MainActivity>()
    private val testEmail = "campsite-rating-${UUID.randomUUID()}@example.com"
    private val firestore = Firebase.firestore
    private val ownerId = UUID.randomUUID().toString()
    private val campsiteId = UUID.randomUUID().toString()
    private var ratingReservationId: String? = null

    @get:Rule
    val testRule: TestRule = freshSessionRule(composeTestRule)

    @After
    fun cleanUpFirebaseTestRecords() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val entrantProfiles = await(
            firestore.collection("profiles").whereEqualTo("email", testEmail).get()
        )
        entrantProfiles.documents.forEach { document -> await(document.reference.delete()) }
        await(firestore.collection("profiles").document(ownerId).delete())
        await(firestore.collection("campsites").document(campsiteId).delete())
        ratingReservationId?.let { reservationId ->
            await(firestore.collection("reservationRatings").document(reservationId).delete())
        }
        runBlocking { AuthManager(context).clearUserId() }
    }

    private fun <T> await(task: Task<T>): T = Tasks.await(task, 20, TimeUnit.SECONDS)

    private fun openHome() {
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithTag("firstNameInput").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("firstNameInput").performTextInput("Rating")
        composeTestRule.onNodeWithTag("lastNameInput").performTextInput("Test")
        composeTestRule.onNodeWithTag("emailInput").performTextInput(testEmail)
        composeTestRule.onNodeWithTag("phoneInput").performTextInput("7801234567")
        composeTestRule.onNodeWithText("Entrant").performClick()
        composeTestRule.onNodeWithText("Sign Up").performClick()

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText("Home").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun seedPastReservation(): Reservation {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val entrantId = runBlocking { AuthManager(context).getCurrentUserId() }
            ?: error("The sign-up flow did not save the entrant profile ID")
        val entrantDocument = firestore.collection("profiles").document(entrantId)
        val entrant = await(entrantDocument.get()).toObject(Entrant::class.java)
            ?: error("The signed-in entrant profile was not saved to Firestore")
        val owner = CampOwner(
            name = "Rating Camp Owner",
            email = "rating-owner-${UUID.randomUUID()}@example.com",
            phone = "7805550199",
            id = ownerId
        )
        val campsite = Campsite(
            name = "Rating Test Campsite",
            location = "Jasper, Alberta",
            description = "A campsite used to test reservation ratings.",
            capacity = 4,
            mapLocation = "Jasper, Alberta",
            id = campsiteId,
            campOwnerId = ownerId
        )
        val reservation = Reservation(
            dates = "2026-10-10 - 2026-10-12",
            guestNumber = 2,
            status = ReservationStatus.Past,
            campsiteId = campsiteId,
            campOwnerId = ownerId,
            entrantIds = listOf(entrantId)
        )

        await(firestore.collection("profiles").document(ownerId).set(owner))
        await(firestore.collection("campsites").document(campsiteId).set(campsite))
        await(entrantDocument.set(entrant.copy(reservations = entrant.reservations + reservation)))
        ratingReservationId = reservation.id
        return reservation
    }

    private fun showRatingScreen(reservationId: String) {
        composeTestRule.runOnUiThread {
            composeTestRule.activity.setContent {
                CodersTheme {
                    RateCampsiteScreen(
                        navController = rememberNavController(),
                        reservationId = reservationId
                    )
                }
            }
        }
    }

    private fun ratingForSignedInEntrant(reservationId: String): Int? {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val entrantId = runBlocking { AuthManager(context).getCurrentUserId() } ?: return null
        val entrant = await(
            firestore.collection("profiles").document(entrantId).get()
        ).toObject(Entrant::class.java) ?: return null
        return entrant.reservations.firstOrNull { it.id == reservationId }?.rating
    }

    @Test
    fun entrantCanRateOnePastReservationAndCampsiteAverageIsSaved() {
        openHome()
        val reservation = seedPastReservation()
        showRatingScreen(reservation.id)

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText("Choose a rating from 1 to 5")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithContentDescription("Rating 4").performClick()
        composeTestRule.onNodeWithText("Complete").performClick()

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            ratingForSignedInEntrant(reservation.id) == 4
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val entrantId = runBlocking { AuthManager(context).getCurrentUserId() }
            ?: error("Entrant ID was not available")
        val savedEntrant = await(
            firestore.collection("profiles").document(entrantId).get()
        ).toObject(Entrant::class.java) ?: error("Entrant was not available")
        val savedCampsite = await(
            firestore.collection("campsites").document(campsiteId).get()
        ).toObject(Campsite::class.java) ?: error("Campsite was not available")
        val savedRating = await(
            firestore.collection("reservationRatings").document(reservation.id).get()
        )

        assertEquals(4, savedEntrant.reservations.first { it.id == reservation.id }.rating)
        assertEquals(4.0, savedCampsite.rating)
        assertEquals(4, savedCampsite.ratingTotal)
        assertEquals(1, savedCampsite.ratingCount)
        assertEquals(4L, savedRating.getLong("score"))
    }
}
