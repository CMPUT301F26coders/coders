/** Exercises US 01.02.02: an entrant leaving a campsite lottery waiting list. */
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import java.time.LocalDate
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

// The following test class was added using Anthropic, Claude Code, "Implement US 01.02.02: allow
// an entrant to leave a campsite lottery waiting list", 2026-11-28.
/**
 * End-to-end checks against Firestore for leaving a waiting list from My Stuff → Waitlists:
 * the entrant is removed and sees a confirmation, can't leave a list they aren't on, and
 * can't leave once winners are drawn.
 */
@RunWith(AndroidJUnit4::class)
class EntrantLeaveWaitlistTest {

    private val composeTestRule = createAndroidComposeRule<MainActivity>()
    private val testEmail = "leave-waitlist-${UUID.randomUUID()}@example.com"
    private val firestore = Firebase.firestore
    private val createdDocuments = mutableListOf<DocumentReference>()

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
        composeTestRule.onNodeWithTag("firstNameInput").performTextInput("Leave")
        composeTestRule.onNodeWithTag("lastNameInput").performTextInput("Entrant")
        composeTestRule.onNodeWithTag("emailInput").performTextInput(testEmail)
        composeTestRule.onNodeWithTag("phoneInput").performTextInput("7801234567")
        composeTestRule.onNodeWithText("Sign Up").performClick()

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText("Home").fetchSemanticsNodes().isNotEmpty()
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        return runBlocking { AuthManager(context).getCurrentUserId() }
            ?: error("The entrant profile ID was not saved after sign-up.")
    }

    /**
     * Seeds an owner, a campsite, and a lottery. By default registration includes today;
     * [drawn] instead seeds a lottery whose registration has closed and winners are drawn.
     */
    private fun seedLottery(
        campsiteName: String,
        waitlistEntrantIds: List<String> = emptyList(),
        drawn: Boolean = false,
        winnerEntrantIds: List<String> = emptyList()
    ): Lottery {
        val owner = Profile(
            name = "Leave Waitlist Owner",
            email = "leave-owner-${UUID.randomUUID()}@example.com",
            phone = "7805550199"
        )
        val campsite = Campsite(
            name = campsiteName,
            location = "Jasper, Alberta",
            description = "A campsite used to test leaving a lottery waiting list.",
            capacity = 4,
            mapLocation = "53.6015, -112.8683",
            campOwner = owner,
            campOwnerId = owner.id
        )
        val today = LocalDate.now()
        val lottery = Lottery(
            campOwnerId = owner.id,
            campsiteIds = listOf(campsite.id),
            registrationStartDate = today.minusDays(if (drawn) 5 else 1).toString(),
            registrationEndDate = (if (drawn) today.minusDays(1) else today.plusDays(1)).toString(),
            status = if (drawn) CampOwnerLotteryStatus.Drawn else CampOwnerLotteryStatus.Active,
            drawCompleted = drawn,
            waitlistEntrantIds = waitlistEntrantIds,
            winnerEntrantIds = winnerEntrantIds
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

    /** Runs a [LotteryRepo] call and returns its error, or `null` on success. */
    private fun awaitRepoError(start: (LotteryRepo, (Exception?) -> Unit) -> Unit): Exception? {
        val completion = CountDownLatch(1)
        val result = AtomicReference<Exception?>(null)
        start(LotteryRepo()) { error ->
            result.set(error)
            completion.countDown()
        }
        assertTrue("Timed out waiting for Firebase.", completion.await(20, TimeUnit.SECONDS))
        return result.get()
    }

    /** Reads the lottery's saved state straight from Firestore. */
    private fun savedLottery(lotteryId: String): Lottery =
        await(firestore.collection("lottery").document(lotteryId).get())
            .toObject(Lottery::class.java)
            ?: error("The seeded lottery was not found.")

    /** Waits until a node with exactly [text] is on screen. */
    private fun waitForText(text: String) {
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    /** Opens My Stuff → Waitlists from the bottom bar. */
    private fun openWaitlists() {
        composeTestRule.onNodeWithContentDescription("My Stuff").performClick()
        composeTestRule.onNodeWithText("Waitlists").performClick()
        waitForText("My Waitlists")
    }

    /**
     * Covers: the entrant views a lottery they joined, selects Leave Lottery, is removed from
     * the waiting list, and sees a confirmation message.
     */
    @Test
    fun entrantLeavesJoinedWaitingListAndSeesConfirmation() {
        val entrantId = signUpEntrant()
        val lottery = seedLottery("Leave Waitlist Camp", waitlistEntrantIds = listOf("other-entrant"))
        assertNull(awaitRepoError { repo, done -> repo.joinWaitlist(lottery.id, entrantId, done) })

        openWaitlists()
        waitForText("Leave Waitlist Camp")
        composeTestRule.onNodeWithTag("leaveLotteryButton_${lottery.id}").performClick()

        composeTestRule.onNodeWithText("Leave waiting list?").assertExists()
        composeTestRule.onNodeWithTag("confirmLeaveLottery").performClick()

        waitForText("Left Lottery")
        composeTestRule.onNodeWithText(
            "You have left the waiting list for Leave Waitlist Camp."
        ).assertExists()
        composeTestRule.onNodeWithText("OK").performClick()

        waitForText("You're not on any waiting lists yet.")
        composeTestRule.onNodeWithTag("waitlistCard_${lottery.id}").assertDoesNotExist()

        val saved = savedLottery(lottery.id)
        assertEquals(listOf("other-entrant"), saved.waitlistEntrantIds)
        assertFalse(entrantId in saved.waitlistJoinedDates)
    }

    /** Covers: the entrant cannot leave a waiting list they are not part of. */
    @Test
    fun entrantCannotLeaveWaitingListTheyAreNotOn() {
        val entrantId = signUpEntrant()
        val lottery = seedLottery("Leave Not Joined Camp", waitlistEntrantIds = listOf("other-entrant"))

        val error = awaitRepoError { repo, done -> repo.leaveWaitlist(lottery.id, entrantId, done) }

        assertEquals(WaitlistLeaveErrors.NOT_ON_WAITLIST, error?.message)
        assertEquals(listOf("other-entrant"), savedLottery(lottery.id).waitlistEntrantIds)

        // The lottery isn't listed on the entrant's Waitlists screen, so there is nothing to leave.
        openWaitlists()
        waitForText("You're not on any waiting lists yet.")
        composeTestRule.onNodeWithTag("leaveLotteryButton_${lottery.id}").assertDoesNotExist()
    }

    /** Covers: once winners are drawn, the waiting list can no longer be left. */
    @Test
    fun drawnLotteryHidesLeaveLotteryAndRejectsLeaving() {
        val entrantId = signUpEntrant()
        val lottery = seedLottery(
            campsiteName = "Leave Drawn Camp",
            waitlistEntrantIds = listOf(entrantId),
            drawn = true,
            winnerEntrantIds = listOf(entrantId)
        )

        val error = awaitRepoError { repo, done -> repo.leaveWaitlist(lottery.id, entrantId, done) }

        assertEquals(WaitlistLeaveErrors.ALREADY_DRAWN, error?.message)
        assertEquals(listOf(entrantId), savedLottery(lottery.id).waitlistEntrantIds)

        openWaitlists()
        waitForText("Leave Drawn Camp")
        composeTestRule.onNodeWithText("Selected!", substring = true).assertExists()
        composeTestRule.onNodeWithTag("leaveLotteryButton_${lottery.id}").assertDoesNotExist()
    }
}
