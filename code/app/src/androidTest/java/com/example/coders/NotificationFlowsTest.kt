package com.example.coders

import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/** End-to-end checks for notification delivery, lottery results, waitlist messages, and dismissal. */
@RunWith(AndroidJUnit4::class)
class NotificationFlowsTest {

    private val composeTestRule = createAndroidComposeRule<MainActivity>()
    private val testEmail = "notification-flow-${UUID.randomUUID()}@example.com"
    private val firestore = Firebase.firestore
    private val createdDocuments = mutableListOf<DocumentReference>()

    @get:Rule
    val testRule: TestRule = freshSessionRule(composeTestRule)

    /** Removes the Firestore data and local profile ID created by a test. */
    @After
    fun cleanUpFirebaseTestRecords() {
        createdDocuments.distinctBy { it.path }.forEach { document ->
            runCatching { await(document.delete()) }
        }

        runCatching {
            val profiles = await(
                firestore.collection("profiles").whereEqualTo("email", testEmail).get()
            )
            profiles.documents.forEach { document -> await(document.reference.delete()) }
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        runBlocking { AuthManager(context).clearUserId() }
    }

    /** Waits for a Firebase task or fails after the shared integration-test timeout. */
    private fun <T> await(task: Task<T>): T = Tasks.await(task, 20, TimeUnit.SECONDS)

    /** Waits for a repository callback and returns its error, if any. */
    private fun awaitError(
        start: ((Exception?) -> Unit) -> Unit
    ): Exception? {
        val latch = CountDownLatch(1)
        val error = AtomicReference<Exception?>(null)
        start { result ->
            error.set(result)
            latch.countDown()
        }
        assertTrue("Timed out waiting for Firebase write", latch.await(20, TimeUnit.SECONDS))
        return error.get()
    }

    /** Signs up a fresh entrant and waits until its profile-scoped notification listener is ready. */
    private fun openHomeAndGetProfileId(profileType: String = "Entrant"): String {
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText("New User:")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("firstNameInput").performTextInput("Notice")
        composeTestRule.onNodeWithTag("lastNameInput").performTextInput("Test")
        composeTestRule.onNodeWithTag("emailInput").performTextInput(testEmail)
        composeTestRule.onNodeWithTag("phoneInput").performTextInput("7801234567")
        composeTestRule.onNodeWithText(profileType).performClick()
        composeTestRule.onNodeWithText("Sign Up").performClick()

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText("You don't have any notifications yet.")
                .fetchSemanticsNodes().isNotEmpty()
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        return runBlocking { AuthManager(context).getCurrentUserId() }
            ?: error("Sign-up did not save the entrant profile ID")
    }

    @Test
    fun reservationNotificationAppearsForRecipientAndXDeletesIt() {
        val entrantId = openHomeAndGetProfileId()
        val ownerId = UUID.randomUUID().toString()
        val campsiteId = UUID.randomUUID().toString()
        val reservationId = UUID.randomUUID().toString()
        val notificationId = UUID.nameUUIDFromBytes("$reservationId:$entrantId".toByteArray()).toString()
        val reservationDocument = firestore.collection("reservation").document(reservationId)
        val notificationDocument = firestore.collection("notification").document(notificationId)
        createdDocuments += listOf(reservationDocument, notificationDocument)

        val hiddenNotification = Notification(
            title = "Notification for another entrant",
            content = "This must not appear on this profile.",
            recipientId = UUID.randomUUID().toString()
        )
        val hiddenDocument = firestore.collection("notification").document(hiddenNotification.id)
        createdDocuments += hiddenDocument
        val notificationRepo = NotificationRepo()
        assertNull(awaitError { notificationRepo.addNotification(hiddenNotification, it) })
        notificationRepo.close()

        val reservation = Reservation(
            campsite = Campsite(id = campsiteId, name = "Notification Test Campsite"),
            dates = "2026-12-10 - 2026-12-12",
            id = reservationId,
            campsiteId = campsiteId,
            campOwnerId = ownerId,
            entrantIds = listOf(entrantId)
        )
        assertNull(awaitError { ReservationRepo().addReservation(reservation, it) })

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText("Reservation Received")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(
            "A reservation is available for Notification Test Campsite (2026-12-10 - 2026-12-12)."
        ).assertExists()
        composeTestRule.onNodeWithText("Notification for another entrant").assertDoesNotExist()

        composeTestRule.onNodeWithContentDescription("Clear notification").performClick()
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText("Reservation Received")
                .fetchSemanticsNodes().isEmpty()
        }

        assertFalse("Dismissing the card should delete its Firestore record", await(notificationDocument.get()).exists())
        assertNotNull("The reservation record should remain after its notice is dismissed", await(reservationDocument.get()).toObject(Reservation::class.java))
    }

