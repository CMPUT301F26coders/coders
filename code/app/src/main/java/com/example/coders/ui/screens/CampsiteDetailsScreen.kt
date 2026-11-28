package com.example.coders.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.coders.Campsite
import com.example.coders.Lottery
import com.example.coders.LotteryRepo
import com.example.coders.SignedInProfile
import com.example.coders.UserSession
import com.example.coders.ViewMode
import com.example.coders.isRegistrationOpen

/**
 * Displays detailed information about a selected campsite.
 *
 * Receives a Campsite object and displays its name, location,
 * description, maximum occupancy, rating, map location,
 * campsite owner, and available amenities.
 *
 * The screen uses a scrollable layout to allow users to view
 * all campsite information and provides a Back button to
 * return to the previous screen.
 *
 * The Back button prevents repeated navigation requests,
 * and the layout includes safe screen padding.
 *
 * For entrants, it observes lotteries associated with the campsite and
 * lets them join lotteries whose registration is currently open.
 *
 * @param navController Handles navigation to the previous screen.
 * @param campsite The selected Campsite object containing
 * the information to display.
 */
@Composable
fun CampsiteDetailsScreen(
    navController: NavController,
    campsite: Campsite
) {

    val signedInProfile by SignedInProfile().rememberSignedInProfile()

    // The following lottery listener and join state were added using OpenAI, Codex, "Implement
    // US 01.01.06: allow an entrant to join an active lottery from campsite details", 2026-11-27.
    val lotteryRepo = remember { LotteryRepo() }
    var campsiteLotteries by remember(campsite.id) {
        mutableStateOf<List<Lottery>>(emptyList())
    }
    var joiningLotteryId by remember(campsite.id) { mutableStateOf<String?>(null) }
    var joinedLotteryIds by remember(campsite.id) { mutableStateOf(setOf<String>()) }
    var joinedLotteryId by remember(campsite.id) { mutableStateOf<String?>(null) }
    var joinError by remember(campsite.id) { mutableStateOf<String?>(null) }

    DisposableEffect(lotteryRepo, campsite.id) {
        val registration = lotteryRepo.listenForCampsiteLotteries(
            campsiteId = campsite.id,
            onUpdate = { lotteries ->
                campsiteLotteries = lotteries
            }
        )
        onDispose { registration.remove() }
    }

    val activeLotteries = campsiteLotteries
        .filter { lottery -> lottery.isRegistrationOpen() }
        .sortedWith(compareBy<Lottery> { it.registrationEndDate }.thenBy { it.id })
    val entrantId = signedInProfile?.id.orEmpty()
    val canJoinLotteries =
        UserSession.viewMode == ViewMode.ENTRANT &&
            entrantId.isNotBlank() &&
            entrantId != campsite.campOwnerId

    // The following join result dialogs were added using OpenAI, Codex, "Implement US 01.01.06:
    // allow an entrant to join an active lottery from campsite details", 2026-11-27.
    joinedLotteryId?.let {
        AlertDialog(
            onDismissRequest = { joinedLotteryId = null },
            title = { Text("Joined Lottery") },
            text = {
                Text("You have joined the lottery for ${campsite.name}.")
            },
            confirmButton = {
                TextButton(onClick = { joinedLotteryId = null }) {
                    Text("OK")
                }
            }
        )
    }

    joinError?.let { error ->
        AlertDialog(
            onDismissRequest = { joinError = null },
            title = { Text("Unable to Join Lottery") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { joinError = null }) {
                    Text("OK")
                }
            }
        )
    }

    val coordinates = remember(campsite.mapLocation) {
        val parts = campsite.mapLocation.split(",")

        if (parts.size == 2) {
            val latitude = parts[0].trim().toDoubleOrNull()
            val longitude = parts[1].trim().toDoubleOrNull()

            if (
                latitude != null &&
                longitude != null &&
                latitude in -90.0..90.0 &&
                longitude in -180.0..180.0
            ) {
                latitude to longitude
            } else {
                null
            }
        } else {
            null
        }
    }
    // Prevent multiple back navigation requests from this screen.
    var backClicked by remember { mutableStateOf(false) }
    var editClicked by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {

        // Back button
        TextButton(
            onClick = {
                if (!backClicked) {
                    backClicked = true
                    navController.popBackStack()
                }
            },
            enabled = !backClicked,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
        ) {
            Text("Back")
        }


        if(signedInProfile?.id == campsite.campOwnerId) {
            Button(
                onClick = {
                    if (!editClicked) {
                        editClicked = true
                        navController.navigate("edit_campsite/${campsite.id}")
                    }
                },
                enabled = !editClicked
            ) {
                Text("Edit Campsite")
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // The following entrant lottery actions were added using OpenAI, Codex, "Implement
        // US 01.01.06: allow an entrant to join an active lottery from campsite details", 2026-11-27.
        if (canJoinLotteries) {
            activeLotteries.forEach { lottery ->
                val hasJoined =
                    lottery.id in joinedLotteryIds || entrantId in lottery.waitlistEntrantIds
                val isJoiningThisLottery = joiningLotteryId == lottery.id

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        joiningLotteryId = lottery.id
                        lotteryRepo.joinWaitlist(lottery.id, entrantId) { error ->
                            joiningLotteryId = null

                            if (error == null) {
                                joinedLotteryIds = joinedLotteryIds + lottery.id
                                joinedLotteryId = lottery.id
                            } else {
                                joinError = error.message
                                    ?: "Unable to join the lottery. Please try again."
                            }
                        }
                    },
                    enabled = joiningLotteryId == null && !hasJoined,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        when {
                            isJoiningThisLottery -> "Joining..."
                            hasJoined -> "Joined"
                            else -> "Join Lottery"
                        }
                    )
                }

                if (activeLotteries.size > 1) {
                    Text(
                        text = "Registration ends: ${lottery.registrationEndDate}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }


        // Campsite name and location
        Text(
            text = campsite.name,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(campsite.location)

        Spacer(modifier = Modifier.height(20.dp))

        // About This Campsite
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "About This Campsite",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(campsite.description)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Campsite Information
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Campsite Information",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Maximum Occupancy: ${campsite.capacity} guests"
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Rating: ${campsite.rating ?: "Not rated yet"}"
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Map Location: ${campsite.mapLocation}"
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Campsite Owner: ${campsite.campOwner.name}"
                )
            }
        }

        Row(
            modifier = Modifier
        ){
            Button(
                onClick = {
                    navController.navigate("campsite_ratings/${campsite.id}")
                }
            ) {
                Text("View all ratings")
            }
            Button(
                onClick = {
                    navController.navigate("campsite_reviews/${campsite.id}")
                }
            ) {
                Text("View all reviews")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Campsite Location",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (coordinates != null) {

                    Text(
                        text = "Latitude: %.6f\nLongitude: %.6f".format(
                            coordinates.first,
                            coordinates.second
                        ),
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    CampsiteMapPicker(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        initialLatitude = coordinates.first,
                        initialLongitude = coordinates.second,
                        interactive = false,
                        onLocationSelected = { _, _ -> }
                    )

                } else {
                    Text(
                        text = "Map location is not available for this campsite.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Amenities & Features
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Amenities & Features",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (campsite.tags.isEmpty()) {
                    Text("No amenities listed")
                } else {
                    for (tag in campsite.tags) {
                        Text("• $tag")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // QR Code
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Campsite QR Code",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                CampsiteQrCodeImage(
                    campsite = campsite,
                    size = 160.dp
                )
            }
        }
    }
}
