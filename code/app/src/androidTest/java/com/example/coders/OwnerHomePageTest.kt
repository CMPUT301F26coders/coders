
package com.example.coders

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.coders.ui.screens.OwnerReservationsScreen
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID
import java.util.concurrent.TimeUnit
import androidx.compose.ui.test.onAllNodesWithText

@RunWith(AndroidJUnit4::class)
class CampOwnerReservationsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val firestore = Firebase.firestore
    private val ownerId = UUID.randomUUID().toString()
    private val otherOwnerId = UUID.randomUUID().toString()

    private val createdReservationIds = mutableListOf<String>()

    private fun <T> await(task: Task<T>): T =
        Tasks.await(task, 20, TimeUnit.SECONDS)

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        // Create a test camp owner in Firestore
        await(
            firestore.collection("profiles")
                .document(ownerId)
                .set(
                    mapOf(
                        "id" to ownerId,
                        "name" to "Test Camp Owner",
                        "email" to "owner@test.com",
                        "phone" to "7801234567",
                        "type" to "CampOwner"
                    )
                )
        )

        // Make the test owner the locally signed-in user
        runBlocking {
            AuthManager(context).saveUserId(ownerId)
        }
    }

    @After
    fun cleanUp() {
        // Remove only records created by this test
        createdReservationIds.forEach { id ->
            await(
                firestore.collection("reservation")
                    .document(id)
                    .delete()
            )
        }

        await(
            firestore.collection("profiles")
                .document(ownerId)
                .delete()
        )

        runBlocking {
            AuthManager(
                InstrumentationRegistry.getInstrumentation().targetContext
            ).clearUserId()
        }
    }

    // Create a test reservation in Firestore
    private fun seedReservation(
        name: String,
        status: ReservationStatus = ReservationStatus.Active,
        campOwnerId: String = ownerId
    ): String {

        val reservationId = UUID.randomUUID().toString()
        createdReservationIds.add(reservationId)

        await(
            firestore.collection("reservation")
                .document(reservationId)
                .set(
                    mapOf(
                        "id" to reservationId,
                        "campOwnerId" to campOwnerId,
                        "campsite" to mapOf(
                            "name" to name
                        ),
                        "dates" to "2026-11-15",
                        "guestNumber" to 4,
                        "status" to status.name
                    )
                )
        )

        return reservationId
    }

    // Directly render the owner's reservations screen
    private fun openReservations() {
        composeTestRule.setContent {
            OwnerReservationsScreen(
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithText(
            "Your Reservations"
        ).assertExists()
    }

    // Wait for a reservation to appear
    private fun waitForReservation(name: String) {
        composeTestRule.waitUntil(10_000) {
            composeTestRule.onAllNodesWithText(name)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    @Test
    fun test1_ShowsActiveReservations() {
        seedReservation("Edmonton Camp")
        openReservations()

        waitForReservation("Edmonton Camp")

        composeTestRule.onNodeWithText(
            "Edmonton Camp"
        ).assertExists()

        composeTestRule.onNodeWithText(
            "Guests: 4"
        ).assertExists()

        composeTestRule.onNodeWithText(
            "Status: Active"
        ).assertExists()
    }

    @Test
    fun test2_ShowsPastReservations() {
        seedReservation(
            "Calgary Camp",
            ReservationStatus.Past
        )

        openReservations()

        composeTestRule.onNodeWithText("Past")
            .performClick()

        waitForReservation("Calgary Camp")

        composeTestRule.onNodeWithText(
            "Calgary Camp"
        ).assertExists()
    }

    @Test
    fun test3_ShowsCancelledReservations() {
        seedReservation(
            "Cancelled Camp",
            ReservationStatus.Cancelled
        )

        seedReservation(
            "Rejected Camp",
            ReservationStatus.Rejected
        )

        openReservations()

        composeTestRule.onNodeWithText("Cancelled")
            .performClick()

        waitForReservation("Cancelled Camp")
        waitForReservation("Rejected Camp")

        composeTestRule.onNodeWithText(
            "Cancelled Camp"
        ).assertExists()

        composeTestRule.onNodeWithText(
            "Rejected Camp"
        ).assertExists()
    }

    @Test
    fun test4_OnlyShowsCurrentOwnerReservations() {
        seedReservation(
            "My Campsite",
            campOwnerId = ownerId
        )

        seedReservation(
            "Other Owner Campsite",
            campOwnerId = otherOwnerId
        )

        openReservations()

        waitForReservation("My Campsite")

        composeTestRule.onNodeWithText(
            "My Campsite"
        ).assertExists()

        composeTestRule.onNodeWithText(
            "Other Owner Campsite"
        ).assertDoesNotExist()
    }

    @Test
    fun test5_RejectReservationUpdatesFirestore() {
        val reservationId = seedReservation(
            "Reject Test Camp"
        )

        openReservations()

        waitForReservation("Reject Test Camp")

        composeTestRule.onNodeWithText(
            "Reject Reservation"
        ).performClick()

        composeTestRule.onNodeWithText(
            "Reject Reservation?"
        ).assertExists()

        composeTestRule.onNodeWithText(
            "Reject",
            useUnmergedTree = true
        ).performClick()

        // Wait until the UI switches to the Cancelled tab
        // and displays the updated reservation.
        composeTestRule.waitUntil(10_000) {
            composeTestRule.onAllNodesWithText("Status: Rejected")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Verify that Firestore actually saved the change
        val document = await(
            firestore.collection("reservation")
                .document(reservationId)
                .get()
        )

        org.junit.Assert.assertEquals(
            "Rejected",
            document.getString("status")
        )

        composeTestRule.onNodeWithText(
            "Reject Test Camp"
        ).assertExists()
    }

    @Test
    fun test6_ShowsEmptyReservations() {
        openReservations()

        composeTestRule.onNodeWithText(
            "No present reservations found."
        ).assertExists()

        composeTestRule.onNodeWithText("Past")
            .performClick()

        composeTestRule.onNodeWithText(
            "No past reservations found."
        ).assertExists()

        composeTestRule.onNodeWithText("Cancelled")
            .performClick()

        composeTestRule.onNodeWithText(
            "No cancelled reservations found."
        ).assertExists()
    }
}
