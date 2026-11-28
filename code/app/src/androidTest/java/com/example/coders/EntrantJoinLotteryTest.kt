/** Exercises an entrant joining an active lottery from a campsite's details screen. */
package com.example.coders

import androidx.activity.compose.setContent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.navigation.compose.rememberNavController
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import java.time.LocalDate
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

// The following test class was added using OpenAI, Codex, "Implement US 01.01.06: allow an
// entrant to join an active lottery from campsite details", 2026-11-27.
/** Verifies successful and inactive lottery joining behavior for entrants. */
@RunWith(AndroidJUnit4::class)
class EntrantJoinLotteryTest {

    private data class LotteryFixture(
        val lottery: Lottery,
        val campsite: Campsite
    )

    private val composeTestRule = createAndroidComposeRule<MainActivity>()
    private val testEmail = "join-lottery-${UUID.randomUUID()}@example.com"
    private val firestore = Firebase.firestore
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
        composeTestRule.onNodeWithTag("firstNameInput").performTextInput("Join")
        composeTestRule.onNodeWithTag("lastNameInput").performTextInput("Lottery")
        composeTestRule.onNodeWithTag("emailInput").performTextInput(testEmail)
        composeTestRule.onNodeWithTag("phoneInput").performTextInput("7801234567")
        composeTestRule.onNodeWithText("Sign Up").performClick()

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText("Home").fetchSemanticsNodes().isNotEmpty()
        }
    }

    // The following fixture and details-screen renderer were modified using OpenAI, Codex,
    // "Implement US 01.01.06: allow an entrant to join an active lottery from campsite details",
    // 2026-11-27.
    /** Seeds a campsite and a lottery whose registration period includes today. */
    private fun seedLottery(
        campsiteName: String,
        status: CampOwnerLotteryStatus = CampOwnerLotteryStatus.Active,
        drawCompleted: Boolean = false
    ): LotteryFixture {
        val owner = Profile(
            name = "Lottery Owner",
            email = "lottery-owner-${UUID.randomUUID()}@example.com",
            phone = "7805550199"
        )
        val campsite = Campsite(
            name = campsiteName,
            location = "Jasper, Alberta",
            description = "A campsite available through a lottery.",
            capacity = 4,
            mapLocation = "53.6015, -112.8683",
            campOwner = owner,
            campOwnerId = owner.id
        )
        val today = LocalDate.now()
        val lottery = Lottery(
            campOwnerId = owner.id,
            campsiteIds = listOf(campsite.id),
            registrationStartDate = today.minusDays(1).toString(),
            registrationEndDate = today.plusDays(1).toString(),
            status = status,
            drawCompleted = drawCompleted
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

    /** Renders the selected campsite's production details screen. */
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

    /** Returns the currently signed-in entrant's profile document ID. */
    private fun signedInEntrantId(): String {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        return runBlocking { AuthManager(context).getCurrentUserId() }
            ?: error("The entrant profile ID was not saved after sign-up.")
    }

    /** Invokes the repository and waits for its asynchronous result. */
    private fun awaitJoinError(lotteryId: String, entrantId: String): Exception? {
        val completion = CountDownLatch(1)
        var joinError: Exception? = null
        LotteryRepo().joinWaitlist(lotteryId, entrantId) { error ->
            joinError = error
            completion.countDown()
        }
        check(completion.await(20, TimeUnit.SECONDS)) {
            "Timed out joining the lottery."
        }
        return joinError
    }

    // The following function was added using OpenAI, Codex, "Implement US 01.01.06: allow an
    // entrant to join an active lottery from campsite details", 2026-11-27.
    @Test
    fun activeLottery_canBeJoinedFromCampsiteDetails() {
        openHome()
        val campsiteName = "Joinable Lottery Camp"
        val fixture = seedLottery(campsiteName)
        val entrantId = signedInEntrantId()

        openCampsiteDetails(fixture.campsite)
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText("Join Lottery")
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Join Lottery").performClick()

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText("Joined Lottery")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Joined Lottery").assertExists()
        composeTestRule.onNodeWithText("OK").performClick()

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText("Joined")
                .fetchSemanticsNodes().isNotEmpty()
        }

        val savedLottery = await(
            firestore.collection("lottery").document(fixture.lottery.id).get()
        ).toObject(Lottery::class.java)
            ?: error("The seeded lottery was not found.")
        assertTrue(entrantId in savedLottery.waitlistEntrantIds)
        assertEquals(LocalDate.now().toString(), savedLottery.waitlistJoinedDates[entrantId])
    }

    // The following function was added using OpenAI, Codex, "Implement US 01.01.06: allow an
    // entrant to join an active lottery from campsite details", 2026-11-27.
    @Test
    fun inactiveLottery_doesNotExposeJoiningAndRepositoryRejectsIt() {
        openHome()
        val campsiteName = "Closed Lottery Camp"
        val fixture = seedLottery(
            campsiteName = campsiteName,
            status = CampOwnerLotteryStatus.BookingPeriod
        )
        val entrantId = signedInEntrantId()

        openCampsiteDetails(fixture.campsite)
        composeTestRule.onNodeWithText("Join Lottery").assertDoesNotExist()

        val error = awaitJoinError(fixture.lottery.id, entrantId)
        assertNotNull(error)
    }
}
