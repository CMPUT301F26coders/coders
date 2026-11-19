
package com.example.coders.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.coders.Lottery
import com.example.coders.Campsite

/**
 * Displays the screen where a campsite owner can join another owner's lottery.
 *
 * Shows available lotteries that accept other campsite owners and displays
 * the campsites belonging to the currently signed-in owner.
 *
 * The owner can select a lottery, choose which campsites to contribute,
 * and submit their selection using the Join Lottery button.
 *
 * The screen also validates the owner's selections and displays error
 * messages when the owner cannot join a lottery.
 *
 * @param navController Handles navigation between screens.
 * @param lotteries List of lotteries available for filtering and display.
 * @param campsites List of campsites used to find the current owner's campsites.
 * @param currentOwnerId ID of the currently signed-in campsite owner.
 * @param onJoinLottery Callback that receives the selected lottery ID and
 * selected campsite IDs when the owner attempts to join.
 * @param isJoining Indicates whether a lottery joining operation is in progress.
 */
@Composable
fun JoinMultiOwnerLotteryScreen(
    navController: NavController,
    lotteries: List<Lottery>,
    campsites: List<Campsite>,
    currentOwnerId: String,
    onJoinLottery: (String, List<String>) -> Unit,
    isJoining: Boolean = false
) {

    // Remember the selected lottery and campsite IDs.
    var selectedLotteryId by remember { mutableStateOf("") }
    var selectedCampsiteIds by remember {
        mutableStateOf(listOf<String>())
    }

    // Store error messages and control whether the error dialog appears.
    var errorMessage by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    // Only show lotteries that allow other owners.
    val availableLotteries = lotteries.filter {
        it.allowOtherOwners &&
                it.campOwnerId != currentOwnerId &&
                currentOwnerId !in it.participatingOwnerIds
    }

    // Find the lottery currently selected by the owner.
    val selectedLottery = availableLotteries.find {
        it.id == selectedLotteryId
    }

    // Only show campsites owned by this owner.
    val ownerCampsites = campsites.filter {
        it.campOwnerId == currentOwnerId
    }

    // Display an error dialog when validation fails.
    if (showError) {
        AlertDialog(
            onDismissRequest = {
                showError = false
            },
            title = {
                Text("Cannot Join Lottery")
            },
            text = {
                Text(errorMessage)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showError = false
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }

    // Main screen layout.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        // Navigate back to the previous screen.
        TextButton(
            onClick = { navController.popBackStack() }
        ) {
            Text("Back")
        }

        // Screen title.
        Text(
            text = "Join A Lottery",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Select an available lottery and contribute your campsite.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Available lotteries section.
        Text(
            text = "Available Lotteries",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Inform the owner when no eligible lotteries exist.
        if (availableLotteries.isEmpty()) {
            Text("No available lotteries right now.")
        }

        // Display real Firebase lotteries.
        availableLotteries.forEach { lottery ->

            Card(
                onClick = {
                    selectedLotteryId = lottery.id
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "Lottery ${lottery.id.take(8)}",
                        fontWeight = FontWeight.Bold
                    )

                    Text("Owner ID: ${lottery.campOwnerId}")

                    Text(
                        "${lottery.campsiteIds.size} participating campsites"
                    )

                    Text(
                        "Registration: ${lottery.registrationStartDate} to ${lottery.registrationEndDate}"
                    )

                    // Indicate which lottery the owner selected.
                    if (selectedLotteryId == lottery.id) {
                        Text("✓ Selected")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))

        // Campsite selection section.
        Text(
            text = "Select Your Campsites",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Choose the campsites you want to add.")

        Spacer(modifier = Modifier.height(12.dp))

        // Inform the owner when they have no campsites.
        if (ownerCampsites.isEmpty()) {
            Text("You have no campsites available.")
        }

        // Display real campsites belonging to the owner.
        ownerCampsites.forEach { campsite ->

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Checkbox(
                    checked = campsite.id in selectedCampsiteIds,
                    onCheckedChange = { checked ->

                        // Add or remove the campsite from the selection.
                        if (checked) {
                            selectedCampsiteIds =
                                selectedCampsiteIds + campsite.id
                        } else {
                            selectedCampsiteIds =
                                selectedCampsiteIds - campsite.id
                        }
                    }
                )

                Text(campsite.name)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))

        // Display a summary of the owner's selections.
        Text(
            text = "Summary",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedLottery != null) {
            Text("Selected Lottery: ${selectedLottery.id.take(8)}")
        } else {
            Text("Selected Lottery: None")
        }

        Text("Your campsites: ${selectedCampsiteIds.size}")

        Spacer(modifier = Modifier.height(20.dp))

        // Validate selections before attempting to join a lottery.
        Button(
            onClick = {

                // Check that a lottery has been selected.
                if (selectedLottery == null) {

                    errorMessage = "Please select a lottery."
                    showError = true

                    // Check that at least one campsite has been selected.
                } else if (selectedCampsiteIds.isEmpty()) {

                    errorMessage = "Please select at least one campsite."
                    showError = true

                    // Prevent the owner from joining their own lottery.
                } else if (selectedLottery.campOwnerId == currentOwnerId) {

                    errorMessage = "You cannot join your own lottery."
                    showError = true

                    // Prevent the owner from joining the same lottery twice.
                } else if (currentOwnerId in selectedLottery.participatingOwnerIds) {

                    errorMessage = "You have already joined this lottery."
                    showError = true

                    // Check whether the lottery accepts other owners.
                } else if (!selectedLottery.allowOtherOwners) {

                    errorMessage = "This lottery is not accepting other owners."
                    showError = true

                } else {

                    // All checks passed. Send the selected IDs to the callback.
                    onJoinLottery(
                        selectedLottery.id,
                        selectedCampsiteIds
                    )
                }
            },

            enabled = !isJoining,

            modifier = Modifier.fillMaxWidth()
        ) {

            // Display joining status while the operation is running.
            if (isJoining) {
                Text("Joining...")
            } else {
                Text("Join Lottery")
            }
        }
    }
}
