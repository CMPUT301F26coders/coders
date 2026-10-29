
package com.example.coders

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EntrantHomePageTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    // Fill out the sign-up form
    private fun enterInformation() {

        composeTestRule.onNodeWithTag("firstNameInput")
            .performTextInput("David")

        composeTestRule.onNodeWithTag("lastNameInput")
            .performTextInput("Test")

        composeTestRule.onNodeWithTag("emailInput")
            .performTextInput("test@example.com")

        composeTestRule.onNodeWithTag("phoneInput")
            .performTextInput("7801234567")
    }

    // Sign up as an Entrant
    private fun openHome() {

        enterInformation()

        composeTestRule.onNodeWithText("Entrant")
            .performClick()

        composeTestRule.onNodeWithText("Sign Up")
            .performClick()

        composeTestRule.onNodeWithText("Home")
            .assertExists()
    }

    // Open the Messages screen
    private fun openMessages() {

        openHome()

        composeTestRule.onNodeWithContentDescription("Messages")
            .performClick()

        composeTestRule.onNodeWithText("Messages")
            .assertExists()
    }

    // Type a message and click Send
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

    // TEST 6: Open Messages
    @Test
    fun test6_OpenMessages() {

        openMessages()

        composeTestRule.onNodeWithText("Pine Lake Campsite")
            .assertExists()

        composeTestRule.onNodeWithText("Mountain View Camp")
            .assertExists()

        composeTestRule.onNodeWithText("Forest Hill Camp")
            .assertExists()

        composeTestRule.onNodeWithText("Riverside Campsite")
            .assertExists()
    }

    // TEST 7: Back from Messages
    @Test
    fun test7_BackFromMessages() {

        openMessages()

        composeTestRule.onNodeWithText("Back")
            .performClick()

        composeTestRule.onNodeWithText("Home")
            .assertExists()
    }

    // TEST 8: Open John Smith's chat
    @Test
    fun test8_OpenJohnChat() {

        openMessages()

        composeTestRule.onNodeWithText("Pine Lake Campsite")
            .performClick()

        composeTestRule.onNodeWithText(
            "John Smith (Campsite Owner)"
        ).assertExists()

        composeTestRule.onNodeWithText(
            "Hi! Thanks for applying to Pine Lake Campsite."
        ).assertExists()
    }

    // TEST 9: Send a message to John Smith
    @Test
    fun test9_SendMessageToJohn() {

        openMessages()

        composeTestRule.onNodeWithText("Pine Lake Campsite")
            .performClick()

        sendMessage("Hello John, I am excited!")
    }

    // TEST 10: Send a message to Sarah Lee
    @Test
    fun test10_SendMessageToSarah() {

        openMessages()

        composeTestRule.onNodeWithText("Mountain View Camp")
            .performClick()

        composeTestRule.onNodeWithText(
            "Sarah Lee (Campsite Owner)"
        ).assertExists()

        sendMessage("Hello Sarah, is parking available?")
    }

    // TEST 11: Send a message to Mike Brown
    @Test
    fun test11_SendMessageToMike() {

        openMessages()

        composeTestRule.onNodeWithText("Forest Hill Camp")
            .performClick()

        composeTestRule.onNodeWithText(
            "Mike Brown (Campsite Owner)"
        ).assertExists()

        sendMessage("Hello Mike, are pets allowed?")
    }

    // TEST 12: Send a message to Emma Wilson
    @Test
    fun test12_SendMessageToEmma() {

        openMessages()

        composeTestRule.onNodeWithText("Riverside Campsite")
            .performClick()

        composeTestRule.onNodeWithText(
            "Emma Wilson (Campsite Owner)"
        ).assertExists()

        sendMessage("Hello Emma, what time is check-in?")
    }

    // TEST 13: Back from Chat
    @Test
    fun test13_BackFromChat() {

        openMessages()

        composeTestRule.onNodeWithText("Pine Lake Campsite")
            .performClick()

        composeTestRule.onNodeWithText("Back")
            .performClick()

        composeTestRule.onNodeWithText("Messages")
            .assertExists()
    }

    // TEST 14: View Application
    @Test
    fun test14_ViewApplication() {

        openMessages()

        composeTestRule.onNodeWithText("View Application →")
            .performClick()

        composeTestRule.onNodeWithText("Your Reservations")
            .assertExists()

        composeTestRule.onNodeWithText("Jasper, Alberta")
            .assertExists()

        composeTestRule.onNodeWithText("September 10 - 12, 2026")
            .assertExists()

        composeTestRule.onNodeWithText("Guests")
            .assertExists()

        composeTestRule.onNodeWithText("View On Map")
            .assertExists()
    }

    // TEST 15: Back from Reservations
    @Test
    fun test15_BackFromReservations() {

        openMessages()

        composeTestRule.onNodeWithText("View Application →")
            .performClick()

        composeTestRule.onNodeWithText("Back")
            .performClick()

        composeTestRule.onNodeWithText("Messages")
            .assertExists()
    }

    // TEST 16: Message John from Reservations
    @Test
    fun test16_MessageJohnFromReservations() {

        openMessages()

        composeTestRule.onNodeWithText("View Application →")
            .performClick()

        composeTestRule.onNodeWithText("Message")
            .performClick()

        composeTestRule.onNodeWithText(
            "John Smith (Campsite Owner)"
        ).assertExists()

        sendMessage("Hello John, I have a question about my reservation!")
    }

    // TEST 17: Back from John Chat to Reservations
    @Test
    fun test17_BackFromJohnChatToReservations() {

        openMessages()

        composeTestRule.onNodeWithText("View Application →")
            .performClick()

        composeTestRule.onNodeWithText("Message")
            .performClick()

        composeTestRule.onNodeWithText("Back")
            .performClick()

        composeTestRule.onNodeWithText("Your Reservations")
            .assertExists()
    }

    // TEST 18: Send multiple messages in one chat
    @Test
    fun test18_SendMultipleMessages() {

        openMessages()

        composeTestRule.onNodeWithText("Pine Lake Campsite")
            .performClick()

        sendMessage("Hello John!")
        sendMessage("Can I bring my dog?")
        sendMessage("Thank you!")

        composeTestRule.onNodeWithText("Hello John!")
            .assertExists()

        composeTestRule.onNodeWithText("Can I bring my dog?")
            .assertExists()

        composeTestRule.onNodeWithText("Thank you!")
            .assertExists()
    }
}
