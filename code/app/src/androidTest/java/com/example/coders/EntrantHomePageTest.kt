
package com.example.coders

import androidx.test.platform.app.InstrumentationRegistry
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
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

@RunWith(AndroidJUnit4::class)
class EntrantHomePageTest {

    private val composeTestRule = createAndroidComposeRule<MainActivity>()
    private val testEmail = "entrant-test-${UUID.randomUUID()}@example.com"
    private val firestore = Firebase.firestore
    private val createdOwnerIds = mutableSetOf<String>()
    private val createdCampsiteIds = mutableSetOf<String>()

    @get:Rule
    val testRule: TestRule = freshSessionRule(composeTestRule)

    @After
    fun cleanUpFirebaseTestRecords() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val entrantProfiles = await(
            firestore.collection("profiles").whereEqualTo("email", testEmail).get()
        )
        entrantProfiles.documents.forEach { document ->
            await(document.reference.delete())
        }
        createdOwnerIds.forEach { await(firestore.collection("profiles").document(it).delete()) }
        createdCampsiteIds.forEach { await(firestore.collection("campsites").document(it).delete()) }
        runBlocking { AuthManager(context).clearUserId() }
    }

    private fun <T> await(task: Task<T>): T = Tasks.await(task, 20, TimeUnit.SECONDS)

    /** Waits until startup routing has displayed the sign-up form. */
    private fun waitForSignUpScreen() {
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithTag("firstNameInput").fetchSemanticsNodes().isNotEmpty()
        }
    }

    // Fill out the sign-up form
    private fun enterInformation() {
        waitForSignUpScreen()

        composeTestRule.onNodeWithTag("firstNameInput")
            .performTextInput("David")

        composeTestRule.onNodeWithTag("lastNameInput")
            .performTextInput("Test")

        composeTestRule.onNodeWithTag("emailInput")
            .performTextInput(testEmail)

        composeTestRule.onNodeWithTag("phoneInput")
            .performTextInput("7801234567")
    }

    /** Signs up as an entrant and waits for the asynchronous Firestore result. */
    private fun openHome() {

        enterInformation()

        composeTestRule.onNodeWithText("Entrant")
            .performClick()

        composeTestRule.onNodeWithText("Sign Up")
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText("Home").fetchSemanticsNodes().isNotEmpty() ||
                composeTestRule.onAllNodesWithText(
                    "Couldn't save your profile. Please try again."
                ).fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Home").assertExists()
    }

    // Open the Messages screen
    private fun openMessages() {

        openHome()

        clickMessages()
    }

    private fun clickMessages() {
        composeTestRule.onNodeWithContentDescription("Messages")
            .performClick()

        composeTestRule.onNodeWithText("Messages")
            .assertExists()
    }

    private fun openEntrantReservations() {
        composeTestRule.onNodeWithContentDescription("My Stuff")
            .performClick()
        composeTestRule.onNodeWithText("My Bookings")
            .performClick()
        composeTestRule.onNodeWithText("Your Reservations")
            .assertExists()
    }

    /** Waits for a newly created entrant's empty conversation list to finish loading. */
    private fun waitForEmptyMessages() {
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText("No conversations yet.")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    /** Seeds one minimal conversation as Firestore profile and campsite records. */
    private fun seedConversationForSignedInEntrant() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val entrantId = runBlocking { AuthManager(context).getCurrentUserId() }
            ?: error("The sign-up flow did not save the entrant profile ID")

        val entrantDocument = firestore.collection("profiles").document(entrantId)
        val entrant = await(entrantDocument.get()).toObject(Entrant::class.java)
            ?: error("The signed-in entrant profile was not saved to Firestore")

        val owner = CampOwner(
            name = "John Smith",
            email = "owner-test-${UUID.randomUUID()}@example.com",
            phone = "7805550100"
        )
        createdOwnerIds.add(owner.id)

        val campsite = Campsite(
            name = "Pine Lake Campsite",
            location = "Jasper, Alberta",
            description = "A test campsite stored in Firestore.",
            capacity = 4,
            mapLocation = "53.6015, -112.8683",
            campOwner = owner,
            id = UUID.randomUUID().toString()
        )
        createdCampsiteIds.add(campsite.id)

        val conversation = Conversation(
            campOwner = owner,
            entrant = entrant,
            campsite = campsite
        )

        await(firestore.collection("profiles").document(owner.id).set(
            owner.copy(conversations = mutableListOf(conversation))
        ))
        await(firestore.collection("campsites").document(campsite.id).set(campsite))
        await(entrantDocument.set(entrant.copy(conversations = mutableListOf(conversation))))
    }

    /** Writes two real reservation records onto the signed-in entrant profile in Firestore. */
    private fun seedReservationsForSignedInEntrant(): List<Reservation> {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val entrantId = runBlocking { AuthManager(context).getCurrentUserId() }
            ?: error("The sign-up flow did not save the entrant profile ID")
        val entrantDocument = firestore.collection("profiles").document(entrantId)
        val entrant = await(entrantDocument.get()).toObject(Entrant::class.java)
            ?: error("The signed-in entrant profile was not saved to Firestore")

        val reservations = listOf(
            Triple("North Lake Reservation", "Jasper, Alberta", "2026-09-10 - 2026-09-12"),
            Triple("South Ridge Reservation", "Banff, Alberta", "2026-10-05 - 2026-10-07")
        ).mapIndexed { index, (campsiteName, location, dates) ->
            val owner = CampOwner(
                name = if (index == 0) "Owner North" else "Owner South",
                email = "reservation-owner-${UUID.randomUUID()}@example.com",
                phone = "78055501${index}"
            )
            createdOwnerIds.add(owner.id)

            val campsite = Campsite(
                name = campsiteName,
                location = location,
                description = "Reservation test campsite $index.",
                capacity = 4,
                mapLocation = location,
                campOwner = owner,
                id = UUID.randomUUID().toString()
            )
            createdCampsiteIds.add(campsite.id)

            await(firestore.collection("profiles").document(owner.id).set(owner))
            await(firestore.collection("campsites").document(campsite.id).set(campsite))

            Reservation(
                campsite = campsite,
                dates = dates,
                guestNumber = if (index == 0) 2 else 4,
                status = ReservationStatus.Active,
                campOwner = owner,
                entrants = emptyList()
            )
        }

        await(entrantDocument.set(entrant.copy(reservations = reservations)))
        return reservations
    }

    // The following function was added using OpenAI, Codex, "Add instrumented tests for accepting
    // and declining lottery offers", 2026-11-06.
    /** Adds one lottery offer that the current entrant has not accepted yet. */
    private fun seedPendingOfferForSignedInEntrant(
        campsiteName: String
    ): Reservation {
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

    // The following function was added using OpenAI, Codex, "Add instrumented tests for accepting
    // and declining lottery offers", 2026-11-06.
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

    private fun assertNoDemoConversations() {
        waitForEmptyMessages()
        listOf(
            "Pine Lake Campsite",
            "Mountain View Camp",
            "Forest Hill Camp",
            "Riverside Campsite"
        ).forEach { campsite ->
            composeTestRule.onNodeWithText(campsite).assertDoesNotExist()
        }
    }

    private fun sendMessage(text: String) {
        composeTestRule.onNodeWithText("Type a message...")
            .performTextInput(text)
        composeTestRule.onNodeWithText("➜")
            .performClick()
        composeTestRule.onNodeWithText(text)
            .assertExists()
    }

    // TEST 1: Enter sign-up information
    @Test
    fun test1_EnterInformation() {

        enterInformation()

        composeTestRule.onNodeWithTag("firstNameInput")
            .assertTextContains("David")

        composeTestRule.onNodeWithTag("lastNameInput")
            .assertTextContains("Test")

        composeTestRule.onNodeWithTag("emailInput")
            .assertTextContains(testEmail)

        composeTestRule.onNodeWithTag("phoneInput")
            .assertTextContains("7801234567")
    }

    // TEST 2: Sign up as Entrant
    @Test
    fun test2_SignUpAsEntrant() {

        openHome()

        composeTestRule.onNodeWithText("Home")
            .assertExists()
    }

    // TEST 3: Check Home notifications
    @Test
    fun test3_HomeNotifications() {

        openHome()

        composeTestRule.onNodeWithText("Notifications")
            .assertExists()

        composeTestRule.onNodeWithText("You Won!")
            .assertExists()

        composeTestRule.onNodeWithText("Reservation Confirmed")
            .assertExists()

        composeTestRule.onNodeWithText("Booking Window Closing!")
            .assertExists()

        composeTestRule.onNodeWithText("Waitlist at Max Capacity")
            .assertExists()

        composeTestRule.onNodeWithText("Lost Lottery")
            .assertExists()
    }

    // TEST 4: Open Policies
    @Test
    fun test4_OpenPolicies() {

        openHome()

        composeTestRule.onNodeWithText("Policies & Guidelines")
            .performClick()

        composeTestRule.onNodeWithText(
            "What Data We Collect",
            substring = true
        ).assertExists()
    }

    // TEST 5: Back from Policies
    @Test
    fun test5_BackFromPolicies() {

        openHome()

        composeTestRule.onNodeWithText("Policies & Guidelines")
            .performClick()

        composeTestRule.onNodeWithText("Back")
            .performClick()

        composeTestRule.onNodeWithText("Home")
            .assertExists()
    }

    // TEST 7: Navigate from Messages back to Home using the bottom bar
    @Test
    fun test7_BackFromMessages() {
        openMessages()
        composeTestRule.onNodeWithContentDescription("Home")
            .performClick()
        composeTestRule.onNodeWithText("Home")
            .assertExists()
    }

    // TEST 8: Open a Firestore-backed conversation and send a message
    @Test
    fun test8_SendMessageInFirestoreBackedConversation() {
        openHome()
        seedConversationForSignedInEntrant()
        clickMessages()

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText("Pine Lake Campsite")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Pine Lake Campsite")
            .performClick()
        composeTestRule.onNodeWithText("John Smith (Campsite Owner)")
            .assertExists()

        sendMessage("Hello John, I am excited!")
    }

    // TEST 13: Messages can be reopened after navigating Home
    @Test
    fun test13_ReopenMessagesAfterHomeNavigation() {
        openMessages()
        composeTestRule.onNodeWithContentDescription("Home")
            .performClick()
        composeTestRule.onNodeWithContentDescription("Messages")
            .performClick()
        composeTestRule.onNodeWithText("Messages")
            .assertExists()
        assertNoDemoConversations()
    }

    // TEST 14: An empty messages list shows no conversation or reservation actions
    @Test
    fun test14_EmptyMessagesListHasNoConversationOrReservationActions() {
        openMessages()
        assertNoDemoConversations()

        composeTestRule.onNodeWithText("No conversations yet.").assertExists()
        composeTestRule.onNodeWithText("View Application →").assertDoesNotExist()
        composeTestRule.onNodeWithText("Your Reservations").assertDoesNotExist()
        composeTestRule.onNodeWithText("Message").assertDoesNotExist()
        composeTestRule.onNodeWithText("Type a message...").assertDoesNotExist()
    }

    // TEST 15: Render all reservations saved on the signed-in entrant's Firestore profile
    @Test
    fun test15_ShowsReservationFieldsFromSignedInEntrantProfile() {
        openHome()
        seedReservationsForSignedInEntrant()
        openEntrantReservations()

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText("North Lake Reservation")
                .fetchSemanticsNodes().isNotEmpty() &&
                    composeTestRule.onAllNodesWithText("South Ridge Reservation")
                        .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("North Lake Reservation").assertExists()
        composeTestRule.onNodeWithText("Jasper, Alberta").assertExists()
        composeTestRule.onNodeWithText("2026-09-10 - 2026-09-12").assertExists()
        composeTestRule.onNodeWithText("2").assertExists()
        composeTestRule.onNodeWithText("Owner North").assertExists()

        composeTestRule.onNodeWithText("South Ridge Reservation").assertExists()
        composeTestRule.onNodeWithText("Banff, Alberta").assertExists()
        composeTestRule.onNodeWithText("2026-10-05 - 2026-10-07").assertExists()
        composeTestRule.onNodeWithText("4").assertExists()
        composeTestRule.onNodeWithText("Owner South").assertExists()
        composeTestRule.onAllNodesWithText("Campsite Name").assertCountEquals(2)
    }

    // TEST 16: Open the owner associated with the selected reservation
    @Test
    fun test16_MessageButtonOpensSelectedReservationOwner() {
        openHome()
        seedReservationsForSignedInEntrant()
        openEntrantReservations()

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText("South Ridge Reservation")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithContentDescription("Message Owner South")
            .performScrollTo()
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText("Owner South (Campsite Owner)")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Owner South (Campsite Owner)").assertExists()
    }

    // The following test was modified using OpenAI, Codex, "Align the entrant reservations empty-
    // state test with the Active tab", 2026-11-06.
    // TEST 17: Show an empty state when the signed-in entrant has no reservations
    @Test
    fun test17_ShowsNoReservationsForEmptyEntrantProfile() {
        openHome()
        openEntrantReservations()

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText("No active reservations.")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("No active reservations.").assertExists()
    }

    // The following test was added using OpenAI, Codex, "Add an instrumented test that accepts a
    // pending lottery offer and persists Active status", 2026-11-06.
    // TEST 18: Accepting a lottery offer changes its saved status to Active.
    @Test
    fun test18_AcceptLotteryOfferMovesItToActiveReservations() {
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

    // The following test was added using OpenAI, Codex, "Add an instrumented test that confirms a
    // declined lottery offer, persists Rejected status, and removes it from Pending", 2026-11-06.
    // TEST 19: Confirming a declined lottery offer rejects it and removes it from Pending.
    @Test
    fun test19_DeclineLotteryOfferRejectsAndRemovesItFromPending() {
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
