package com.example.coders.ui.screens

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
import com.example.coders.ReservationStatus
import com.example.coders.Reservation

@Composable
fun OwnerReservationsScreen(navController: NavController) {

    var reservations by remember {
        mutableStateOf<List<Reservation>>(emptyList())
    }
    var reservationToReject by remember {
        mutableStateOf<Reservation?>(null)
    }
    var isNavigating by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("Present") }

    // Temporary UI state (replace with Firebase data later)

    var showRejectDialog by remember { mutableStateOf(false) }

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
            listOf("Past", "Present", "Cancelled").forEach { tab ->
                Button(
                    onClick = { selectedTab = tab },
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

            val filteredReservations = reservations.filter { reservation ->
                when (selectedTab) {
                    "Present" -> reservation.status == ReservationStatus.Active
                    "Past" -> reservation.status == ReservationStatus.Past
                    "Cancelled" -> reservation.status == ReservationStatus.Cancelled ||
                            reservation.status == ReservationStatus.Rejected
                    else -> false
                }
            }
        filteredReservations.forEach { reservation ->
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = reservation.campsite.name,
                        fontWeight = FontWeight.Bold
                    )

                    Text("Dates: ${reservation.date}")
                    Text("Guests: ${reservation.guestNumber}")
                    Text("Status: ${reservation.status}")

                    if (
                        selectedTab == "Present" &&
                        reservation.status == ReservationStatus.Active
                    ) {
                        Button(
                            onClick = {
                                reservationToReject = reservation
                                // TODO: Select reservation for rejection
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
                showRejectDialog = false
            },

            title = {
                Text("Reject Reservation?")
            },

            text = {
                Text(
                    "Are you sure you want to reject the reservation at " +
                            "${reservationToReject?.campsite?.name}? " +
                            "This action cannot be undone."
                )
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        reservationToReject?.let { selected ->
                            reservations = reservations.map { reservation ->
                                if (reservation.id === selected.id &&
                                    reservation.status == ReservationStatus.Active
                                ) {
                                    reservation.copy(
                                        status = ReservationStatus.Rejected
                                    )
                                } else {
                                    reservation
                                }
                            }
                        }

                        reservationToReject = null
                        showRejectDialog = false
                        selectedTab = "Cancelled"
                    }
                ) {
                    Text(
                        "Reject",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        showRejectDialog = false
                    }
                ) {
                    Text("Keep Reservation")
                }
            }
        )
    }
}