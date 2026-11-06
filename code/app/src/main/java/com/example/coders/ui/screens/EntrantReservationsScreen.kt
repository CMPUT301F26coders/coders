package com.example.coders.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.coders.Entrant
import com.example.coders.ProfileRepo
import com.example.coders.ReservationStatus
import com.example.coders.SignedInProfile
import com.example.coders.Reservation
import com.example.coders.ReservationRepo
import com.example.coders.Campsite
import com.example.coders.CampsiteRepo
import com.example.coders.Profile

// The following enum was added using OpenAI, Codex, "Implement US 01.04.03: add Pending and
// Active reservation tabs for entrant lottery offers", 2026-11-06.
private enum class EntrantReservationTab(
    val label: String,
    val status: ReservationStatus
) {
    Pending("Pending", ReservationStatus.Pending),
    Active("Active", ReservationStatus.Active)
}

// The following composable was modified using OpenAI, Codex, "Implement US 01.04.03: add Pending
// and Active reservation tabs plus accept and confirmed-decline lottery-offer actions", 2026-11-06.
/**
 * Displays the signed-in entrant's lottery offers and active reservations.
 *
 * Pending lottery offers can be accepted or declined. Accepting makes the reservation active;
 * declining it requires confirmation and changes its status to rejected. Active reservations can
 * still be cancelled after confirmation.
 *
 * @param navController Handles navigation to the map, chat, and previous screens.
 */
