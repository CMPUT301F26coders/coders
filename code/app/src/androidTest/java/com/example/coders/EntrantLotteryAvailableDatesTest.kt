/** Exercises US 01.04.01: a lottery winner viewing the available dates of the won campsite. */
package com.example.coders

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onAllNodesWithTag
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
import java.time.LocalDate
import java.util.UUID
import java.util.concurrent.TimeUnit

// The following test class was added using Anthropic, Claude Code, "Implement US 01.04.01: show
// lottery winners the available dates for the campsite they won", 2026-11-28.

@RunWith(AndroidJUnit4::class)
class EntrantLotteryAvailableDatesTest {

    private val composeTestRule = createAndroidComposeRule<MainActivity>()
    private val testEmail = "available-dates-${UUID.randomUUID()}@example.com"
    private val firestore = Firebase.firestore
    private val createdDocuments = mutableListOf<DocumentReference>()

    @get:Rule
    val testRule: TestRule = freshSessionRule(composeTestRule)


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


    private fun <T> await(task: Task<T>): T = Tasks.await(task, 20, TimeUnit.SECONDS)


    private fun signUpEntrant(): String {
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithTag("firstNameInput")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("firstNameInput").performTextInput("Dates")
        composeTestRule.onNodeWithTag("lastNameInput").performTextInput("Winner")
        composeTestRule.onNodeWithTag("emailInput").performTextInput(testEmail)
        composeTestRule.onNodeWithTag("phoneInput").performTextInput("7801234567")
        composeTestRule.onNodeWithText(UserRole.NORMAL.name).performClick()
        composeTestRule.onNodeWithText("Sign Up").performClick()

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText("Home").fetchSemanticsNodes().isNotEmpty()
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        return runBlocking { AuthManager(context).getCurrentUserId() }
            ?: error("The entrant profile ID was not saved after sign-up.")
    }


    private fun seedDrawnLottery(entrantId: String, campsiteName: String, won: Boolean): Lottery {
        val owner = Profile(
            name = "Available Dates Owner",
            email = "dates-owner-${UUID.randomUUID()}@example.com",
            phone = "7805550199"
        )
        val campsite = Campsite(
            name = campsiteName,
            location = "Jasper, Alberta",
            description = "A campsite used to test lottery available dates.",
            capacity = 4,
            mapLocation = "53.6015, -112.8683",
            campOwner = owner,
            campOwnerId = owner.id
        )
        val today = LocalDate.now()
        val lottery = Lottery(
            campOwnerId = owner.id,
            campsiteIds = listOf(campsite.id),
            registrationStartDate = today.minusDays(5).toString(),
            registrationEndDate = today.minusDays(1).toString(),
            status = CampOwnerLotteryStatus.Drawn,
            drawCompleted = true,
            waitlistEntrantIds = listOf(entrantId, "other-entrant"),
            winnerEntrantIds = if (won) listOf(entrantId) else listOf("other-entrant"),
            availabilityStartDate = "2027-01-05",
            availabilityEndDate = "2027-01-10"
        )
        val ownerDocument = firestore.collection("profiles").document(owner.id)
        val campsiteDocument = firestore.collection("campsites").document(campsite.id)
        val lotteryDocument = firestore.collection("lottery").document(lottery.id)
        createdDocuments += listOf(ownerDocument, campsiteDocument, lotteryDocument)

        await(ownerDocument.set(owner))
        await(campsiteDocument.set(campsite))
        await(lotteryDocument.set(lottery))

        return lottery
    }


    private fun seedReservation(
        lottery: Lottery,
        entrantId: String,
        start: String,
        end: String,
        status: ReservationStatus
    ) {
        val reservationId = UUID.randomUUID().toString()
        val document = firestore.collection("reservation").document(reservationId)
        createdDocuments += document
        await(
            document.set(
                mapOf(
                    "id" to reservationId,
                    "campsiteId" to lottery.campsiteIds.first(),
                    "campOwnerId" to lottery.campOwnerId,
                    "entrantIds" to listOf(entrantId),
                    "lotteryId" to lottery.id,
                    "dates" to "$start - $end",
                    "startDate" to start,
                    "endDate" to end,
                    "guestNumber" to 2,
                    "status" to status.name
                )
            )
        )
    }


    private fun waitForText(text: String) {
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun openWaitlists() {
        composeTestRule.onNodeWithContentDescription("My Stuff").performClick()
        composeTestRule.onNodeWithText("Waitlists").performClick()
        waitForText("My Waitlists")
    }

    private fun assertDay(date: String, status: CampsiteDateStatus) {
        composeTestRule.onNodeWithTag("day_$date")
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.ContentDescription,
                    listOf("$date ${status.label}")
                )
            )
    }

    // AC: When an entrant wins, the available dates are shown on a calendar that clearly marks
    // available, booked, and unavailable dates, and the entrant's own awarded dates.
    @Test
    fun winnerSeesAvailableBookedUnavailableAndOwnDates() {
        val entrantId = signUpEntrant()
        val lottery = seedDrawnLottery(entrantId, "Available Dates Camp", won = true)
        seedReservation(lottery, "other-entrant", "2027-01-07", "2027-01-09", ReservationStatus.Active)
        seedReservation(lottery, entrantId, "2027-01-10", "2027-01-11", ReservationStatus.Pending)

        openWaitlists()
        waitForText("Available Dates Camp")
        composeTestRule.onNodeWithTag("viewAvailableDates_${lottery.id}").performClick()

        waitForText("Available Dates")
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText("Available: 3 dates between 2027-01-05 and 2027-01-10")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("January 2027").assertExists()

        assertDay("2027-01-04", CampsiteDateStatus.Unavailable)
        assertDay("2027-01-05", CampsiteDateStatus.Available)
        assertDay("2027-01-06", CampsiteDateStatus.Available)
        assertDay("2027-01-07", CampsiteDateStatus.Booked)
        assertDay("2027-01-08", CampsiteDateStatus.Booked)
        assertDay("2027-01-09", CampsiteDateStatus.Available)
        assertDay("2027-01-10", CampsiteDateStatus.Yours)
        assertDay("2027-01-11", CampsiteDateStatus.Unavailable)

        composeTestRule.onNodeWithTag("day_2027-01-07").performClick()
        composeTestRule.onNodeWithText("Status: Booked").performScrollTo().assertExists()
    }

    // AC: Only entrants who won are shown the available dates.
    @Test
    fun entrantWhoWasNotSelectedIsNotOfferedAvailableDates() {
        val entrantId = signUpEntrant()
        val lottery = seedDrawnLottery(entrantId, "Not Selected Camp", won = false)

        openWaitlists()
        waitForText("Not Selected Camp")

        composeTestRule.onNodeWithText("Not selected this time").assertExists()
        composeTestRule.onNodeWithTag("viewAvailableDates_${lottery.id}").assertDoesNotExist()
    }
}
