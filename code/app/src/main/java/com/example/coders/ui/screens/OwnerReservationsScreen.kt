
package com.example.coders.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.coders.CampOwner
import com.example.coders.Reservation
import com.example.coders.ReservationRepo
import com.example.coders.ReservationStatus
import com.example.coders.SignedInProfile
import com.example.coders.Campsite
import com.example.coders.CampsiteRepo

@Composable
fun OwnerReservationsScreen(navController: NavController) {

    val reservationRepo = remember { ReservationRepo() }
    val campsiteRepo = remember { CampsiteRepo() }

    var campsites by remember {
        mutableStateOf<Map<String, Campsite>>(emptyMap())
    }
    val signedInProfile = remember { SignedInProfile() }
    val profile by signedInProfile.rememberSignedInProfile()

    var reservations by remember {
        mutableStateOf<List<Reservation>>(emptyList())
    }

    var reservationToReject by remember {
        mutableStateOf<Reservation?>(null)
    }

    var isRejecting by remember {
        mutableStateOf(false)
    }

    var rejectionError by remember {
        mutableStateOf<String?>(null)
    }

    var isNavigating by remember {
        mutableStateOf(false)
    }

    var selectedTab by remember {
        mutableStateOf("Present")
    }

    var showRejectDialog by remember {
        mutableStateOf(false)
    }

    // Listen to the current camp owner's reservations in Firestore
    DisposableEffect(profile?.id, profile is CampOwner, reservationRepo) {
        val ownerId = (profile as? CampOwner)?.id

        val listener = if (!ownerId.isNullOrBlank()) {
            reservationRepo.listenForOwnerReservations(
                campOwnerId = ownerId,
                onUpdate = { updatedReservations ->
                    reservations = updatedReservations
                }
            )
        } else {
            reservations = emptyList()
            null
        }
        onDispose {
            listener?.remove()
        }
    }
    DisposableEffect(reservations, campsiteRepo) {
        var disposed = false


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

    DisposableEffect(campsiteRepo) {
        onDispose {
            campsiteRepo.close()
        }
    }

    // Filter reservations based on the selected tab
    val filteredReservations = reservations.filter { reservation ->
        when (selectedTab) {
            "Present" ->
                reservation.status == ReservationStatus.Active

            "Past" ->
                reservation.status == ReservationStatus.Past

            "Cancelled" ->
                reservation.status == ReservationStatus.Cancelled ||
                        reservation.status == ReservationStatus.Rejected

            else -> false
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

        // Reservation status tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Past", "Present", "Cancelled").forEach { tab ->

                Button(
                    onClick = {
                        selectedTab = tab
                    },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(
                        horizontal = 4.dp,
                        vertical = 8.dp
                    ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == tab)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.secondaryContainer,

                        contentColor = if (selectedTab == tab)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text(tab)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Display reservations
        if (filteredReservations.isEmpty()) {
            Text(
                text = "No ${selectedTab.lowercase()} reservations found.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        filteredReservations.forEach { reservation ->
            val campsite = campsites[reservation.campsiteId]
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Text(
                        text = campsite?.name ?: "Loading...",
                        fontWeight = FontWeight.Bold
                    )

                    Text("Dates: ${reservation.dates}")
                    Text("Guests: ${reservation.guestNumber}")
                    Text("Status: ${reservation.status}")

                    // Owners can reject active reservations
                    if (
                        selectedTab == "Present" &&
                        reservation.status == ReservationStatus.Active
                    ) {
                        Button(
                            onClick = {
                                reservationToReject = reservation
                                rejectionError = null
                                showRejectDialog = true
                            }
                        ) {
                            Text("Reject Reservation")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    // Confirmation dialog
    if (showRejectDialog) {

        AlertDialog(
            onDismissRequest = {
                if (!isRejecting) {
                    showRejectDialog = false
                    reservationToReject = null
                    rejectionError = null
                }
            },

            title = {
                Text("Reject Reservation?")
            },

            text = {
                Column {
                    Text(
                        "Are you sure you want to reject the reservation at " +
                                "${reservationToReject?.let { campsites[it.campsiteId]?.name } ?: "Loading..."}? " +
                                "This action cannot be undone."
                    )

                    rejectionError?.let { error ->
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },

            confirmButton = {
                TextButton(
                    enabled = !isRejecting,
                    onClick = {
                        reservationToReject?.let { selected ->

                            isRejecting = true
                            rejectionError = null

                            reservationRepo.updateReservationStatus(
                                reservationId = selected.id,
                                status = ReservationStatus.Rejected,
                                onComplete = { error ->

                                    isRejecting = false

                                    if (error == null) {
                                        // Firestore listener updates the list
                                        selectedTab = "Cancelled"
                                        reservationToReject = null
                                        showRejectDialog = false
                                        rejectionError = null
                                    } else {
                                        Log.e(
                                            "OwnerReservations",
                                            "Failed to reject reservation",
                                            error
                                        )

                                        rejectionError =
                                            "Failed to reject reservation. Please try again."
                                    }
                                }
                            )
                        }
                    }
                ) {
                    Text(
                        if (isRejecting) "Rejecting..." else "Reject",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },

            dismissButton = {
                TextButton(
                    enabled = !isRejecting,
                    onClick = {
                        showRejectDialog = false
                        reservationToReject = null
                        rejectionError = null
                    }
                ) {
                    Text("Keep Reservation")
                }
            }
        )
    }
}
