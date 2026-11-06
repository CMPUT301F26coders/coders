/** Provides the entrant-facing screen for submitting a one-time campsite rating. */
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
import com.example.coders.Entrant
import com.example.coders.ReservationRatingRepo
import com.example.coders.ReservationStatus
import com.example.coders.SignedInProfile

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
    val entrant = profile as? Entrant
    val reservation = entrant?.reservations?.firstOrNull { it.id == reservationId }
    val campsiteRepo = remember { CampsiteRepo() }
    val ratingRepo = remember { ReservationRatingRepo() }

    var campsiteName by remember { mutableStateOf("Loading campsite…") }
    var selectedRating by remember { mutableIntStateOf(0) }
    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }

    // The campsite is intentionally fetched by campsiteId; no Reservation.campsite object is used.
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

        when {
            profile == null -> CircularProgressIndicator()
            entrant == null -> Text("Ratings are available to entrant profiles only.")
            reservation == null -> Text("This reservation is no longer available.")
            reservation.status != ReservationStatus.Past -> {
                Text("Only past reservations can be rated.")
            }
            reservation.rating != null -> {
                Text("You already rated this campsite ${reservation.rating}/5.")
            }
            else -> {
                Text(campsiteName, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Choose a rating from 1 to 5")
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (1..5).forEach { score ->
                        OutlinedButton(
                            onClick = {
                                selectedRating = score
                                saveError = null
                            },
                            modifier = Modifier
                                .weight(1f)
                                .semantics { contentDescription = "Rating $score" },
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
                    Text(error, color = MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    enabled = selectedRating in 1..5 && !isSaving,
                    onClick = {
                        isSaving = true
                        saveError = null
                        ratingRepo.submitRating(
                            entrantId = entrant.id,
                            reservationId = reservation.id,
                            score = selectedRating
                        ) { error ->
                            isSaving = false
                            if (error == null) {
                                navController.popBackStack()
                            } else {
                                saveError = error.message ?: "Unable to save your rating."
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
