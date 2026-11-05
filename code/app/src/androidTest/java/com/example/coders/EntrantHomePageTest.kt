
package com.example.coders

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EntrantHomePageTest {

    private val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val testRule: TestRule = freshSessionRule(composeTestRule)

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
            .performTextInput("test@example.com")

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

        composeTestRule.onNodeWithContentDescription("Messages")
            .performClick()

        composeTestRule.onNodeWithText("Messages")
            .assertExists()
    }

    /** Waits for a newly created entrant's empty conversation list to finish loading. */
    private fun waitForEmptyMessages() {
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText("No conversations yet.")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    /** Asserts that the removed demo conversations are not displayed. */
    private fun assertNoDemoConversations() {
        waitForEmptyMessages()
        composeTestRule.onNodeWithText("No conversations yet.").assertExists()
        listOf(
            "Pine Lake Campsite",
            "Mountain View Camp",
            "Forest Hill Camp",
            "Riverside Campsite"
        ).forEach { campsite ->
            composeTestRule.onNodeWithText(campsite).assertDoesNotExist()
        }
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
            .assertTextContains("test@example.com")

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

    // TEST 6: A new entrant has no conversations
    @Test
    fun test6_OpenMessages() {
        openMessages()
        assertNoDemoConversations()
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

    // TEST 8: No hard-coded John Smith chat is shown
    @Test
    fun test8_NoHardCodedJohnChat() {
        openMessages()
        assertNoDemoConversations()
        composeTestRule.onNodeWithText("John Smith").assertDoesNotExist()
    }

    // TEST 9: No sample chat is available to message John
    @Test
    fun test9_EmptyListHasNoChatComposer() {
        openMessages()
        assertNoDemoConversations()
        composeTestRule.onNodeWithText("Type a message...").assertDoesNotExist()
    }

    // TEST 10: No sample chat is available to message Sarah
    @Test
    fun test10_NoHardCodedSarahChat() {
        openMessages()
        assertNoDemoConversations()
        composeTestRule.onNodeWithText("Sarah Lee").assertDoesNotExist()
    }

    // TEST 11: No sample chat is available to message Mike
    @Test
    fun test11_NoHardCodedMikeChat() {
        openMessages()
        assertNoDemoConversations()
        composeTestRule.onNodeWithText("Mike Brown").assertDoesNotExist()
    }

    // TEST 12: No sample chat is available to message Emma
    @Test
    fun test12_NoHardCodedEmmaChat() {
        openMessages()
        assertNoDemoConversations()
        composeTestRule.onNodeWithText("Emma Wilson").assertDoesNotExist()
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

    // TEST 14: The list does not show a campsite-name-based application action
    @Test
    fun test14_NoCampsiteNameBasedApplicationAction() {
        openMessages()
        assertNoDemoConversations()
        composeTestRule.onNodeWithText("View Application →").assertDoesNotExist()
    }

    // TEST 15: The Messages screen does not open sample reservation details
    @Test
    fun test15_NoSampleReservationDetails() {
        openMessages()
        assertNoDemoConversations()
        composeTestRule.onNodeWithText("Your Reservations").assertDoesNotExist()
    }

    // TEST 16: The removed sample application action cannot open a reservation chat
    @Test
    fun test16_NoSampleReservationMessageAction() {
        openMessages()
        assertNoDemoConversations()
        composeTestRule.onNodeWithText("Message").assertDoesNotExist()
    }

    // TEST 17: No sample reservation or chat screen is opened from Messages
    @Test
    fun test17_NoSampleReservationOrChatScreen() {
        openMessages()
        assertNoDemoConversations()
        composeTestRule.onNodeWithText("Your Reservations").assertDoesNotExist()
        composeTestRule.onNodeWithText("Type a message...").assertDoesNotExist()
    }

    // TEST 18: The empty state has no message composer until a conversation is selected
    @Test
    fun test18_EmptyListHasNoMessageComposer() {
        openMessages()
        assertNoDemoConversations()
        composeTestRule.onNodeWithText("Type a message...").assertDoesNotExist()
    }
}
