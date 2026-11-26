
package com.example.coders.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.time.LocalDate

import com.example.coders.AuthManager
import com.example.coders.Lottery
import com.example.coders.ProfileRepo

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * Represents an entrant displayed on a lottery waitlist.
 *
 * @property id The unique ID of the entrant.
 * @property name The entrant's name.
 * @property joinedDate The date the entrant joined the waitlist.
 */
private data class WaitlistPerson(
    val id: String,
    val name: String,
    val joinedDate: String
)

/**
 * Displays the waitlist for a campsite owner's lottery.
 *
 * Retrieves the lottery and entrant profiles from Firebase, displays
 * waitlist information, and allows the owner to request a winner draw
 * after registration has ended.
 *
 * @param navController Controls navigation between screens.
 * @param lotteryId The unique ID of the lottery being viewed.
 * @param onDrawWinners Callback that requests winner selection and
 * returns an error message if the operation fails.
 */
@Composable
fun OwnerLotteryWaitlistScreen(
    navController: NavController,
    lotteryId: String,
    onDrawWinners: (String, (String?) -> Unit) -> Unit
) {
    /**
     * Initializes Firebase, authentication, and profile helpers.
     */
    val context = LocalContext.current
    val authManager = remember {
        AuthManager(context.applicationContext)
    }
    val profileRepo = remember { ProfileRepo() }
    val db = remember { Firebase.firestore }

    /**
     * Stores the current owner, lottery, waitlist entrants,
     * loading states, and messages displayed on the screen.
     */
    var ownerId by remember { mutableStateOf("") }
    var lottery by remember { mutableStateOf<Lottery?>(null) }

    var people by remember {
        mutableStateOf<List<WaitlistPerson>>(emptyList())
    }

    var loading by remember { mutableStateOf(true) }
    var loadingPeople by remember { mutableStateOf(false) }
    var drawing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var resultMessage by remember { mutableStateOf("") }

    /**
     * Retrieves the signed-in user's profile and owner ID.
     */
    LaunchedEffect(Unit) {
        val userId = authManager.getCurrentUserId()

        if (userId.isNullOrBlank()) {
            errorMessage = "Please sign in."
            loading = false
        } else {
            profileRepo.getProfileById(userId) { profile, error ->
                if (error != null || profile == null) {
                    errorMessage = "Unable to load your profile."
                    loading = false
                } else {
                    ownerId = profile.id
                }
            }
        }
    }

    /**
     * Listens for real-time lottery updates from Firebase.
     *
     * Verifies that the signed-in owner created the lottery
     * and removes the listener when it is no longer needed.
     */
    DisposableEffect(ownerId, lotteryId) {

        val listener = if (ownerId.isNotEmpty()) {
            db.collection("lottery")
                .document(lotteryId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        errorMessage = "Unable to load lottery."
                        loading = false
                        return@addSnapshotListener
                    }

                    val loaded = snapshot
                        ?.toObject(Lottery::class.java)
                        ?.copy(id = snapshot.id)

                    if (loaded == null) {
                        errorMessage = "Lottery not found."
                        loading = false
                        return@addSnapshotListener
                    }

                    if (loaded.campOwnerId != ownerId) {
                        errorMessage = "You do not own this lottery."
                        loading = false
                        return@addSnapshotListener
                    }

                    lottery = loaded
                    errorMessage = ""
                    loading = false
                }
        } else {
            null
        }

        onDispose {
            listener?.remove()
        }
    }

    /**
     * Loads the profiles of entrants currently on the waitlist.
     *
     * Retrieves each entrant's name and join date, then sorts
     * the completed list alphabetically by name.
     */
    val currentLottery = lottery

    LaunchedEffect(
        currentLottery?.waitlistEntrantIds,
        currentLottery?.waitlistJoinedDates
    ) {
        val current = currentLottery ?: return@LaunchedEffect
        val ids = current.waitlistEntrantIds.distinct()

        if (ids.isEmpty()) {
            people = emptyList()
            loadingPeople = false
            return@LaunchedEffect
        }

        loadingPeople = true

        val result = mutableListOf<WaitlistPerson>()
        var finished = 0

        ids.forEach { entrantId ->
            profileRepo.getProfileById(entrantId) { profile, _ ->

                /**
                 * Uses the entrant's name when available and
                 * displays a fallback when the profile is missing.
                 */
                val name = profile?.name ?: "Profile unavailable"

                result.add(
                    WaitlistPerson(
                        id = entrantId,
                        name = name,
                        joinedDate = current.waitlistJoinedDates[entrantId]
                            ?: "Not recorded"
                    )
                )

                finished++

                if (finished == ids.size) {
                    people = result.sortedBy { it.name }
                    loadingPeople = false
                }
            }
        }
    }

    /**
     * Displays the lottery waitlist screen and its controls.
     *
     * Shows lottery details, winner selection status, and
     * individual entrant information.
     */
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(20.dp)
    ) {
        TextButton(
            onClick = { navController.popBackStack() }
        ) {
            Text("Back")
        }

        Text(
            "Lottery Waitlist",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(20.dp))

        if (errorMessage.isNotEmpty()) {
            Text(errorMessage)
            return@Column
        }

        if (loading || currentLottery == null) {
            CircularProgressIndicator()
            return@Column
        }

        /**
         * Displays the number of entrants and lottery settings.
         */
        Text(
            "Total Entrants: ${currentLottery.waitlistEntrantIds.distinct().size}",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        val capacity = currentLottery.waitlistCapacity

        Text(
            if (capacity == null) {
                "Maximum Capacity: No Limit"
            } else {
                "Maximum Capacity: $capacity"
            }
        )

        Text("Number of Winners: ${currentLottery.winnerCount}")

        Text(
            "Registration Ends: ${currentLottery.registrationEndDate}"
        )

        Spacer(Modifier.height(20.dp))

        /**
         * Displays the draw status and allows winner selection
         * when registration has ended and entrants are available.
         */
        if (currentLottery.drawCompleted) {

            Text(
                "Draw Completed",
                fontWeight = FontWeight.Bold
            )

        } else {

            Text("Waiting for winner selection")

            Spacer(Modifier.height(10.dp))

            /**
             * Requests winner selection through the provided callback.
             *
             * The backend is responsible for enforcing the
             * registration deadline and completing the draw.
             */
            Button(
                onClick = {
                    drawing = true
                    resultMessage = ""

                    onDrawWinners(lotteryId) { error ->
                        drawing = false

                        resultMessage = error
                            ?: "Winner selection completed."
                    }
                },
                enabled = !drawing &&
                        currentLottery.waitlistEntrantIds.isNotEmpty() &&
                        runCatching {
                            LocalDate.now().isAfter(
                                LocalDate.parse(currentLottery.registrationEndDate)
                            )
                        }.getOrDefault(false)
            ) {
                Text(
                    if (drawing) "Drawing..." else "Draw Winners"
                )
            }
        }

        if (resultMessage.isNotEmpty()) {
            Text(resultMessage)
        }

        Spacer(Modifier.height(20.dp))

        HorizontalDivider()

        Spacer(Modifier.height(15.dp))

        Text(
            "Entrants",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(10.dp))

        /**
         * Displays a loading indicator, an empty waitlist
         * message, or the list of registered entrants.
         */
        if (loadingPeople) {
            CircularProgressIndicator()
        } else if (people.isEmpty()) {

            Text("No entrants have joined the waitlist yet.")

        } else {

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(people, key = { it.id }) { person ->

                    /**
                     * Determines whether the entrant is waitlisted,
                     * selected as a winner, or not selected.
                     */
                    val status = when {
                        !currentLottery.drawCompleted -> "Waitlisted"
                        person.id in currentLottery.winnerEntrantIds -> "Selected"
                        else -> "Not Selected"
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(15.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                person.name,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(5.dp))

                            Text("Joined: ${person.joinedDate}")

                            Spacer(Modifier.height(5.dp))

                            Text("Status: $status")
                        }
                    }
                }
            }
        }
    }
}
