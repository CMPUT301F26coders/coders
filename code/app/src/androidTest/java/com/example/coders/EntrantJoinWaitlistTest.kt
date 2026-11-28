/** Exercises US 01.02.01: an entrant joining the waiting list for a campsite lottery. */
package com.example.coders

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.coders.ui.screens.CampsiteDetailsScreen
import com.example.coders.ui.theme.CodersTheme
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
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

// The following test class was added using Anthropic, Claude Code, "Implement US 01.02.01: allow
// an entrant to join the waiting list for an active campsite lottery", 2026-11-27.
/**
 * End-to-end checks against Firestore for joining a lottery's waiting list: the entrant is
 * added exactly once, sees a confirmation, cannot join twice, and cannot join an inactive lottery.
 */
@RunWith(AndroidJUnit4::class)
class EntrantJoinWaitlistTest {

    private data class LotteryFixture(
        val lottery: Lottery,
        val campsite: Campsite
    )

    private val composeTestRule = createAndroidComposeRule<MainActivity>()
    private val testEmail = "join-waitlist-${UUID.randomUUID()}@example.com"
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
        composeTestRule.onNodeWithTag("firstNameInput").performTextInput("Waitlist")
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

    /** Seeds an owner, a campsite, and a lottery with the given registration period. */
    private fun seedLottery(
        campsiteName: String,
        registrationStartDate: LocalDate = LocalDate.now().minusDays(1),
        registrationEndDate: LocalDate = LocalDate.now().plusDays(1),
        waitlistEntrantIds: List<String> = emptyList()
    ): LotteryFixture {
        val owner = Profile(
            name = "Waitlist Owner",
            email = "waitlist-owner-${UUID.randomUUID()}@example.com",
            phone = "7805550199"
        )
        val campsite = Campsite(
            name = campsiteName,
            location = "Jasper, Alberta",
            description = "A campsite used to test joining a lottery waiting list.",
            capacity = 4,
            mapLocation = "53.6015, -112.8683",
            campOwner = owner,
            campOwnerId = owner.id
        )
        val lottery = Lottery(
            campOwnerId = owner.id,
            campsiteIds = listOf(campsite.id),
            registrationStartDate = registrationStartDate.toString(),
            registrationEndDate = registrationEndDate.toString(),
            status = CampOwnerLotteryStatus.Active,
            waitlistEntrantIds = waitlistEntrantIds
        )
        val ownerDocument = firestore.collection("profiles").document(owner.id)
        val campsiteDocument = firestore.collection("campsites").document(campsite.id)
        val lotteryDocument = firestore.collection("lottery").document(lottery.id)
        createdDocuments += listOf(ownerDocument, campsiteDocument, lotteryDocument)

        await(ownerDocument.set(owner))
        await(campsiteDocument.set(campsite))
        await(lotteryDocument.set(lottery))

        return LotteryFixture(lottery, campsite)
    }

    /** Renders the production campsite details screen for [campsite]. */
    private fun openCampsiteDetails(campsite: Campsite) {
        composeTestRule.runOnUiThread {
            composeTestRule.activity.setContent {
                CodersTheme {
                    CampsiteDetailsScreen(
                        navController = rememberNavController(),
                        campsite = campsite
                    )
                }
            }
        }
        composeTestRule.onNodeWithText(campsite.name).assertExists()
    }

    /** Calls [LotteryRepo.joinWaitlist] and returns its error, or `null` on success. */
    private fun joinWaitlist(lotteryId: String, entrantId: String): Exception? {
        val completion = CountDownLatch(1)
        val joinError = AtomicReference<Exception?>(null)
        LotteryRepo().joinWaitlist(lotteryId, entrantId) { error ->
            joinError.set(error)
            completion.countDown()
        }
        assertTrue("Timed out joining the waiting list.", completion.await(20, TimeUnit.SECONDS))
        return joinError.get()
    }

    /** Reads the lottery's saved waitlist straight from Firestore. */
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

    /**
     * Covers: the entrant can join an active lottery's waiting list, the application adds them
     * to it, and the application confirms the join.
     */
    @Test
    fun activeLottery_entrantJoinsWaitingListAndSeesConfirmation() {
        val entrantId = signUpEntrant()
        val fixture = seedLottery("Waitlist Join Camp")
        val joinButtonTag = "joinWaitlistButton_${fixture.lottery.id}"

        openCampsiteDetails(fixture.campsite)
        waitForText("Join Lottery")
        composeTestRule.onNodeWithText("Waiting list: 0 entrants").assertExists()

        composeTestRule.onNodeWithTag(joinButtonTag).performClick()

        waitForText("Joined Lottery")
        composeTestRule.onNodeWithText(
            "You have joined the waiting list for Waitlist Join Camp."
        ).assertExists()
        composeTestRule.onNodeWithText("OK").performClick()

        waitForText("On Waiting List")
        composeTestRule.onNodeWithTag(joinButtonTag).assertIsNotEnabled()
        waitForText("Waiting list: 1 entrant")

        val saved = savedLottery(fixture.lottery.id)
        assertEquals(listOf(entrantId), saved.waitlistEntrantIds)
        assertEquals(LocalDate.now().toString(), saved.waitlistJoinedDates[entrantId])
    }

    /** Covers: the application prevents the entrant from joining the same waiting list twice. */
    @Test
    fun entrantCannotJoinSameWaitingListTwice() {
        val entrantId = signUpEntrant()
        val fixture = seedLottery("Waitlist Duplicate Camp")

        assertNull(joinWaitlist(fixture.lottery.id, entrantId))
        val secondAttempt = joinWaitlist(fixture.lottery.id, entrantId)

        assertEquals(WaitlistJoinErrors.ALREADY_JOINED, secondAttempt?.message)
        assertEquals(
            "The entrant should appear on the waitlist exactly once.",
            1,
            savedLottery(fixture.lottery.id).waitlistEntrantIds.count { it == entrantId }
        )

        // Reopening the screen reads the saved waitlist, so the join button stays disabled.
        openCampsiteDetails(fixture.campsite)
        waitForText("On Waiting List")
        composeTestRule.onNodeWithTag("joinWaitlistButton_${fixture.lottery.id}")
            .assertIsNotEnabled()
        composeTestRule.onNodeWithText("Join Lottery").assertDoesNotExist()
    }

    /** Covers: the entrant cannot join the waiting list if the lottery is inactive. */
    @Test
    fun inactiveLottery_entrantCannotJoinWaitingList() {
        val entrantId = signUpEntrant()
        val fixture = seedLottery(
            campsiteName = "Waitlist Closed Camp",
            registrationStartDate = LocalDate.now().minusDays(5),
            registrationEndDate = LocalDate.now().minusDays(1)
        )

        val error = joinWaitlist(fixture.lottery.id, entrantId)

        assertEquals(WaitlistJoinErrors.INACTIVE, error?.message)
        assertTrue(savedLottery(fixture.lottery.id).waitlistEntrantIds.isEmpty())

        openCampsiteDetails(fixture.campsite)
        composeTestRule.onNodeWithText("Join Lottery").assertDoesNotExist()
        composeTestRule.onNodeWithTag("joinWaitlistButton_${fixture.lottery.id}")
            .assertDoesNotExist()
    }
}
