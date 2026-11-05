package com.example.coders.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.coders.CampsiteRepo

@Composable
fun CampsiteDetailsScreen(
    navController: NavController,
    campsiteId: String
) {
    val campsiteRepo = remember { CampsiteRepo() }

    val campsite = campsiteRepo.getAllCampsites()
        .firstOrNull { it.id == campsiteId }

    Scaffold(
        bottomBar = {
            AppBottomBar(navController)
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            TextButton(
                onClick = {
                    navController.popBackStack()
                }
            ) {
                Text("← Back")
            }

            if (campsite == null) {
                Text("Loading campsite details...")

            } else {

                Text(
                    text = campsite.name,
                    style = MaterialTheme.typography.headlineMedium
                )

                Text(
                    text = "Location: ${campsite.location}"
                )

                Text(
                    text = "Rating: ${campsite.rating ?: "Not rated"}"
                )

                HorizontalDivider()

                Text(
                    text = "Description",
                    style = MaterialTheme.typography.titleLarge
                )

                Text(campsite.description)

                HorizontalDivider()

                Text(
                    text = "Amenities",
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = if (campsite.tags.isEmpty()) {
                        "No amenities listed."
                    } else {
                        campsite.tags.joinToString(", ")
                    }
                )

                HorizontalDivider()

                Text(
                    text = "Maximum Occupancy",
                    style = MaterialTheme.typography.titleLarge
                )

                Text("${campsite.capacity} guests")

                HorizontalDivider()

                Text(
                    text = "Map Location",
                    style = MaterialTheme.typography.titleLarge
                )

                Text(campsite.mapLocation)

                HorizontalDivider()

                Text(
                    text = "QR Code",
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = campsite.qr.ifBlank {
                        "QR code not available."
                    }
                )

                HorizontalDivider()

                Button(
                    onClick = {
                        // TODO: Connect to Lottery backend
                    },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Join Active Lottery")
                }
            }
        }
    }
}
