
package com.example.coders.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.coders.Entrant
import com.example.coders.ProfileRepo
import com.example.coders.Reservation
import com.example.coders.ReservationStatus
import com.example.coders.SignedInProfile

@Composable
fun EntrantReservationsScreen(navController: NavController) {
    var isNavigating by remember { mutableStateOf(false) }

    var reservationToCancel by remember { mutableStateOf<Reservation?>(null) }
    var isCancelling by remember { mutableStateOf(false) }
    var cancellationError by remember { mutableStateOf<String?>(null) }
    var locallyCancelledIds by remember { mutableStateOf(emptySet<String>()) }

    val profileRepo = remember { ProfileRepo() }

    val profile by SignedInProfile().rememberSignedInProfile()
    val entrant = profile as? Entrant

    val activeReservations = entrant?.reservations.orEmpty().filter { reservation ->
        reservation.status == ReservationStatus.Active && reservation.id !in locallyCancelledIds
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        TextButton(
            onClick = {
                if(!isNavigating){
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

        when {
            profile == null -> CircularProgressIndicator()
            entrant == null -> Text("Reservations are available to entrant profiles only.")
            activeReservations.isEmpty() -> Text("No active reservations.")
            else -> activeReservations.forEach { summary ->
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {

                        Text("Campsite Name", fontWeight = FontWeight.Bold)
                        Text(summary.campsite.name)

                        Spacer(modifier = Modifier.height(20.dp))

                        Text("Location", fontWeight = FontWeight.Bold)
                        Text(summary.campsite.location)

                        Spacer(modifier = Modifier.height(20.dp))

                        Text("Dates", fontWeight = FontWeight.Bold)
                        Text(summary.dates)

                        Spacer(modifier = Modifier.height(20.dp))

                        Text("Guests", fontWeight = FontWeight.Bold)
                        Text(summary.guestNumber.toString())

                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Button(
                            onClick = {
                                navController.navigate("minimap")
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("View On Map")
                        }
                        Spacer(modifier = Modifier.height(12.dp))

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

                        Spacer(modifier = Modifier.height(25.dp))

                        Text(
                            text = "Campsite Owner",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = summary.campOwner.name,
                            fontSize = 20.sp
                        )

                        Button(
                            onClick = {
                                ChatData.owner = summary.campOwner.name
                                navController.navigate("chat")
                            },
                            modifier = Modifier.semantics {
                                contentDescription = "Message ${summary.campOwner.name}"
                            }
                        ) {
                            Text("Message")
                        }
                    }
                }
            }
        }
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
                                    locallyCancelledIds = locallyCancelledIds + reservation.id
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
}
