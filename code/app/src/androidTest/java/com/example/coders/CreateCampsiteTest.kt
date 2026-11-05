package com.example.coders

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CreateCampsiteTest {

    private val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val testRule: TestRule = freshSessionRule(composeTestRule)

   // wait until display of signup
    private fun waitForSignUpScreen() {
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithTag("firstNameInput").fetchSemanticsNodes().isNotEmpty()
        }
    }

    // fill out the sign-up form and select camp owner role
    private fun signUpAsCampOwner() {
        waitForSignUpScreen()

        composeTestRule.onNodeWithTag("firstNameInput")
            .performTextInput("John")

        composeTestRule.onNodeWithTag("lastNameInput")
            .performTextInput("Smith")

        composeTestRule.onNodeWithTag("emailInput")
            .performTextInput("john.smith@example.com")

        composeTestRule.onNodeWithTag("phoneInput")
            .performTextInput("7805550199")

        composeTestRule.onNodeWithText("Campsite owner")
            .performClick()

        composeTestRule.onNodeWithText("Sign Up")
            .performClick()


        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText("Campsite Owner Home").fetchSemanticsNodes().isNotEmpty()
        }
    }

    //goes from homescreen to the create campsite screen
    private fun navigateToCreateCampsite() {
        signUpAsCampOwner()

        composeTestRule.onNodeWithContentDescription("My Stuff")
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("My Campsites").fetchSemanticsNodes().isNotEmpty()
        }


        composeTestRule.onNodeWithText("My Campsites")
            .performClick()

        composeTestRule.onNodeWithText("Create Campsite")
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Confirm Campsite").fetchSemanticsNodes().isNotEmpty()
        }
    }

    // info collection screen shows up properly
    @Test
    fun test1_CreateCampsiteScreenLoads() {
        navigateToCreateCampsite()

        // all the required fields are there
        composeTestRule.onNodeWithText("Create Campsite").assertIsDisplayed()
        composeTestRule.onNodeWithText("Enter Campsite Name").assertExists()
        composeTestRule.onNodeWithText("Enter Campsite Location").assertExists()
        composeTestRule.onNodeWithText("Enter Campsite Description").assertExists()
        composeTestRule.onNodeWithText("Enter Maximum Occupancy").assertExists()
        composeTestRule.onNodeWithText("Confirm Campsite").assertExists()
    }

    // check missing required info pops up properly
    @Test
    fun test2_MissingInfoShowsErrorDialog() {
        navigateToCreateCampsite()

        composeTestRule.onNodeWithText("Confirm Campsite")
            .performClick()

        composeTestRule.onNodeWithText("Missing Information")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("You did not provide all required information (Name, Location, Description, and Occupancy).")
            .assertIsDisplayed()
    }

    // clicking 'retry' returns to right screen
    @Test
    fun test3_ErrorDialogRetryButtonReturnsToForm() {
        navigateToCreateCampsite()


        composeTestRule.onNodeWithText("Confirm Campsite")
            .performClick()


        composeTestRule.onNodeWithText("Retry")
            .performClick()


        composeTestRule.onNodeWithText("Missing Information")
            .assertDoesNotExist()
        composeTestRule.onNodeWithText("Confirm Campsite")
            .assertIsDisplayed()
    }

    // clicking "Cancel" on the error returns to prev screen
    @Test
    fun test4_ErrorDialogCancelButtonNavigatesBack() {
        navigateToCreateCampsite()

        composeTestRule.onNodeWithText("Confirm Campsite")
            .performClick()


        composeTestRule.onNodeWithText("Cancel")
            .performClick()

        composeTestRule.onNodeWithText("My Campsites:")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Confirm Campsite")
            .assertDoesNotExist()
    }

    // all required info successfully registers the campsite
    @Test
    fun test5_SuccessfulCampsiteRegistration() {
        navigateToCreateCampsite()


        composeTestRule.onNodeWithTag("input_Name")
            .performTextInput("Bear Creek Site")

        composeTestRule.onNodeWithTag("input_Location")
            .performTextInput("Banff, Alberta")

        composeTestRule.onNodeWithTag("input_Description")
            .performTextInput("A lovely site near the creek.")

        composeTestRule.onNodeWithTag("input_Occupancy")
            .performTextInput("4")

        composeTestRule.onNodeWithText("Confirm Campsite")
            .performClick()


        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("My Campsites:").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("My Campsites:")
            .assertIsDisplayed()


        composeTestRule.onNodeWithText("Bear Creek Site")
            .assertIsDisplayed()
    }
}