    @Test
    fun lotteryDrawCreatesOneWinnerNoticeAndOneLoserNotice() {
        openHomeAndGetProfileId()
        val ownerId = UUID.randomUUID().toString()
        val lotteryId = UUID.randomUUID().toString()
        val entrantIds = listOf(UUID.randomUUID().toString(), UUID.randomUUID().toString())
        val lotteryDocument = firestore.collection("lottery").document(lotteryId)
        createdDocuments += lotteryDocument
        val notificationDocuments = entrantIds.map { entrantId ->
            firestore.collection("notification").document(
                UUID.nameUUIDFromBytes("lottery-result:$lotteryId:$entrantId".toByteArray()).toString()
            )
        }
        createdDocuments += notificationDocuments

        await(
            lotteryDocument.set(
                Lottery(
                    id = lotteryId,
                    campOwnerId = ownerId,
                    registrationEndDate = "2000-01-01",
                    waitlistEntrantIds = entrantIds,
                    winnerCount = 1
                )
            )
        )

        assertNull(awaitError { LotteryRepo().drawWinners(lotteryId, ownerId, it) })

        val savedLottery = await(lotteryDocument.get()).toObject(Lottery::class.java)
        assertNotNull(savedLottery)
        assertTrue(savedLottery!!.drawCompleted)
        assertEquals(1, savedLottery.winnerEntrantIds.size)
        assertTrue(entrantIds.containsAll(savedLottery.winnerEntrantIds))

        val notices = notificationDocuments.map { await(it.get()).toObject(Notification::class.java) }
        assertTrue(notices.all { it != null })
        assertEquals(1, notices.count { it?.title == "Lottery Won!" })
        assertEquals(1, notices.count { it?.title == "Lottery Result" })
        notices.forEachIndexed { index, notice ->
            assertEquals(entrantIds[index], notice!!.recipientId)
            assertEquals(ownerId, notice.senderId)
        }
    }

    @Test
    fun ownerWaitlistMessageIsDeliveredToEveryEntrant() {
        val ownerId = openHomeAndGetProfileId("Campsite owner")
        val lotteryId = UUID.randomUUID().toString()
        val entrantIds = listOf(UUID.randomUUID().toString(), UUID.randomUUID().toString())
        val lotteryDocument = firestore.collection("lottery").document(lotteryId)
        createdDocuments += lotteryDocument
        await(
            lotteryDocument.set(
                Lottery(
                    id = lotteryId,
                    campOwnerId = ownerId,
                    waitlistEntrantIds = entrantIds
                )
            )
        )

        val latch = CountDownLatch(1)
        val recipientCount = AtomicReference<Int?>(null)
        val error = AtomicReference<Exception?>(null)
        LotteryRepo().sendWaitlistNotification(
            ownerId = ownerId,
            lotteryId = lotteryId,
            title = " Lottery update ",
            content = " Please check the updated booking dates. ",
            onComplete = { count, result ->
                recipientCount.set(count)
                error.set(result)
                latch.countDown()
            }
        )
        assertTrue("Timed out sending waitlist message", latch.await(20, TimeUnit.SECONDS))
        assertNull(error.get())
        assertEquals(entrantIds.size, recipientCount.get())

        val sent = await(
            firestore.collection("notification").whereEqualTo("senderId", ownerId).get()
        )
        createdDocuments += sent.documents.map { it.reference }
        assertEquals(entrantIds.toSet(), sent.documents.mapNotNull { document ->
            document.toObject(Notification::class.java)?.let { notification ->
                assertEquals("Lottery update", notification.title)
                assertEquals("Please check the updated booking dates.", notification.content)
                notification.recipientId
            }
        }.toSet())

        val rejectedLatch = CountDownLatch(1)
        val rejectedError = AtomicReference<Exception?>(null)
        LotteryRepo().sendWaitlistNotification(
            ownerId = UUID.randomUUID().toString(),
            lotteryId = lotteryId,
            title = "Unauthorized message",
            content = "This must not be sent.",
            onComplete = { _, result ->
                rejectedError.set(result)
                rejectedLatch.countDown()
            }
        )
        assertTrue("Timed out checking waitlist ownership", rejectedLatch.await(20, TimeUnit.SECONDS))
        assertTrue(rejectedError.get() is SecurityException)
    }
}
