package com.example.coders

import androidx.compose.ui.test.onAllNodesWithText
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

/** UI tests for required profile fields on the sign-up screen. */
@RunWith(AndroidJUnit4::class)
class SignUpValidationTest {

    private val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val testRule: TestRule = freshSessionRule(composeTestRule)

    /** Waits for startup routing to finish before interacting with the form. */
    private fun waitForSignUpScreen() {
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText("Sign Up").fetchSemanticsNodes().isNotEmpty()
        }
    }

    /** Empty submission displays every missing text-field message and stays on the form. */
    @Test
    fun emptySubmissionShowsRequiredFieldErrors() {
        waitForSignUpScreen()
        composeTestRule.onNodeWithText("Sign Up").performClick()

        composeTestRule.onNodeWithText("First name is empty", substring = true).assertExists()
        composeTestRule.onNodeWithText("Last name is empty", substring = true).assertExists()
        composeTestRule.onNodeWithText("Email is empty", substring = true).assertExists()
        composeTestRule.onNodeWithText("Phone is empty", substring = true).assertExists()
        composeTestRule.onNodeWithText("Home").assertDoesNotExist()
    }

    /** A partially completed profile reports only the field that remains empty. */
    @Test
    fun partialSubmissionReportsOnlyMissingFields() {
        waitForSignUpScreen()
        composeTestRule.onNodeWithText("Sign Up").performClick()

        composeTestRule.onNodeWithText("First name is empty", substring = true).assertExists()
        composeTestRule.onNodeWithText("Email is empty", substring = true).assertExists()
        composeTestRule.onNodeWithText("Last name is empty", substring = true).assertExists()
        composeTestRule.onNodeWithText("Phone is empty", substring = true).assertExists()

        composeTestRule.onNodeWithTag("firstNameInput").performTextInput("Alex")
        composeTestRule.onNodeWithText("Sign Up").performClick()

        composeTestRule.onNodeWithText("First name is empty", substring = true).assertDoesNotExist()
        composeTestRule.onNodeWithText("Last name is empty", substring = true).assertExists()
        composeTestRule.onNodeWithText("Email is empty", substring = true).assertExists()
        composeTestRule.onNodeWithText("Phone is empty", substring = true).assertExists()
        composeTestRule.onNodeWithText("Home").assertDoesNotExist()
    }
}
