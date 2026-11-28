/**
 * Defines the entrant's Waitlists screen, opened from My Stuff.
 *
 * Lists every lottery waiting list the signed-in entrant has joined and shows
 * where they stand on each one: registration still open, waiting for the draw,
 * selected, or not selected. Entrants can leave a waiting list until winners
 * are drawn.
 *
 * Outstanding issues: None known.
 */

package com.example.coders.ui.screens

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.coders.AuthManager
import com.example.coders.CampsiteRepo
import com.example.coders.EntrantWaitlistStatus
import com.example.coders.Lottery
import com.example.coders.LotteryRepo
import com.example.coders.entrantWaitlistStatus
import com.example.coders.waitlistLeaveError

// The following screen was added using Anthropic, Claude Code, "Implement US 01.02.01: allow an
// entrant to join the waiting list for an active campsite lottery", 2026-11-27.
/**
 * Displays the waiting lists the signed-in entrant has joined.
 *
 * Listens for lotteries whose waitlist contains the entrant, so a lottery joined from the
 * campsite details screen appears here right away. Tapping a card opens its campsite, and
 * its Leave Lottery button removes the entrant from that waiting list after they confirm.
 *
 * @param navController Used to go back and to open a lottery's campsite details.
 */
@Composable
fun EntrantWaitlistsScreen(navController: NavController) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context.applicationContext) }
    val lotteryRepo = remember { LotteryRepo() }
    val campsiteRepo = remember { CampsiteRepo() }

    // null while the entrant ID or their lotteries are still loading.
    var entrantId by remember { mutableStateOf<String?>(null) }
    var lotteries by remember { mutableStateOf<List<Lottery>?>(null) }
    var loadFailed by remember { mutableStateOf(false) }
    var campsiteNames by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    LaunchedEffect(Unit) {
        entrantId = authManager.getCurrentUserId().orEmpty()
    }

    DisposableEffect(entrantId) {
        val id = entrantId
        val registration = if (!id.isNullOrBlank()) {
            lotteryRepo.listenForEntrantLotteries(
                entrantId = id,
                onUpdate = { joined ->
                    lotteries = joined
                    loadFailed = false
                },
                onError = {
                    lotteries = emptyList()
                    loadFailed = true
                }
            )
        } else {
            if (id != null) lotteries = emptyList()
            null
        }
        onDispose { registration?.remove() }
    }

    // Looks up the names of campsites in the joined lotteries that haven't been loaded yet.
    val campsiteIds = lotteries.orEmpty().flatMap { it.campsiteIds }.distinct()
    LaunchedEffect(campsiteIds) {
        campsiteIds.filterNot { it in campsiteNames }.forEach { campsiteId ->
            campsiteRepo.getCampsiteById(campsiteId) { campsite, _ ->
                if (campsite != null) {
                    campsiteNames = campsiteNames + (campsiteId to campsite.name)
                }
            }
        }
    }

    val id = entrantId.orEmpty()
    val sortedLotteries = lotteries.orEmpty().sortedWith(
        compareBy<Lottery> { it.entrantWaitlistStatus(id).ordinal }
            .thenBy { it.registrationEndDate }
            .thenBy { it.id }
    )

    // The following leave state and dialogs were added using Anthropic, Claude Code, "Implement
    // US 01.02.02: allow an entrant to leave a campsite lottery waiting list", 2026-11-28.
    var lotteryToLeave by remember { mutableStateOf<Lottery?>(null) }
    var leavingLotteryId by remember { mutableStateOf<String?>(null) }
    var leftLotteryName by remember { mutableStateOf<String?>(null) }
    var leaveError by remember { mutableStateOf<String?>(null) }

    fun displayName(lottery: Lottery): String =
        lottery.campsiteIds.mapNotNull { campsiteNames[it] }
            .joinToString(", ")
            .ifBlank { "this campsite lottery" }

    lotteryToLeave?.let { lottery ->
        val name = displayName(lottery)
        AlertDialog(
            onDismissRequest = { lotteryToLeave = null },
            title = { Text("Leave waiting list?") },
            text = {
                Text(
                    "You'll be removed from the waiting list for $name. " +
                        "You can rejoin while registration is still open."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        lotteryToLeave = null
                        leavingLotteryId = lottery.id
                        lotteryRepo.leaveWaitlist(lottery.id, id) { error ->
                            leavingLotteryId = null
                            if (error == null) {
                                leftLotteryName = name
                            } else {
                                leaveError = error.message
                                    ?: "Unable to leave the waiting list. Please try again."
                            }
                        }
                    },
                    modifier = Modifier.testTag("confirmLeaveLottery")
                ) {
                    Text("Leave")
                }
            },
            dismissButton = {
                TextButton(onClick = { lotteryToLeave = null }) {
                    Text("Stay")
                }
            }
        )
    }

    leftLotteryName?.let { name ->
        AlertDialog(
            onDismissRequest = { leftLotteryName = null },
            title = { Text("Left Lottery") },
            text = { Text("You have left the waiting list for $name.") },
            confirmButton = {
                TextButton(onClick = { leftLotteryName = null }) {
                    Text("OK")
                }
            }
        )
    }

    leaveError?.let { error ->
        AlertDialog(
            onDismissRequest = { leaveError = null },
            title = { Text("Unable to Leave Lottery") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { leaveError = null }) {
                    Text("OK")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(20.dp)
    ) {
        TextButton(onClick = { navController.popBackStack() }) {
            Text("Back")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "My Waitlists",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Lotteries you've joined and where you stand",
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when {
                lotteries == null -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                loadFailed -> {
                    Text(
                        text = "Could not load your waitlists. Please try again.",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                sortedLotteries.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "You're not on any waiting lists yet.",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Open a campsite from Search and tap Join Lottery " +
                                "to join its waiting list."
                        )
                    }
                }

                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(sortedLotteries, key = { it.id }) { lottery ->
                            WaitlistCard(
                                lottery = lottery,
                                entrantId = id,
                                campsiteNames = lottery.campsiteIds.mapNotNull { campsiteNames[it] },
                                onClick = lottery.campsiteIds.firstOrNull()?.let { campsiteId ->
                                    {
                                        navController.navigate(
                                            "campsiteDetails/${Uri.encode(campsiteId)}"
                                        )
                                    }
                                },
                                onViewAvailableDates = {
                                    navController.navigate(
                                        "lotteryavailabledates/${Uri.encode(lottery.id)}"
                                    )
                                },
                                isLeaving = leavingLotteryId == lottery.id,
                                leaveEnabled = leavingLotteryId == null,
                                onLeave = if (lottery.waitlistLeaveError(id) == null) {
                                    { lotteryToLeave = lottery }
                                } else {
                                    null
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Displays one joined waiting list with the entrant's status, join date, and list size.
 *
 * @param lottery The lottery whose waiting list the entrant joined.
 * @param entrantId The signed-in entrant's profile ID.
 * @param campsiteNames Names of the lottery's campsites that have loaded so far.
 * @param onClick Opens the lottery's campsite, or `null` if it has no campsite.
 * @param onViewAvailableDates Opens the available-dates calendar, shown only to winners.
 * @param isLeaving Whether a leave request for this lottery is in progress.
 * @param leaveEnabled Whether the Leave Lottery button can be pressed right now.
 * @param onLeave Starts leaving this waiting list, or `null` if leaving isn't allowed.
 */
@Composable
private fun WaitlistCard(
    lottery: Lottery,
    entrantId: String,
    campsiteNames: List<String>,
    onClick: (() -> Unit)?,
    onViewAvailableDates: () -> Unit = {},
    isLeaving: Boolean = false,
    leaveEnabled: Boolean = true,
    onLeave: (() -> Unit)? = null
) {
    val status = lottery.entrantWaitlistStatus(entrantId)
    val registrationCloses =
        "${lottery.registrationEndDate} ${lottery.registrationEndTime}".trim()
    val (statusText, statusColor) = when (status) {
        EntrantWaitlistStatus.RegistrationOpen ->
            "On waiting list · registration closes $registrationCloses" to
                MaterialTheme.colorScheme.primary
        EntrantWaitlistStatus.AwaitingDraw ->
            "Registration closed · waiting for the draw" to
                MaterialTheme.colorScheme.onSurfaceVariant
        EntrantWaitlistStatus.Selected ->
            "Selected! View your available dates below." to Color(0xFF2E7D32)
        EntrantWaitlistStatus.NotSelected ->
            "Not selected this time" to MaterialTheme.colorScheme.error
    }
    val waitlistSize = lottery.waitlistEntrantIds.distinct().size
    val capacityText = lottery.waitlistCapacity?.let { " / $it" }.orEmpty()
    val entrantNoun = if (waitlistSize == 1) "entrant" else "entrants"
    val joinedDate = lottery.waitlistJoinedDates[entrantId]

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("waitlistCard_${lottery.id}")
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(15.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = campsiteNames.joinToString(", ").ifBlank { "Campsite lottery" },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(text = statusText, color = statusColor)

            Spacer(modifier = Modifier.height(6.dp))

            if (joinedDate != null) {
                Text(
                    text = "Joined: $joinedDate",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = "Waiting list: $waitlistSize$capacityText $entrantNoun",
                style = MaterialTheme.typography.bodySmall
            )

            // The following button was added using Anthropic, Claude Code, "Implement
            // US 01.04.01: show lottery winners the available dates for the campsite they won",
            // 2026-11-28.
            if (status == EntrantWaitlistStatus.Selected) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onViewAvailableDates,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("viewAvailableDates_${lottery.id}")
                ) {
                    Text("View Available Dates")
                }
            }

            // The following button was added using Anthropic, Claude Code, "Implement
            // US 01.02.02: allow an entrant to leave a campsite lottery waiting list", 2026-11-28.
            if (onLeave != null) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onLeave,
                    enabled = leaveEnabled,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("leaveLotteryButton_${lottery.id}")
                ) {
                    Text(if (isLeaving) "Leaving..." else "Leave Lottery")
                }
            }
        }
    }
}
