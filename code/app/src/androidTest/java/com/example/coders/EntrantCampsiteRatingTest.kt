/** Exercises an entrant's ID-based Firestore campsite-rating workflow. */
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.coders.ui.screens.EntrantPastReservationsScreen
import com.example.coders.ui.screens.RateCampsiteScreen
import com.example.coders.ui.theme.CodersTheme
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentReference
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
/** Verifies rating navigation, persistence, and campsite-average updates for an entrant. */
@RunWith(AndroidJUnit4::class)
class EntrantCampsiteRatingTest {

    private val composeTestRule = createAndroidComposeRule<MainActivity>()
    private val testEmail = "campsite-rating-${UUID.randomUUID()}@example.com"
    private val firestore = Firebase.firestore
    private val ownerId = UUID.randomUUID().toString()
    private val campsiteId = UUID.randomUUID().toString()
    private var ratingReservationId: String? = null
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
        ratingReservationId?.let { reservationId ->
            await(firestore.collection("reservationRatings").document(reservationId).delete())
        }
        runBlocking { AuthManager(context).clearUserId() }
    }

    /** Waits for a Firebase task or fails after the test timeout. */
    private fun <T> await(task: Task<T>): T = Tasks.await(task, 20, TimeUnit.SECONDS)

    /** Signs up a fresh entrant and waits until the Home screen is visible. */
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

    /** Creates ID-only owner, campsite, profile, and reservation documents for a past stay. */
    private fun seedPastReservation(): Reservation {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val entrantId = runBlocking { AuthManager(context).getCurrentUserId() }
            ?: error("The sign-up flow did not save the entrant profile ID")
        val entrantDocument = firestore.collection("profiles").document(entrantId)
        val reservationId = UUID.randomUUID().toString()
        val ownerDocument = firestore.collection("profiles").document(ownerId)
        val campsiteDocument = firestore.collection("campsites").document(campsiteId)
        val reservationDocument = firestore.collection("reservation").document(reservationId)
        createdDocuments += listOf(ownerDocument, campsiteDocument, reservationDocument)

        await(
            ownerDocument.set(
                mapOf(
                    "id" to ownerId,
                    "name" to "Rating Camp Owner",
                    "email" to "rating-owner-${UUID.randomUUID()}@example.com",
                    "phone" to "7805550199",
                    "type" to "CampOwner"
                )
            )
        )
        await(
            campsiteDocument.set(
                mapOf(
                    "id" to campsiteId,
                    "name" to "Rating Test Campsite",
                    "location" to "Jasper, Alberta",
                    "description" to "A campsite used to test reservation ratings.",
                    "capacity" to 4,
                    "mapLocation" to "Jasper, Alberta",
                    "campOwnerId" to ownerId,
                    "ratingTotal" to 0,
                    "ratingCount" to 0
                )
            )
        )
        val reservationData = mapOf(
            "id" to reservationId,
            "campsiteId" to campsiteId,
            "campOwnerId" to ownerId,
            "entrantIds" to listOf(entrantId),
            "dates" to "2026-10-10 - 2026-10-12",
            "guestNumber" to 2,
            "status" to ReservationStatus.Past.name
        )
        val reservation = Reservation(
            dates = "2026-10-10 - 2026-10-12",
            guestNumber = 2,
            status = ReservationStatus.Past,
            id = reservationId,
            campsiteId = campsiteId,
            campOwnerId = ownerId,
            entrantIds = listOf(entrantId)
        )

        await(reservationDocument.set(reservationData))
        await(entrantDocument.update("reservations", listOf(reservationData)))
        ratingReservationId = reservation.id
        return reservation
    }

    /** Renders a navigation graph that starts on Past Reservations for [reservation]. */
    private fun showPastReservationsScreen(reservation: Reservation) {
        composeTestRule.runOnUiThread {
            composeTestRule.activity.setContent {
                CodersTheme {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "entrantpastreservations"
                    ) {
                        composable("entrantpastreservations") {
                            EntrantPastReservationsScreen(
                                navController = navController,
                                testEntrant = Entrant(reservations = listOf(reservation))
                            )
                        }
                        composable(
                            route = "rate_campsite/{reservationId}",
                            arguments = listOf(
                                navArgument("reservationId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            RateCampsiteScreen(
                                navController = navController,
                                reservationId = backStackEntry.arguments
                                    ?.getString("reservationId")
                                    .orEmpty()
                            )
                        }
                    }
                }
            }
        }
    }

    /** Returns the score saved in the signed-in entrant's profile for [reservationId]. */
    private fun ratingForSignedInEntrant(reservationId: String): Int? {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val entrantId = runBlocking { AuthManager(context).getCurrentUserId() } ?: return null
        val entrant = await(
            firestore.collection("profiles").document(entrantId).get()
        ).toObject(Entrant::class.java) ?: return null
        return entrant.reservations.firstOrNull { it.id == reservationId }?.rating
    }

    /** Confirms one submitted rating updates every required Firestore record. */
    @Test
    fun entrantCanRateOnePastReservationAndCampsiteAverageIsSaved() {
        openHome()
        val reservation = seedPastReservation()
        showPastReservationsScreen(reservation)

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText("Rate").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Rate").performClick()

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
        val savedReservation = await(
            firestore.collection("reservation").document(reservation.id).get()
        )

        assertEquals(4, savedEntrant.reservations.first { it.id == reservation.id }.rating)
        assertEquals(4.0, savedCampsite.rating)
        assertEquals(4, savedCampsite.ratingTotal)
        assertEquals(1, savedCampsite.ratingCount)
        assertEquals(4L, savedRating.getLong("score"))
        assertEquals(4L, savedReservation.getLong("rating"))
    }
}
