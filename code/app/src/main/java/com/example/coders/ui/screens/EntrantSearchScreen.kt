package com.example.coders.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.coders.CampsiteRepo
import com.example.coders.filterCampsites

@Composable
fun EntrantSearchScreen(navController: NavController) {

    val campsiteRepo = remember { CampsiteRepo() }
    val campsites = campsiteRepo.getAllCampsites()

    var searchText by remember { mutableStateOf("") }

    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf("") }
    var selectedStartDate by remember { mutableStateOf("") }
    var selectedEndDate by remember { mutableStateOf("") }
    var selectedGuests by remember { mutableIntStateOf(0) }

    // Apply location, date, and guest filters,
    // then search by campsite name or location.
    val filteredCampsites = filterCampsites(
        campsites = campsites,
        location = selectedLocation,
        guests = selectedGuests,
        startDate = selectedStartDate,
        endDate = selectedEndDate
    ).filter { campsite ->
        searchText.isBlank() ||
                campsite.name.contains(
                    searchText.trim(),
                    ignoreCase = true
                ) ||
                campsite.location.contains(
                    searchText.trim(),
                    ignoreCase = true
                )
    }

    Scaffold(
        bottomBar = {
            AppBottomBar(navController)
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {

            Text(
                text = "Search Campsites",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = {
                    Text("Search Campsites...")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = "Available Campsites",
                    style = MaterialTheme.typography.titleMedium
                )

                OutlinedButton(
                    onClick = {
                        showFilterDialog = true
                    }
                ) {
                    Text("Filter")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredCampsites.isEmpty()) {

                Text(
                    text = "No campsites available.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

            } else {

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    items(
                        items = filteredCampsites
                    ) { campsite ->

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                // TODO: Open campsite details
                            }
                        ) {

                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {

                                Text(
                                    text = campsite.name,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Spacer(
                                    modifier = Modifier.height(6.dp)
                                )

                                Text(
                                    text = "Location: ${campsite.location}"
                                )

                                Text(
                                    text = "Maximum Occupancy: ${campsite.capacity} guests"
                                )

                                Text(
                                    text = "Rating: ${campsite.rating ?: "Not rated"}"
                                )
                            }
                        }
                    }
                }
            }
        }
    }



    if (showFilterDialog) {
        CampsiteFilterDialog(
            initialLocation = selectedLocation,
            initialStartDate = selectedStartDate,
            initialEndDate = selectedEndDate,
            initialGuests = selectedGuests,
            onDismiss = {
                showFilterDialog = false
            },
            onApply = { location, startDate, endDate, guests ->
                selectedLocation = location
                selectedStartDate = startDate
                selectedEndDate = endDate
                selectedGuests = guests
                showFilterDialog = false
            },
            onClear = {
                selectedLocation = ""
                selectedStartDate = ""
                selectedEndDate = ""
                selectedGuests = 0
                showFilterDialog = false
            }
        )
    }

}
