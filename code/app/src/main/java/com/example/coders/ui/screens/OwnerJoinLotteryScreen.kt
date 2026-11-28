package com.example.coders.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.coders.Campsite
import com.example.coders.CampsiteRepo
import com.example.coders.Lottery
import com.example.coders.LotteryRepo
import com.example.coders.Profile
import com.example.coders.ProfileRepo

/**
 * Allows an invited campsite owner to join another owner's lottery
 * by selecting one or more of their own campsites.
 *
 * All lottery, campsite and owner information comes from Firebase.
 *
 * @param navController Used to navigate between screens.
 */
@Composable
fun OwnerJoinLotteryScreen(navController: NavController) {

    // Repositories used to access Firebase.
    val context = LocalContext.current
    val authManager = remember {
        AuthManager(context.applicationContext)
    }
    val profileRepo = remember { ProfileRepo() }
    val lotteryRepo = remember { LotteryRepo() }
    val campsiteRepo = remember { CampsiteRepo() }

    // Information loaded from Firebase.
    var ownerId by remember { mutableStateOf("") }
    var lotteries by remember {
        mutableStateOf<List<Lottery>>(emptyList())
    }
    var campsites by remember {
        mutableStateOf<List<Campsite>>(emptyList())
    }
    var profiles by remember {
        mutableStateOf<List<Profile>>(emptyList())
    }

    // Stores the owner's selections.
    var selectedLotteryId by remember { mutableStateOf("") }
    val selectedCampsiteIds = remember {
        mutableStateListOf<String>()
    }

    // Loading and joining states.
    var isLoading by remember { mutableStateOf(true) }
    var campsitesLoading by remember { mutableStateOf(true) }
    var isJoining by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Gets the currently signed-in owner's ID.
    LaunchedEffect(Unit) {

        val id = authManager.getCurrentUserId()

        if (id != null) {
            profileRepo.getProfileById(id) { profile, error ->

                if (profile != null && error == null) {
                    ownerId = profile.id
                } else {
                    errorMessage = "Unable to load your profile."
                    isLoading = false
                }
            }
        } else {
            errorMessage = "Please sign in."
            isLoading = false
        }

        // Loads profiles so owner names can be displayed.
        profileRepo.getAllProfiles { result, error ->
            if (error == null) {
                profiles = result.orEmpty()
            }
        }
    }

    // Loads lotteries from Firebase.
    DisposableEffect(ownerId) {

        val listener = if (ownerId.isNotEmpty()) {

            lotteryRepo.listenForAllLotteries(
                onUpdate = {
                    lotteries = it
                    isLoading = false
                },
                onError = {
                    errorMessage = "Unable to load lotteries."
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

    // Loads all campsites from Firebase.
    DisposableEffect(ownerId) {

        val listener = if (ownerId.isNotEmpty()) {

            campsiteRepo.listenForCampsites(
                onUpdate = {
                    campsites = it
                    campsitesLoading = false
                },
                onError = {
                    errorMessage = "Unable to load campsites."
                    campsitesLoading = false
                }
            )

        } else {
            null
        }

        onDispose {
            listener?.remove()
        }
    }

    // Only shows lotteries that the current owner was invited to.
    val invitedLotteries = lotteries.filter {
        it.allowOtherOwners &&
                ownerId in it.invitedOwnerIds &&
                it.campOwnerId != ownerId &&
                ownerId !in it.participatingOwnerIds &&
                !it.drawCompleted
    }

    // Only shows campsites belonging to the current owner.
    val myCampsites = campsites.filter {
        it.campOwnerId == ownerId
    }

    // Automatically selects a lottery if none is selected.
    LaunchedEffect(invitedLotteries) {

        if (invitedLotteries.none { it.id == selectedLotteryId }) {

            selectedLotteryId =
                invitedLotteries.firstOrNull()?.id.orEmpty()

            selectedCampsiteIds.clear()
        }
    }

    // Removes selections if a campsite is no longer available.
    LaunchedEffect(myCampsites) {
        selectedCampsiteIds.removeAll { id ->
            myCampsites.none { it.id == id }
        }
    }

    // Gets the selected lottery.
    val selectedLottery = invitedLotteries.find {
        it.id == selectedLotteryId
    }

    // Displays the Join Lottery screen.
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
            },
            enabled = !isJoining
        ) {
            Text("Back")
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Screen title.
        Text(
            text = "Join Lottery",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(
                Alignment.CenterHorizontally
            )
        )

        Spacer(modifier = Modifier.height(30.dp))

        when {

            // Displays an error if Firebase fails.
            errorMessage.isNotEmpty() -> {

                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Displays loading indicator.
            isLoading || campsitesLoading -> {

                CircularProgressIndicator(
                    modifier = Modifier.align(
                        Alignment.CenterHorizontally
                    )
                )
            }

            // No lottery invitations.
            invitedLotteries.isEmpty() -> {

                Text(
                    text = "You have no lottery invitations.",
                    fontSize = 18.sp
                )
            }

            else -> {

                // Displays every lottery invitation.
                invitedLotteries.forEach { lottery ->

                    // Finds the name of the lottery creator.
                    val creatorName = profiles.find {
                        it.id == lottery.campOwnerId
                    }?.name ?: "Unknown Owner"

                    // Finds the campsites already in the lottery.
                    val lotteryCampsites = campsites.filter {
                        it.id in lottery.campsiteIds
                    }

                    val campsiteNames =
                        lotteryCampsites.joinToString(", ") {
                            it.name
                        }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(15.dp),
                        onClick = {

                            if (!isJoining &&
                                selectedLotteryId != lottery.id
                            ) {
                                selectedLotteryId = lottery.id
                                selectedCampsiteIds.clear()
                            }
                        }
                    ) {

                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {

                            // Lottery name.
                            Text(
                                text = if (campsiteNames.isEmpty()) {
                                    "Booking Lottery"
                                } else {
                                    "$campsiteNames Lottery"
                                },
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(
                                modifier = Modifier.height(10.dp)
                            )

                            Text("Created by $creatorName")

                            Spacer(
                                modifier = Modifier.height(15.dp)
                            )

                            HorizontalDivider()

                            Spacer(
                                modifier = Modifier.height(15.dp)
                            )

                            // Availability period.
                            Text(
                                text = "Availability Period",
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                "From: ${lottery.availabilityStartDate} " +
                                        lottery.availabilityStartTime
                            )

                            Text(
                                "To: ${lottery.availabilityEndDate} " +
                                        lottery.availabilityEndTime
                            )

                            Spacer(
                                modifier = Modifier.height(15.dp)
                            )

                            // Registration period.
                            Text(
                                text = "Registration Period",
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                "From: ${lottery.registrationStartDate} " +
                                        lottery.registrationStartTime
                            )

                            Text(
                                "To: ${lottery.registrationEndDate} " +
                                        lottery.registrationEndTime
                            )

                            Spacer(
                                modifier = Modifier.height(15.dp)
                            )

                            // Booking period.
                            Text(
                                text = "Booking Period",
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                "From: ${lottery.bookingStartDate} " +
                                        lottery.bookingStartTime
                            )

                            Text(
                                "To: ${lottery.bookingEndDate} " +
                                        lottery.bookingEndTime
                            )

                            Spacer(
                                modifier = Modifier.height(15.dp)
                            )

                            // Number of winners.
                            Text(
                                "Number of winners: ${lottery.winnerCount}"
                            )

                            Spacer(
                                modifier = Modifier.height(15.dp)
                            )

                            Text(
                                text = "You have been invited",
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Indicates the selected lottery.
                            if (selectedLotteryId == lottery.id) {

                                Text(
                                    text = "✓ Selected",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )
                }

                // My Campsites section.
                Text(
                    text = "My Campsites",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    "Select the campsites you want to add to this lottery."
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (myCampsites.isEmpty()) {

                    Text("You have no campsites.")

                } else {

                    // Displays each campsite owned by this owner.
                    myCampsites.forEach { campsite ->

                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Checkbox(
                                checked = campsite.id in
                                        selectedCampsiteIds,

                                enabled = !isJoining,

                                onCheckedChange = { checked ->

                                    if (checked) {

                                        selectedCampsiteIds.add(
                                            campsite.id
                                        )

                                    } else {

                                        selectedCampsiteIds.remove(
                                            campsite.id
                                        )
                                    }
                                }
                            )

                            Text(
                                text = campsite.name,
                                fontSize = 18.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(15.dp))

                Text(
                    "Selected campsites: ${selectedCampsiteIds.size}"
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Adds the selected campsites to the lottery.
                Button(
                    onClick = {

                        if (selectedLottery != null) {

                            isJoining = true

                            lotteryRepo.joinOwnerLottery(
                                lotteryId = selectedLottery.id,
                                ownerId = ownerId,
                                campsiteIds =
                                    selectedCampsiteIds.toList(),

                                onComplete = { error ->

                                    isJoining = false

                                    if (error == null) {

                                        Toast.makeText(
                                            context,
                                            "Lottery joined successfully.",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        navController.popBackStack()

                                    } else {

                                        Toast.makeText(
                                            context,
                                            error.message
                                                ?: "Unable to join lottery.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            )
                        }
                    },

                    enabled = selectedLottery != null &&
                            selectedCampsiteIds.isNotEmpty() &&
                            !isJoining,

                    modifier = Modifier.fillMaxWidth()

                ) {

                    if (isJoining) {
                        Text("Joining...")
                    } else {
                        Text("Join Lottery")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
