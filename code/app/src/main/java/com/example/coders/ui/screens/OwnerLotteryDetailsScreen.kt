
package com.example.coders.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

import com.example.coders.AuthManager
import com.example.coders.CampOwner
import com.example.coders.Campsite
import com.example.coders.CampsiteRepo
import com.example.coders.Lottery
import com.example.coders.LotteryRepo
import com.example.coders.ProfileRepo

/**
 * Displays the details of a lottery created by the signed-in campsite owner.
 *
 * Retrieves the selected lottery and its associated campsites from Firebase.
 * Displays campsite information, availability dates, registration dates,
 * booking dates, winner count, and whether other campsite owners are allowed.
 *
 * Provides navigation to the lottery waitlist, booked dates, and lottery
 * editing screen. Also allows the owner to delete the lottery after
 * confirming the action.
 *
 * Only the campsite owner associated with the lottery can view its details.
 *
 * @param navController Controls navigation between screens.
 * @param lotteryId The unique ID of the lottery whose details are displayed.
 */
@Composable
fun OwnerLotteryDetailsScreen(
    navController: NavController,
    lotteryId: String
) {

    val context = LocalContext.current

    val authManager = remember {
        AuthManager(context.applicationContext)
    }

    val profileRepo = remember { ProfileRepo() }
    val lotteryRepo = remember { LotteryRepo() }
    val campsiteRepo = remember { CampsiteRepo() }

    var ownerId by remember {
        mutableStateOf("")
    }

    var lottery by remember {
        mutableStateOf<Lottery?>(null)
    }

    var campsites by remember {
        mutableStateOf<List<Campsite>>(emptyList())
    }

    var loading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    var showCampsitePicker by remember {
        mutableStateOf(false)
    }

    var showNotificationDialog by remember { mutableStateOf(false) }
    var notificationTitle by remember { mutableStateOf("Lottery Update") }
    var notificationContent by remember { mutableStateOf("") }
    var isSendingNotification by remember { mutableStateOf(false) }
    var notificationError by remember { mutableStateOf<String?>(null) }
    var notificationResult by remember { mutableStateOf<String?>(null) }

    // Delete lottery variables.
    var showDeleteConfirmation by remember {
        mutableStateOf(false)
    }

    var isDeleting by remember {
        mutableStateOf(false)
    }

    var deleteError by remember {
        mutableStateOf("")
    }

    /**
     * Retrieves and verifies the currently signed-in campsite owner.
     *
     * Uses AuthManager to obtain the authenticated user's ID and ProfileRepo
     * to load their profile from Firebase.
     *
     * If the profile belongs to a CampOwner, the owner's ID is stored for
     * loading and verifying the lottery. Otherwise, an error is displayed.
     *
     * Runs once when the screen first enters composition.
     */
    LaunchedEffect(Unit) {

        val id = authManager.getCurrentUserId()

        if (id == null) {
            errorMessage = "Please sign in as a campsite owner."
            loading = false
        } else {

            profileRepo.getProfileById(id) { profile, _ ->

                if (profile is CampOwner) {
                    ownerId = profile.id
                } else {
                    errorMessage = "Please sign in as a campsite owner."
                    loading = false
                }
            }
        }
    }

    /**
     * Loads the selected lottery from Firebase and verifies its ownership.
     *
     * Retrieves the lottery using its unique ID after the campsite owner's
     * profile has been loaded.
     *
     * Displays an error if the lottery cannot be retrieved, does not exist,
     * or belongs to a different campsite owner.
     *
     * Updates the loading state once the retrieval is complete.
     *
     * Runs whenever the owner ID or lottery ID changes.
     */
    LaunchedEffect(ownerId, lotteryId) {

        if (ownerId.isNotEmpty()) {

            lotteryRepo.getLotteryById(lotteryId) { result, error ->

                if (error != null) {
                    errorMessage = "Unable to load lottery."
                } else if (result == null) {
                    errorMessage = "Lottery not found."
                } else if (result.campOwnerId != ownerId) {
                    errorMessage = "You do not own this lottery."
                } else {
                    lottery = result
                }

                loading = false
            }
        }
    }

    /**
     * Observes campsite information stored in Firebase.
     *
     * Starts a campsite listener after the campsite owner's ID is available.
     * Updates the campsite list whenever Firebase reports changes, allowing
     * the screen to display the names and locations of selected campsites.
     *
     * Removes the Firebase listener when the effect is disposed or
     * the owner ID changes to prevent unnecessary active listeners.
     */
    DisposableEffect(ownerId) {

        val listener = if (ownerId.isNotEmpty()) {

            campsiteRepo.listenForCampsites(
                onUpdate = { updatedCampsites ->
                    campsites = updatedCampsites
                }
            )

        } else {
            null
        }

        onDispose {
            if (listener != null) {
                listener.remove()
            }
        }
    }

    /**
     * Displays the lottery details and available management actions.
     *
     * Shows a loading indicator while lottery information is retrieved
     * and an error message if retrieval or ownership verification fails.
     *
     * Once loaded, displays the lottery's campsite information, important
     * dates, winner count, and owner settings in a scrollable card.
     *
     * Provides buttons for viewing the waitlist, viewing booked dates,
     * sending notifications, editing the lottery, and deleting the lottery.
     */
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        // Back button.
        TextButton(
            onClick = {
                navController.popBackStack()
            }
        ) {
            Text("Back")
        }

        Spacer(modifier = Modifier.height(15.dp))

        Text(
            text = "Lottery Info",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(25.dp))

        if (loading) {

            CircularProgressIndicator()

        } else if (errorMessage.isNotEmpty()) {

            Text(errorMessage)

        } else {

            val currentLottery = lottery

            if (currentLottery != null) {

                // Find the campsites belonging to this lottery.
                val selectedCampsites = campsites.filter {
                    it.id in currentLottery.campsiteIds
                }

                val campsiteNames = selectedCampsites.joinToString(", ") {
                    it.name
                }

                val campsiteLocations = selectedCampsites.joinToString(", ") {
                    it.location
                }

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {

                        // Lottery name.
                        Text(
                            text = if (campsiteNames.isEmpty()) {
                                "Lottery Details"
                            } else {
                                "$campsiteNames Lottery"
                            },
                            fontSize = 23.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(campsiteLocations)

                        Spacer(modifier = Modifier.height(25.dp))

                        // Campsite Period.
                        Text(
                            text = "Campsite Period",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "From: " +
                                    currentLottery.availabilityStartDate.ifEmpty {
                                        "Not set"
                                    }
                        )

                        Text(
                            text = "To: " +
                                    currentLottery.availabilityEndDate.ifEmpty {
                                        "Not set"
                                    }
                        )

                        Spacer(modifier = Modifier.height(25.dp))

                        // Registration Period.
                        Text(
                            text = "Registration Period",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "From: ${currentLottery.registrationStartDate}"
                        )

                        Text(
                            text = "To: ${currentLottery.registrationEndDate}"
                        )

                        Spacer(modifier = Modifier.height(25.dp))

                        // Booking Period.
                        Text(
                            text = "Booking Period",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "From: ${currentLottery.bookingStartDate}"
                        )

                        Text(
                            text = "To: ${currentLottery.bookingEndDate}"
                        )

                        Spacer(modifier = Modifier.height(25.dp))

                        // Number of winners.
                        Text(
                            text = "Number of Winners",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = currentLottery.winnerCount.toString(),
                            fontSize = 25.sp
                        )

                        Spacer(modifier = Modifier.height(25.dp))

                        // Other campsite owners.
                        Text(
                            text = "Other Campsite Owners",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = if (currentLottery.allowOtherOwners) {
                                "Allowed"
                            } else {
                                "Not Allowed"
                            }
                        )

                        Spacer(modifier = Modifier.height(30.dp))

                        // View Waitlist button.
                        Button(
                            onClick = {
                                navController.navigate("ownerLotteryWaitlist/$lotteryId")
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("View Waitlist")
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Booked Dates button.
                        Button(
                            onClick = {

                                if (currentLottery.campsiteIds.size == 1) {

                                    val campsiteId = currentLottery.campsiteIds[0]

                                    navController.navigate(
                                        "booked_dates/$campsiteId"
                                    )

                                } else {
                                    showCampsitePicker = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Booked Dates")
                        }

                        // Choose a campsite popup.
                        if (showCampsitePicker) {

                            AlertDialog(
                                onDismissRequest = {
                                    showCampsitePicker = false
                                },

                                title = {
                                    Text("Choose a Campsite")
                                },

                                text = {
                                    Column {

                                        if (currentLottery.campsiteIds.isEmpty()) {
                                            Text("No campsites found.")
                                        }

                                        currentLottery.campsiteIds.forEach { campsiteId ->

                                            val campsite = campsites.find {
                                                it.id == campsiteId
                                            }

                                            TextButton(
                                                onClick = {
                                                    showCampsitePicker = false

                                                    navController.navigate(
                                                        "booked_dates/$campsiteId"
                                                    )
                                                }
                                            ) {
                                                Text(
                                                    text = if (campsite != null) {
                                                        campsite.name
                                                    } else {
                                                        "Campsite"
                                                    }
                                                )
                                            }
                                        }
                                    }
                                },

                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            showCampsitePicker = false
                                        }
                                    ) {
                                        Text("Cancel")
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Send Notification button.
                        Button(
                            onClick = {
                                notificationTitle = "Lottery Update"
                                notificationContent = ""
                                notificationError = null
                                showNotificationDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Send Notification")
                        }

                        notificationResult?.let { result ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(result, color = MaterialTheme.colorScheme.primary)
                        }

                        if (showNotificationDialog) {
                            AlertDialog(
                                onDismissRequest = {
                                    if (!isSendingNotification) showNotificationDialog = false
                                },
                                title = { Text("Notify waitlisted entrants") },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text(
                                            "This message will be sent to " +
                                                "${currentLottery.waitlistEntrantIds.filter(String::isNotBlank).distinct().size} entrants on this lottery's waitlist."
                                        )
                                        OutlinedTextField(
                                            value = notificationTitle,
                                            onValueChange = { notificationTitle = it },
                                            label = { Text("Title") },
                                            singleLine = true,
                                            enabled = !isSendingNotification
                                        )
                                        OutlinedTextField(
                                            value = notificationContent,
                                            onValueChange = { notificationContent = it },
                                            label = { Text("Message") },
                                            minLines = 3,
                                            enabled = !isSendingNotification
                                        )
                                        notificationError?.let { error ->
                                            Text(error, color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(
                                        enabled = !isSendingNotification &&
                                            notificationTitle.isNotBlank() &&
                                            notificationContent.isNotBlank(),
                                        onClick = {
                                            isSendingNotification = true
                                            notificationError = null
                                            lotteryRepo.sendWaitlistNotification(
                                                ownerId = ownerId,
                                                lotteryId = currentLottery.id,
                                                title = notificationTitle,
                                                content = notificationContent
                                            ) { recipientCount, error ->
                                                isSendingNotification = false
                                                if (error != null) {
                                                    notificationError = error.message
                                                        ?: "Unable to send notification."
                                                } else {
                                                    notificationResult = if (recipientCount == 0) {
                                                        "There are no entrants on this waitlist yet."
                                                    } else {
                                                        "Notification sent to $recipientCount entrants."
                                                    }
                                                    showNotificationDialog = false
                                                }
                                            }
                                        }
                                    ) {
                                        Text(if (isSendingNotification) "Sending…" else "Send")
                                    }
                                },
                                dismissButton = {
                                    TextButton(
                                        enabled = !isSendingNotification,
                                        onClick = { showNotificationDialog = false }
                                    ) {
                                        Text("Cancel")
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Edit Lottery button.
                        Button(
                            onClick = {
                                navController.navigate(
                                    "editLottery/${currentLottery.id}"
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Edit Lottery")
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Delete Lottery button.
                        Button(
                            onClick = {
                                showDeleteConfirmation = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isDeleting,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Text("Delete Lottery")
                        }

                        // Show an error if deletion fails.
                        if (deleteError.isNotEmpty()) {

                            Text(
                                text = deleteError,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        // Delete confirmation popup.
                        if (showDeleteConfirmation) {

                            AlertDialog(
                                onDismissRequest = {
                                    if (!isDeleting) {
                                        showDeleteConfirmation = false
                                    }
                                },

                                title = {
                                    Text("Delete Lottery?")
                                },

                                text = {
                                    Text(
                                        "Are you sure you want to permanently delete this lottery?"
                                    )
                                },

                                confirmButton = {
                                    TextButton(
                                        onClick = {

                                            // Only the owner can delete their lottery.
                                            if (currentLottery.campOwnerId == ownerId) {

                                                isDeleting = true
                                                deleteError = ""

                                                // Delete the selected lottery from Firebase.
                                                lotteryRepo.deleteLottery(
                                                    currentLottery.id
                                                ) { error ->

                                                    isDeleting = false

                                                    if (error == null) {

                                                        showDeleteConfirmation = false

                                                        // Return to the lotteries list.
                                                        navController.popBackStack()

                                                    } else {

                                                        showDeleteConfirmation = false
                                                        deleteError = "Unable to delete lottery."
                                                    }
                                                }

                                            } else {

                                                showDeleteConfirmation = false
                                                deleteError = "You do not own this lottery."
                                            }
                                        },
                                        enabled = !isDeleting
                                    ) {
                                        Text("Delete")
                                    }
                                },

                                dismissButton = {
                                    TextButton(
                                        onClick = {
                                            showDeleteConfirmation = false
                                        },
                                        enabled = !isDeleting
                                    ) {
                                        Text("Cancel")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
