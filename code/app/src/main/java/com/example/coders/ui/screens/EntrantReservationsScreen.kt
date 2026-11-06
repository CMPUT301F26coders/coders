package com.example.coders.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.coders.Entrant
import com.example.coders.SignedInProfile
import com.example.coders.Reservation
import com.example.coders.ReservationRepo
import com.example.coders.Campsite
import com.example.coders.CampsiteRepo


@Composable
fun EntrantReservationsScreen(navController: NavController) {

    var isNavigating by remember { mutableStateOf(false) }

    val profile by SignedInProfile().rememberSignedInProfile()
    val entrant = profile as? Entrant

    val campsiteRepo = remember { CampsiteRepo() }
    val reservationRepo = remember { ReservationRepo() }


    var campsites by remember {
        mutableStateOf<Map<String, Campsite>>(emptyMap())
    }

    var reservations by remember(entrant?.id) {
        mutableStateOf<List<Reservation>>(emptyList())
    }

    var isLoading by remember(entrant?.id) {
        mutableStateOf(true)
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

        when {
            profile == null || (entrant != null && isLoading) -> {
                CircularProgressIndicator()
            }

            entrant == null -> {
                Text("Reservations are available to entrant profiles only.")
            }

            reservations.isEmpty() -> {
                Text("No reservations yet.")
            }

            else -> reservations.forEach { summary ->

                val campsite = campsites[summary.campsiteId]
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

                        /**
                        Button(
                            onClick = {
                                navController.navigate("minimap")
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("View On Map")
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
                        */

                    }
                }
            }
        }
    }
}