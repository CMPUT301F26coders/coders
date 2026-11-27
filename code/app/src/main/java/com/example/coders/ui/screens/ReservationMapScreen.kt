package com.example.coders.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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

/**
 * Displays the map for one campsite selected from a reservation card.
 *
 * @param navController Handles returning to the reservations screen.
 * @param campsiteId ID of the campsite whose location should be displayed.
 */
@Composable
fun ReservationMapScreen(
    navController: NavController,
    campsiteId: String
) {
    val campsiteRepo = remember { CampsiteRepo() }

    var campsite by remember(campsiteId) {
        mutableStateOf<Campsite?>(null)
    }
    var isLoading by remember(campsiteId) {
        mutableStateOf(true)
    }
    var loadError by remember(campsiteId) {
        mutableStateOf<String?>(null)
    }

    DisposableEffect(campsiteId, campsiteRepo) {
        var disposed = false

        if (campsiteId.isBlank()) {
            isLoading = false
            loadError = "No campsite was selected."
        } else {
            campsiteRepo.getCampsiteById(campsiteId) { result, error ->
                if (!disposed) {
                    campsite = result
                    loadError = when {
                        error != null -> error.message ?: "Unable to load this campsite."
                        result == null -> "Campsite not found."
                        else -> null
                    }
                    isLoading = false
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

    val coordinates = remember(campsite?.mapLocation) {
        campsite?.mapLocation?.let(::parseMapCoordinates)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (coordinates != null) {
            CampsiteMapPicker(
                modifier = Modifier.fillMaxSize(),
                initialLatitude = coordinates.first,
                initialLongitude = coordinates.second,
                interactive = false,
                onLocationSelected = { _, _ -> }
            )
        }

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
                        text = campsite?.name?.ifBlank { "Campsite Location" }
                            ?: "Campsite Location",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    campsite?.location?.takeIf { it.isNotBlank() }?.let { location ->
                        Text(
                            text = location,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(
                        text = "Map location",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        when {
            isLoading -> {
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
                        Text("Loading campsite location…")
                    }
                }
            }

            loadError != null -> {
                Surface(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(20.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = loadError.orEmpty(),
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            coordinates == null -> {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 6.dp
                ) {
                    Text(
                        text = "This campsite doesn't have valid map coordinates yet.",
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

/** Parses a campsite mapLocation stored as "latitude,longitude". */
private fun parseMapCoordinates(mapLocation: String): Pair<Double, Double>? {
    val parts = mapLocation.split(",")
    if (parts.size != 2) return null

    val latitude = parts[0].trim().toDoubleOrNull() ?: return null
    val longitude = parts[1].trim().toDoubleOrNull() ?: return null

    if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) {
        return null
    }

    return latitude to longitude
}
