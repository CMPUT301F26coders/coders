/**
 * Provides the entrant-facing screen for submitting a one-time campsite rating.
 */
package com.example.coders.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.coders.CampsiteRepo
import com.example.coders.Reservation
import com.example.coders.ReservationRepo
import com.example.coders.ReservationRatingRepo
import com.example.coders.ReservationStatus
import com.example.coders.SignedInProfile
import com.example.coders.ReservationRating

// The following composable was added using OpenAI, Codex, "Implement US 01.13.01: add a
// one-to-five rating screen for a past campsite reservation", 2026-11-06.

/**
 * Displays the one-time rating form for a past reservation.
 *
 * @param navController Navigates back to Past Reservations after a successful submission.
 * @param reservationId Identifies the past reservation to rate.
 */
@Composable
fun RateCampsiteScreen(
    navController: NavController,
    reservationId: String
) {
    val profile by SignedInProfile().rememberSignedInProfile()
    val entrantId = profile?.id

    val reservationRepo = remember { ReservationRepo() }

    var reservation by remember(reservationId) {
        mutableStateOf<Reservation?>(null)
    }

    var isLoadingReservation by remember(reservationId) {
        mutableStateOf(true)
    }

    var reservationError by remember(reservationId) {
        mutableStateOf<Exception?>(null)
    }

    LaunchedEffect(reservationId) {
        reservationRepo.getReservationById(reservationId) { result, error ->
            reservation = result
            reservationError = error
            isLoadingReservation = false
        }
    }

    val campsiteRepo = remember { CampsiteRepo() }
    val ratingRepo = remember { ReservationRatingRepo() }

    var campsiteName by remember {
        mutableStateOf("Loading campsite…")
    }

    var selectedRating by remember { mutableIntStateOf(0) }
    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }

    var rating by remember(reservationId) {
        mutableStateOf<ReservationRating?>(null)
    }

    var isLoadingRating by remember(reservationId) {
        mutableStateOf(true)
    }

    var ratingError by remember(reservationId) {
        mutableStateOf<Exception?>(null)
    }

    // Fetches the campsite using its document ID.
    LaunchedEffect(reservation?.campsiteId) {
        val campsiteId = reservation?.campsiteId.orEmpty()

        if (campsiteId.isBlank()) {
            campsiteName = "Campsite unavailable"
        } else {
            campsiteRepo.getCampsiteById(campsiteId) { campsite, _ ->
                campsiteName = campsite?.name ?: "Campsite unavailable"
            }
        }
    }

    // Checks for a pre-existing rating.
    LaunchedEffect(reservationId) {
        ratingRepo.getRatingByReservation(reservationId) { result, error ->
            rating = result
            ratingError = error
            isLoadingRating = false
        }
    }

    DisposableEffect(campsiteRepo) {
        onDispose {
            campsiteRepo.close()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(20.dp)
    ) {
        TextButton(
            enabled = !isSaving,
            onClick = { navController.popBackStack() }
        ) {
            Text("Back")
        }

        Text(
            text = "Rate Campsite",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        val currentReservation = reservation

        when {
            entrantId == null || isLoadingReservation ->
                CircularProgressIndicator()

            reservationError != null ->
                Text("Unable to retrieve reservation.")

            currentReservation == null ->
                Text("This reservation is no longer available.")

            entrantId !in currentReservation.entrantIds ->
                Text("This reservation does not belong to you.")

            currentReservation.status != ReservationStatus.Past ->
                Text("Only past reservations can be rated.")

            isLoadingRating ->
                CircularProgressIndicator()

            ratingError != null ->
                Text("Unable to retrieve rating.")

            rating != null ->
                Text("You already rated this campsite ${rating?.score}/5.")

            else -> {
                Text(
                    campsiteName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Choose a rating from 1 to 5")

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (1..5).forEach { score ->
                        OutlinedButton(
                            enabled = !isSaving,
                            onClick = {
                                selectedRating = score
                                saveError = null
                            },
                            modifier = Modifier
                                .weight(1f)
                                .semantics {
                                    contentDescription = "Rating $score"
                                },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selectedRating == score) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            )
                        ) {
                            Text(score.toString())
                        }
                    }
                }

                saveError?.let { error ->
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        error,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    enabled = selectedRating in 1..5 && !isSaving,
                    onClick = {
                        isSaving = true
                        saveError = null

                        ratingRepo.submitRating(
                            entrantId = entrantId,
                            reservationId = currentReservation.id,
                            score = selectedRating
                        ) { error ->
                            isSaving = false

                            if (error == null) {
                                navController.popBackStack()
                            } else {
                                saveError = error.message
                                    ?: "Unable to save your rating."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isSaving) "Saving…" else "Complete")
                }
            }
        }
    }
}