@Composable
fun EntrantReservationsScreen(navController: NavController) {

    var isNavigating by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(EntrantReservationTab.Active) }

    var reservationToCancel by remember { mutableStateOf<Reservation?>(null) }
    var isCancelling by remember { mutableStateOf(false) }
    var cancellationError by remember { mutableStateOf<String?>(null) }

    var reservationToDecline by remember { mutableStateOf<Reservation?>(null) }
    var isUpdatingOffer by remember { mutableStateOf(false) }
    var offerUpdateError by remember { mutableStateOf<String?>(null) }

    // The signed-in profile is loaded once. Keep successful changes visible until the next load.
    var locallyUpdatedStatuses by remember { mutableStateOf(emptyMap<String, ReservationStatus>()) }

    val profileRepo = remember { ProfileRepo() }

    val profile by SignedInProfile().rememberSignedInProfile()
    val entrant = profile as? Entrant

    val campsiteRepo = remember { CampsiteRepo() }
    val reservationRepo = remember { ReservationRepo() }


    var campsites by remember {
        mutableStateOf<Map<String, Campsite>>(emptyMap())
    }

    var campOwners by remember {
        mutableStateOf<Map<String, Profile>>(emptyMap())
    }

    var reservations by remember(entrant?.id) {
        mutableStateOf<List<Reservation>>(emptyList())
    }

    var isLoading by remember(entrant?.id) {
        mutableStateOf(true)
    }


    DisposableEffect(reservations, profileRepo) {
        var disposed = false

        campOwners = emptyMap()

        reservations.map { it.campOwnerId }
            .filter { it.isNotBlank() }
            .distinct()
            .forEach { ownerId ->

                profileRepo.getProfileById(ownerId) { owner, _ ->
                    if (!disposed && owner != null) {
                        campOwners = campOwners + (ownerId to owner)
                    }
                }
            }

        onDispose {
            disposed = true
        }
    }

    // Listen for reservations associated with the signed-in entrant
    DisposableEffect(entrant?.id, reservationRepo) {

        val listener = entrant?.id?.let { entrantId ->
            reservationRepo.listenForEntrantReservations(
                entrantId = entrantId,

                onUpdate = { updatedReservations ->
                    reservations = updatedReservations
                    isLoading = false
                },

                onError = {
                    isLoading = false
                }
            )
        }

        onDispose {
            listener?.remove()
        }
    }

    DisposableEffect(campsiteRepo) {
        onDispose {
            campsiteRepo.close()
        }
    }
    DisposableEffect(profileRepo) {
        onDispose {
            profileRepo.close()
        }
    }

    DisposableEffect(reservations, campsiteRepo) {
        var disposed = false

        campsites = emptyMap()

        reservations.map { it.campsiteId }
            .filter { it.isNotBlank() }
            .distinct()
            .forEach { campsiteId ->

                campsiteRepo.getCampsiteById(campsiteId) { campsite, _ ->

                    if (!disposed && campsite != null) {
                        campsites = campsites + (campsiteId to campsite)
                    }
                }
            }

        onDispose {
            disposed = true
        }
    }

    val visibleReservations = reservations.filter { it.status == selectedTab.status }

    fun updateReservationStatus(
        reservation: Reservation,
        status: ReservationStatus,
        onSuccess: () -> Unit
    ) {
        val currentEntrant = entrant ?: return
        val updatedEntrant = currentEntrant.copy(
            reservations = currentEntrant.reservations.map {
                if (it.id == reservation.id) it.copy(status = status) else it
            }
        )

        profileRepo.updateProfile(currentEntrant, updatedEntrant) { error ->
            isUpdatingOffer = false

            if (error == null) {
                locallyUpdatedStatuses = locallyUpdatedStatuses + (reservation.id to status)
                onSuccess()
            } else {
                offerUpdateError = error.message ?: "Unable to update the lottery offer."
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        TextButton(
            onClick = {
                if (!isNavigating) {
                    isNavigating = true
                    navController.popBackStack()
                }
            }
        ) {
            Text("Back")
        }

        Text(
            text = "Your Reservations",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EntrantReservationTab.entries.forEach { tab ->
                Button(
                    onClick = { selectedTab = tab },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == tab) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        },
                        contentColor = if (selectedTab == tab) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        }
                    )
                ) {
                    Text(tab.label)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        when {
            profile == null || (entrant != null && isLoading) -> {
                CircularProgressIndicator()
            }

            entrant == null -> {
                Text("Reservations are available to entrant profiles only.")
            }

            reservations.isEmpty() -> {
                Text("No reservations in this tab.")
            }

            else -> visibleReservations.forEach { summary ->

                val campsite = campsites[summary.campsiteId]
                val campOwner = campOwners[summary.campOwnerId]
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {

                        Text("Campsite Name", fontWeight = FontWeight.Bold)
                        Text(campsite?.name ?: "Loading...")

                        Spacer(modifier = Modifier.height(20.dp))

                        Text("Location", fontWeight = FontWeight.Bold)
                        Text(campsite?.location ?: "Loading...")

                        Spacer(modifier = Modifier.height(20.dp))

                        Text("Dates", fontWeight = FontWeight.Bold)
                        Text(summary.dates)

                        Spacer(modifier = Modifier.height(20.dp))

                        Text("Guests", fontWeight = FontWeight.Bold)
                        Text(summary.guestNumber.toString())

                        Spacer(modifier = Modifier.height(20.dp))

                        if (summary.status == ReservationStatus.Pending) {
                            Button(
                                enabled = !isUpdatingOffer,
                                onClick = {
                                    offerUpdateError = null
                                    isUpdatingOffer = true
                                    updateReservationStatus(
                                        reservation = summary,
                                        status = ReservationStatus.Active,
                                        onSuccess = {
                                            selectedTab = EntrantReservationTab.Active
                                        }
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .semantics {
                                        contentDescription =
                                            "Accept offer for ${summary.campsite.name}"
                                    }
                            ) {
                                Text(if (isUpdatingOffer) "Accepting…" else "Accept Offer")
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedButton(
                                enabled = !isUpdatingOffer,
                                onClick = {
                                    offerUpdateError = null
                                    reservationToDecline = summary
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .semantics {
                                        contentDescription =
                                            "Decline offer for ${summary.campsite.name}"
                                    }
                            ) {
                                Text("Decline Offer")
                            }

                            offerUpdateError?.let { error ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(error, color = MaterialTheme.colorScheme.error)
                            }
                        } else {
                            Button(
                                onClick = {
                                    cancellationError = null
                                    reservationToCancel = summary
                                },
                                modifier = Modifier.fillMaxWidth().semantics {
                                    contentDescription = "Cancel reservation for ${summary.campsite.name}"
                                }
                            ) {
                                Text("Cancel Reservation")
                            }
                        }

                        Spacer(modifier = Modifier.height(25.dp))
                        Text(
                            text = "Campsite Owner",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = campOwner?.name?: "Loading... ",
                            fontSize = 20.sp
                        )


                    }
                }
            }
        }
//        Spacer(modifier = Modifier.height(24.dp))
//        Button(
//            onClick = { navController.navigate("entrantpastreservations") },
//            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray, contentColor = Color.Black),
//            shape = RoundedCornerShape(8.dp)
//        ) {
//            Text("Past Reservations", fontWeight = FontWeight.Bold)
//        }
//        Spacer(modifier = Modifier.height(24.dp))
    }

// The following AlertDialog is from OpenAI, Codex, "in my [EntrantReservationsScreen.kt](code/app/src/main/java/com/example/coders/ui/screens/EntrantReservationsScreen.kt) I've already added the cancel reservation feature to each card. I need you to create the AlertDialog; confirmation updates that reservation in entrant.reservations to cancelled through ProfileRepo.updateProfile(...). Then Remove it from the active display after saving. This should go after the main Column in [EntrantReservationsScreen.kt](code/app/src/main/java/com/example/coders/ui/screens/EntrantReservationsScreen.kt)"
    reservationToCancel?.let { reservation ->
        AlertDialog(
            onDismissRequest = {
                if (!isCancelling) reservationToCancel = null
            },
            title = { Text("Cancel reservation?") },
            text = {
                Column {
                    Text(
                        "Cancel your reservation for ${reservation.campsite.name}? " +
                            "This action cannot be undone."
                    )

                    cancellationError?.let { error ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(error, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !isCancelling,
                    onClick = {
                        entrant?.let { currentEntrant ->
                            isCancelling = true

                            val updatedEntrant = currentEntrant.copy(
                                reservations = currentEntrant.reservations.map {
                                    if (it.id == reservation.id) {
                                        it.copy(status = ReservationStatus.Cancelled)
                                    } else {
                                        it
                                    }
                                }
                            )

                            profileRepo.updateProfile(currentEntrant, updatedEntrant) { error ->
                                isCancelling = false

                                if (error == null) {
                                    locallyUpdatedStatuses = locallyUpdatedStatuses +
                                        (reservation.id to ReservationStatus.Cancelled)
                                    reservationToCancel = null
                                } else {
                                    cancellationError =
                                        error.message ?: "Unable to cancel reservation."
                                }
                            }
                        }
                    }
                ) {
                    Text(if (isCancelling) "Cancelling…" else "Confirm cancellation")
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isCancelling,
                    onClick = { reservationToCancel = null }
                ) {
                    Text("Keep reservation")
                }
            }
        )
    }

    reservationToDecline?.let { reservation ->
        AlertDialog(
            onDismissRequest = {
                if (!isUpdatingOffer) reservationToDecline = null
            },
            title = { Text("Decline lottery offer?") },
            text = {
                Column {
                    Text(
                        "Decline your reservation offer for ${reservation.campsite.name}? " +
                            "This action cannot be undone."
                    )

                    offerUpdateError?.let { error ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(error, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !isUpdatingOffer,
                    onClick = {
                        offerUpdateError = null
                        isUpdatingOffer = true
                        updateReservationStatus(
                            reservation = reservation,
                            status = ReservationStatus.Rejected,
                            onSuccess = {
                                reservationToDecline = null
                            }
                        )
                    }
                ) {
                    Text(if (isUpdatingOffer) "Declining…" else "Decline offer")
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isUpdatingOffer,
                    onClick = { reservationToDecline = null }
                ) {
                    Text("Keep offer")
                }
            }
        )
    }
}
