/** Exercises the Past Reservations screen's filtering and empty-state behavior. */
package com.example.coders


import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.coders.ui.screens.EntrantPastReservationsScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Verifies the Past Reservations screen using lightweight Compose test data. */
@RunWith(AndroidJUnit4::class)
class EntrantPastReservationsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /** Confirms a reservation with Past status appears in the list. */
    @Test
    fun testPastReservationIsDisplayed() {

        val pastReservation = Reservation(
            campsiteId = "test-campsite",
            dates = "Oct 10-12, 2026",
            guestNumber = 4,
            status = ReservationStatus.Past
        )

        val testEntrant = Entrant(
            name = "John Doe",
            reservations = listOf(pastReservation)
        )


        composeTestRule.setContent {
            EntrantPastReservationsScreen(
                navController = rememberNavController(),
                testEntrant = testEntrant
            )
        }


        composeTestRule.onNodeWithText("Past Reservations:").assertIsDisplayed()
        composeTestRule.onNodeWithText("4 guests").assertIsDisplayed()
    }

    /** Confirms the screen communicates when an entrant has no past reservations. */
    @Test
    fun testEmptyPastReservationsShowsPlaceholder() {

        val emptyEntrant = Entrant(name = "Jane Doe", reservations = emptyList())

        composeTestRule.setContent {
            EntrantPastReservationsScreen(
                navController = rememberNavController(),
                testEntrant = emptyEntrant
            )
        }

        composeTestRule.onNodeWithText("No past reservations found.").assertIsDisplayed()
    }
}
