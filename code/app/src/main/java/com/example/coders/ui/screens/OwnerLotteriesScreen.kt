
package com.example.coders.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
 * Displays the lotteries created by the signed-in campsite owner.
 *
 * Gets lottery information from Firebase and shows it in a list.
 * Owners can also open the screen to create a new lottery.
 *
 * @param navController Used to move between screens.
 */
@Composable
fun OwnerLotteriesScreen(navController: NavController) {

    // Repositories used to get information from Firebase.
    val context = LocalContext.current
    val authManager = remember { AuthManager(context.applicationContext) }
    val profileRepo = remember { ProfileRepo() }
    val lotteryRepo = remember { LotteryRepo() }
    val campsiteRepo = remember { CampsiteRepo() }

    // Information displayed on the screen.
    var ownerId by remember { mutableStateOf("") }
    var lotteries by remember { mutableStateOf<List<Lottery>>(emptyList()) }
    var campsites by remember { mutableStateOf<List<Campsite>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var lotteryToNotify by remember { mutableStateOf<Lottery?>(null) }
    var notificationTitle by remember { mutableStateOf("Lottery Update") }
    var notificationContent by remember { mutableStateOf("") }
    var isSendingNotification by remember { mutableStateOf(false) }
    var notificationError by remember { mutableStateOf<String?>(null) }
    var notificationResult by remember { mutableStateOf<String?>(null) }

    /**
     * Gets the currently signed-in campsite owner's ID.
     *
     * This ID is used to find the owner's lotteries.
     */
    LaunchedEffect(Unit) {
        val id = authManager.getCurrentUserId()

        if (id != null) {
            profileRepo.getProfileById(id) { profile, error ->
                if (profile is CampOwner) {
                    ownerId = profile.id
                }
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    /**
     * Loads the owner's lotteries from Firebase.
     *
     * Updates the list when lotteries are added or changed.
     */
    DisposableEffect(ownerId) {

        val listener = if (ownerId.isNotEmpty()) {
            lotteryRepo.listenForOwnerLotteries(
                ownerId = ownerId,
                onUpdate = {
                    lotteries = it
                    isLoading = false
                },
                onError = {
                    isLoading = false
                }
            )
        } else {
            null
        }

        onDispose {
            listener?.remove()
        }
    }

    /**
     * Loads campsite information from Firebase.
     *
     * Campsite names are displayed on lottery cards.
     */
    DisposableEffect(ownerId) {

        val listener = if (ownerId.isNotEmpty()) {
            campsiteRepo.listenForCampsites(
                onUpdate = {
                    campsites = it
                }
            )
        } else {
            null
        }

        onDispose {
            listener?.remove()
        }
    }

    /**
     * Displays the My Lotteries screen.
     */
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(20.dp)
    ) {

        // Back button.
        TextButton(
            onClick = { navController.popBackStack() }
        ) {
            Text("Back")
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Screen title.
        Text(
            text = "Lotteries",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        notificationResult?.let { result ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(result, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Area where lotteries appear.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {

            when {

                // Firebase is loading.
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Owner has not created any lotteries.
                lotteries.isEmpty() -> {
                    Text(
                        text = "You have no lotteries yet.",
                        fontSize = 18.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Display the owner's lotteries.
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(15.dp)
                    ) {

                        items(lotteries) { lottery ->

                            // Find the names of the selected campsites.
                            val names = campsites.filter {
                                it.id in lottery.campsiteIds
                            }

                            val campsiteNames = names.joinToString(", ") {
                                it.name
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // Lottery details will be added next.
                                    },
                                shape = RoundedCornerShape(15.dp)
                            ) {

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {

                                        Text(
                                            text = "Booking Lottery",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = campsiteNames
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("${lottery.waitlistEntrantIds.filter(String::isNotBlank).distinct().size} entrants on waitlist")
                                        TextButton(
                                            onClick = {
                                                lotteryToNotify = lottery
                                                notificationTitle = "Lottery Update"
                                                notificationContent = ""
                                                notificationError = null
                                            }
                                        ) {
                                            Text("Notify entrants")
                                        }
                                    }

                                    Text(
                                        text = "→",
                                        fontSize = 24.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Opens the existing lottery creation screen.
        Button(
            onClick = {
                navController.navigate("multiOwnerLottery")
            },
            enabled = ownerId.isNotEmpty(),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("+ Add Lottery")
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    lotteryToNotify?.let { lottery ->
        AlertDialog(
            onDismissRequest = {
                if (!isSendingNotification) lotteryToNotify = null
            },
            title = { Text("Notify waitlisted entrants") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("This message will be sent to ${lottery.waitlistEntrantIds.filter(String::isNotBlank).distinct().size} entrants on this lottery's waitlist.")
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
                        enabled = !isSendingNotification,
                        minLines = 3
                    )
                    notificationError?.let { error ->
                        Text(error, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !isSendingNotification && notificationTitle.isNotBlank() && notificationContent.isNotBlank(),
                    onClick = {
                        isSendingNotification = true
                        notificationError = null
                        lotteryRepo.sendWaitlistNotification(
                            ownerId = ownerId,
                            lotteryId = lottery.id,
                            title = notificationTitle,
                            content = notificationContent
                        ) { recipientCount, error ->
                            isSendingNotification = false
                            if (error != null) {
                                notificationError = error.message ?: "Unable to send notification."
                            } else {
                                notificationResult = if (recipientCount == 0) {
                                    "There are no entrants on this waitlist yet."
                                } else {
                                    "Notification sent to $recipientCount entrants."
                                }
                                lotteryToNotify = null
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
                    onClick = { lotteryToNotify = null }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
