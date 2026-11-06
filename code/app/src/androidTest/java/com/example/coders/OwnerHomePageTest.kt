
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.example.coders.ui.screens.OwnerCampsitesScreen
import androidx.compose.material3.Text
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.coders.ui.screens.MessagesScreen
import androidx.compose.ui.test.performTextInput
import com.example.coders.ui.screens.ChatScreen


@RunWith(AndroidJUnit4::class)
class OwnerHomePageTest {


    @get:Rule
    val composeTestRule = createComposeRule()

    private val firestore = Firebase.firestore
    private val ownerId = UUID.randomUUID().toString()
    private val otherOwnerId = UUID.randomUUID().toString()

    private val createdCampsiteIds = mutableListOf<String>()
    private val createdReservationIds = mutableListOf<String>()
    private val createdConversationIds = mutableListOf<String>()

    private fun <T> await(task: Task<T>): T =
        Tasks.await(task, 20, TimeUnit.SECONDS)

    private val createdEntrantIds = mutableListOf<String>()


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

        createdCampsiteIds.forEach { id ->
            await(
                firestore.collection("campsites")
                    .document(id)
                    .delete()
            )
        }

        createdEntrantIds.forEach { id ->
            await(
                firestore.collection("profiles")
                    .document(id)
                    .delete()
            )
        }

        createdConversationIds.forEach { id ->
            await(
                firestore.collection("conversation")
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

    private fun seedConversationForSignedInOwner() {


        val entrant = Entrant(
            name = "Alice Smith",
            email = "test-entrant-${UUID.randomUUID()}@example.com",
            phone = "7805550100"
        )

        createdEntrantIds.add(entrant.id)

        val owner = CampOwner(
            id = ownerId,
            name = "Test Camp Owner",
            email = "owner@test.com",
            phone = "7801234567"
        )

        val campsite = Campsite(
            name = "Pine Lake Campsite",
            location = "Jasper, Alberta",
            description = "Test campsite",
            capacity = 123,
            mapLocation = "Jasper",
            campOwner = owner,
            campOwnerId = ownerId
        )

        val conversation = Conversation(
            campOwner = owner,
            entrant = entrant,
            campsite = campsite,
            messages = listOf(
                Message(
                    sender = entrant,
                    content = "Hello, is this campsite available?",
                    date = "2026-11-05"
                )
            ),
            campOwnerId = ownerId,
            entrantId = entrant.id,
            campsiteId = campsite.id,
            participantIds = listOf(ownerId, entrant.id)
        )

        createdConversationIds.add(conversation.id)


        await(
            firestore.collection("conversation")
                .document(conversation.id)
                .set(conversation)
        )

        await(
            firestore.collection("profiles")
                .document(entrant.id)
                .set(entrant)
        )



        await(
            firestore.collection("profiles")
                .document(ownerId)
                .set(
                    owner.copy(
                        conversations = mutableListOf(conversation)
                    )
                )
        )
    }

    private fun seedCampsite(
        name: String,
        location: String,
        capacity: Int,
        campOwnerId: String = ownerId
    ): String {

        val campsiteId = UUID.randomUUID().toString()
        createdCampsiteIds.add(campsiteId)

        await(
            firestore.collection("campsites")
                .document(campsiteId)
                .set(
                    mapOf(
                        "id" to campsiteId,
                        "name" to name,
                        "location" to location,
                        "description" to "Test campsite",
                        "capacity" to capacity,
                        "mapLocation" to location,
                        "campOwnerId" to campOwnerId
                    )
                )
        )

        return campsiteId
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
                        "guestNumber" to 123,
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
            "Guests: 123"
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

    @Test
    fun test7_ShowsOwnerCampsites() {

        seedCampsite(
            name = "Edmonton Test Camp",
            location = "Edmonton",
            capacity = 123
        )

        seedCampsite(
            name = "Calgary Test Camp",
            location = "Calgary",
            capacity = 8
        )

        composeTestRule.setContent {
            val campsiteRepo = remember { CampsiteRepo() }

            DisposableEffect(campsiteRepo) {
                onDispose {
                    campsiteRepo.close()
                }
            }

            OwnerCampsitesScreen(
                navController = rememberNavController(),
                campsiteRepo = campsiteRepo
            )
        }

        // Verify page opened
        composeTestRule.onNodeWithText(
            "My Campsites:"
        ).assertExists()

        // Wait for Firestore to load campsites
        composeTestRule.waitUntil(10_000) {
            composeTestRule.onAllNodesWithText(
                "Edmonton Test Camp"
            ).fetchSemanticsNodes().isNotEmpty() &&
                    composeTestRule.onAllNodesWithText(
                        "Calgary Test Camp"
                    ).fetchSemanticsNodes().isNotEmpty()
        }

        // Verify Edmonton campsite
        composeTestRule.onNodeWithText(
            "Edmonton Test Camp"
        ).assertExists()

        composeTestRule.onNodeWithText(
            "Edmonton"
        ).assertExists()

        composeTestRule.onNodeWithText(
            "123 guests"
        ).assertExists()

        // Verify Calgary campsite
        composeTestRule.onNodeWithText(
            "Calgary Test Camp"
        ).assertExists()

        composeTestRule.onNodeWithText(
            "Calgary"
        ).assertExists()

        composeTestRule.onNodeWithText(
            "8 guests"
        ).assertExists()
    }


    @Test
    fun test8_OwnerCanSendMessage() {

        // Create a conversation in Firestore
        seedConversationForSignedInOwner()

        composeTestRule.setContent {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = "messages"
            ) {
                composable("messages") {
                    MessagesScreen(navController)
                }

                composable("chat") {
                    ChatScreen(navController)
                }
            }
        }

        // Wait for conversation preview
        composeTestRule.waitUntil(10_000) {
            composeTestRule.onAllNodesWithText(
                "Pine Lake Campsite"
            ).fetchSemanticsNodes().isNotEmpty()
        }

        // Verify conversation information
        composeTestRule.onNodeWithText(
            "Alice Smith"
        ).assertExists()

        composeTestRule.onNodeWithText(
            "Hello, is this campsite available?"
        ).assertExists()

        // Open the conversation
        composeTestRule.onNodeWithText(
            "Pine Lake Campsite"
        ).performClick()

        // Verify the chat has loaded its saved message
        composeTestRule.waitUntil(10_000) {
            composeTestRule.onAllNodesWithText(
                "Hello, is this campsite available?"
            ).fetchSemanticsNodes().isNotEmpty()
        }

        // Type a response
        composeTestRule.onNodeWithText(
            "Type a message..."
        ).performTextInput(
            "Yes Alice, the campsite is available!"
        )

        // Send it
        composeTestRule.onNodeWithText(
            "➜"
        ).performClick()

        // Wait for Firestore listener to display it
        composeTestRule.waitUntil(10_000) {
            composeTestRule.onAllNodesWithText(
                "Yes Alice, the campsite is available!"
            ).fetchSemanticsNodes().isNotEmpty()
        }

        // Verify the message was saved in Firestore
        val conversationId = createdConversationIds.first()

        val document = await(
            firestore.collection("conversation")
                .document(conversationId)
                .get()
        )

        val savedMessages =
            document.get("messages") as? List<*>
                ?: emptyList<Any>()

        val messageWasSaved = savedMessages.any { item ->
            val data = item as? Map<*, *>
            data?.get("content") ==
                    "Yes Alice, the campsite is available!" &&
                    data["senderId"] == ownerId
        }

        org.junit.Assert.assertTrue(
            "Sent message was not saved in Firestore",
            messageWasSaved
        )
    }


}
