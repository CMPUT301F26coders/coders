// The following Test is from OpenAI, Codex, "okay instead of using dummy data in the [EntrantReservationsScreen.kt](code/app/src/main/java/com/example/coders/ui/screens/EntrantReservationsScreen.kt) we need the demo to be acutally working with the backend, so get rid of the in file dummy testing in [EntrantReservationsScreen.kt](code/app/src/main/java/com/example/coders/ui/screens/EntrantReservationsScreen.kt) and instead create a test like [EntrantHomePageTest.kt](code/app/src/androidTest/java/com/example/coders/EntrantHomePageTest.kt)  for the [EntrantSearchScreen.kt](code/app/src/main/java/com/example/coders/ui/screens/EntrantSearchScreen.kt)""
package com.example.coders

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.hasSetTextAction
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
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import java.util.UUID
import java.util.concurrent.TimeUnit

/** Verifies that entrant search displays campsite records delivered by Firestore. */
@RunWith(AndroidJUnit4::class)
class EntrantSearchScreenTest {

    private val composeTestRule = createAndroidComposeRule<MainActivity>()
    private val testEmail = "search-entrant-${UUID.randomUUID()}@example.com"
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
        entrantProfiles.documents.forEach { document -> await(document.reference.delete()) }
        createdOwnerIds.forEach { ownerId ->
            await(firestore.collection("profiles").document(ownerId).delete())
        }
        createdCampsiteIds.forEach { campsiteId ->
            await(firestore.collection("campsites").document(campsiteId).delete())
        }
        runBlocking { AuthManager(context).clearUserId() }
    }

    @Test
    fun searchShowsCampsiteLoadedFromFirestore() {
        signUpAsEntrant()
        val campsite = seedCampsite(
            name = "Backend Pine ${UUID.randomUUID().toString().take(8)}",
            location = "Jasper, Alberta",
            capacity = 4
        )

        openSearch()

        waitForText(campsite.name)
        composeTestRule.onNodeWithText(campsite.name).assertExists()
        composeTestRule.onNodeWithText("Location: Jasper, Alberta").assertExists()
        composeTestRule.onNodeWithText("Maximum Occupancy: 4 guests").assertExists()
    }

    @Test
    fun searchFiltersFirestoreCampsitesByName() {
        signUpAsEntrant()
        val matchingCampsite = seedCampsite(
            name = "Matchable Lake ${UUID.randomUUID().toString().take(8)}",
            location = "Canmore, Alberta",
            capacity = 2
        )
        val otherCampsite = seedCampsite(
            name = "Unrelated Ridge ${UUID.randomUUID().toString().take(8)}",
            location = "Banff, Alberta",
            capacity = 6
        )

        openSearch()

        waitForText(matchingCampsite.name)
        waitForText(otherCampsite.name)
        composeTestRule.onNode(hasSetTextAction())
            .performTextInput(matchingCampsite.name)

        composeTestRule.onNodeWithText(matchingCampsite.name).assertExists()
        composeTestRule.onNodeWithText(otherCampsite.name).assertDoesNotExist()
    }

    private fun signUpAsEntrant() {
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithTag("firstNameInput").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("firstNameInput").performTextInput("Search")
        composeTestRule.onNodeWithTag("lastNameInput").performTextInput("Tester")
        composeTestRule.onNodeWithTag("emailInput").performTextInput(testEmail)
        composeTestRule.onNodeWithTag("phoneInput").performTextInput("7801234567")
        composeTestRule.onNodeWithText("Entrant").performClick()
        composeTestRule.onNodeWithText("Sign Up").performClick()

        waitForText("Home")
    }

    private fun openSearch() {
        composeTestRule.onNodeWithContentDescription("Search").performClick()
        waitForText("Search Campsites")
    }

    private fun seedCampsite(name: String, location: String, capacity: Int): Campsite {
        val owner = CampOwner(
            name = "Search Test Owner",
            email = "search-owner-${UUID.randomUUID()}@example.com",
            phone = "7805550100"
        )
        createdOwnerIds.add(owner.id)

        val campsite = Campsite(
            name = name,
            location = location,
            description = "Campsite inserted by EntrantSearchScreenTest.",
            capacity = capacity,
            mapLocation = "53.5461, -113.4938",
            campOwner = owner,
            rating = 4.5,
            campOwnerId = owner.id
        )
        createdCampsiteIds.add(campsite.id)

        await(firestore.collection("profiles").document(owner.id).set(owner))
        await(firestore.collection("campsites").document(campsite.id).set(campsite))
        return campsite
    }

    private fun waitForText(text: String) {
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun <T> await(task: Task<T>): T = Tasks.await(task, 20, TimeUnit.SECONDS)
}
