package com.example.coders.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.coders.Campsite
import com.example.coders.CampsiteRepo
import com.example.coders.Reservation
import com.example.coders.ReservationRepo
import com.example.coders.ReservationStatus
import com.example.coders.SignedInProfile

/** A campsite the entrant has visited, together with all past reservations there. */
private data class CampsiteMemory(
    val campsiteId: String,
    val campsite: Campsite,
    val reservations: List<Reservation>,
    val latitude: Double,
    val longitude: Double
)

/**
 * Displays one marker for every campsite the signed-in entrant has visited.
 * Multiple past reservations at the same campsite are grouped under one marker.
 */
@Composable
fun CampsiteMemoriesMapScreen(navController: NavController) {
    val profile by SignedInProfile().rememberSignedInProfile()
    val entrantId = profile?.id

    val reservationRepo = remember { ReservationRepo() }
    val campsiteRepo = remember { CampsiteRepo() }

    var reservations by remember(entrantId) {
        mutableStateOf<List<Reservation>>(emptyList())
    }
    var isLoadingReservations by remember(entrantId) {
        mutableStateOf(true)
    }
    var reservationError by remember(entrantId) {
        mutableStateOf<String?>(null)
    }

    DisposableEffect(entrantId, reservationRepo) {
        var listener: com.google.firebase.firestore.ListenerRegistration? = null

        if (entrantId.isNullOrBlank()) {
            isLoadingReservations = false
            reservationError = "Please sign in as an entrant to view your campsite memories."
        } else {
            listener = reservationRepo.listenForEntrantReservations(
                entrantId = entrantId,
                onUpdate = { updatedReservations ->
                    reservations = updatedReservations.filter {
                        it.status == ReservationStatus.Past
                    }
                    isLoadingReservations = false
                    reservationError = null
                },
                onError = { error ->
                    isLoadingReservations = false
                    reservationError = error.message ?: "Unable to load your past reservations."
                }
            )
        }

        onDispose {
            listener?.remove()
        }
    }

    val pastReservations = remember(reservations) {
        reservations.filter { it.status == ReservationStatus.Past }
    }
    val campsiteIds = remember(pastReservations) {
        pastReservations.map { it.campsiteId }
            .filter { it.isNotBlank() }
            .distinct()
    }

    var campsites by remember(entrantId) {
        mutableStateOf<Map<String, Campsite>>(emptyMap())
    }
    var isLoadingCampsites by remember(entrantId) {
        mutableStateOf(false)
    }
    var campsiteLoadError by remember(entrantId) {
        mutableStateOf<String?>(null)
    }

    DisposableEffect(campsiteIds, campsiteRepo) {
        var disposed = false
        campsites = emptyMap()
        campsiteLoadError = null

        if (campsiteIds.isEmpty()) {
            isLoadingCampsites = false
        } else {
            isLoadingCampsites = true
            var remaining = campsiteIds.size

            campsiteIds.forEach { campsiteId ->
                campsiteRepo.getCampsiteById(campsiteId) { campsite, error ->
                    if (!disposed) {
                        if (campsite != null) {
                            campsites = campsites + (campsiteId to campsite)
                        } else if (error != null && campsiteLoadError == null) {
                            campsiteLoadError = error.message
                        }

                        remaining -= 1
                        if (remaining == 0) {
                            isLoadingCampsites = false
                        }
                    }
                }
            }
        }

        onDispose {
            disposed = true
        }
    }

    DisposableEffect(campsiteRepo) {
        onDispose { campsiteRepo.close() }
    }

    val memories = remember(pastReservations, campsites) {
        pastReservations
            .filter { it.campsiteId.isNotBlank() }
            .groupBy { it.campsiteId }
            .mapNotNull { (campsiteId, visits) ->
                val campsite = campsites[campsiteId] ?: return@mapNotNull null
                val coordinates = parseMemoryCoordinates(campsite.mapLocation)
                    ?: return@mapNotNull null

                CampsiteMemory(
                    campsiteId = campsiteId,
                    campsite = campsite,
                    reservations = visits.sortedBy {
                        it.startDate.ifBlank { it.dates }
                    },
                    latitude = coordinates.first,
                    longitude = coordinates.second
                )
            }
    }

    val mapMarkers = remember(memories) {
        memories.map { memory ->
            CampsiteMapMarker(
                id = memory.campsiteId,
                title = memory.campsite.name.ifBlank { "Visited campsite" },
                latitude = memory.latitude,
                longitude = memory.longitude,
                isPast = true
            )
        }
    }

    var selectedCampsiteId by remember { mutableStateOf<String?>(null) }
    val selectedMemory = memories.firstOrNull {
        it.campsiteId == selectedCampsiteId
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CampsiteMapPicker(
            modifier = Modifier.fillMaxSize(),
            interactive = false,
            markers = mapMarkers,
            onMarkerClick = { marker ->
                selectedCampsiteId = marker.id
            },
            onLocationSelected = { _, _ -> }
        )

        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(12.dp),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Campsite Memories",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Places you've visited",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        when {
            isLoadingReservations || isLoadingCampsites -> {
                Surface(
                    modifier = Modifier.align(Alignment.Center),
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("Loading your campsite memories…")
                    }
                }
            }

            reservationError != null -> {
                Surface(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(20.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = reservationError.orEmpty(),
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            pastReservations.isEmpty() -> {
                Surface(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(20.dp),
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 6.dp
                ) {
                    Text(
                        text = "You don't have any campsite memories yet.",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            memories.isEmpty() -> {
                Surface(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(20.dp),
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 6.dp
                ) {
                    Text(
                        text = campsiteLoadError
                            ?: "Your past reservations don't have valid campsite map coordinates yet.",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }

    selectedMemory?.let { memory ->
        AlertDialog(
            onDismissRequest = { selectedCampsiteId = null },
            title = {
                Text(memory.campsite.name.ifBlank { "Campsite Memories" })
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (memory.campsite.location.isNotBlank()) {
                        Text("Location: ${memory.campsite.location}")
                    }

                    Text(
                        text = if (memory.reservations.size == 1) {
                            "Your visit"
                        } else {
                            "Your ${memory.reservations.size} visits"
                        },
                        fontWeight = FontWeight.Bold
                    )

                    memory.reservations.forEachIndexed { index, reservation ->
                        val visitDates = reservation.dates.ifBlank {
                            listOf(reservation.startDate, reservation.endDate)
                                .filter { it.isNotBlank() }
                                .joinToString(" – ")
                        }.ifBlank { "Dates unavailable" }

                        Text(
                            text = "${index + 1}. $visitDates · ${reservation.guestNumber} guests"
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedCampsiteId = null }) {
                    Text("Close")
                }
            }
        )
    }
}

/** Parses a campsite mapLocation stored as "latitude,longitude". */
private fun parseMemoryCoordinates(mapLocation: String): Pair<Double, Double>? {
    val parts = mapLocation.split(",")
    if (parts.size != 2) return null

    val latitude = parts[0].trim().toDoubleOrNull() ?: return null
    val longitude = parts[1].trim().toDoubleOrNull() ?: return null

    if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) {
        return null
    }

    return latitude to longitude
}
