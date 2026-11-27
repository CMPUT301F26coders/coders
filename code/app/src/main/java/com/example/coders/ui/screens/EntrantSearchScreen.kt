/*
 * Purpose/Role:
 * The primary discovery screen for Entrants
 * Integrates the CampsiteFilterDialog and
 * local text search to filter the list of campsites
 *
 * Outstanding Issues:
 * - no critical outstanding issues
 */

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
import com.example.coders.SignedInProfile
import com.example.coders.Reservation
import com.example.coders.ReservationRepo

/**
 * Displays searchable and filterable list of all available campsites for entrants
 *
 * The screen combines a direct text search (matching against campsite names and locations)
 * with a detailed filter dialog (location, dates, guests). Result dataset is
 * displayed in a lis. Allows the user to tap a campsite for more details.
 *
 * @param navController Navigation controller used to route to the campsite details screen
 */
@Composable
fun EntrantSearchScreen(navController: NavController) {


    val signedInProfile by SignedInProfile().rememberSignedInProfile()

    val campsiteRepo = remember { CampsiteRepo() }
    val campsites = campsiteRepo.getAllCampsites()
    val reservationRepo = remember { ReservationRepo() }
    var reservations by remember { mutableStateOf<List<Reservation>>(emptyList()) }
    var reservationsLoaded by remember { mutableStateOf(false) }
    var reservationsError by remember { mutableStateOf<String?>(null) }

    DisposableEffect(reservationRepo) {
        val registration = reservationRepo.listenForAllReservations(
            onUpdate = { currentReservations ->
                reservations = currentReservations
                reservationsLoaded = true
                reservationsError = null
            },
            onError = { error ->
                reservationsError = error.message ?: "Unable to check reservation availability."
                reservationsLoaded = true
            }
        )
        onDispose { registration.remove() }
    }

    var searchText by remember { mutableStateOf("") }

    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf("") }
    var selectedStartDate by remember { mutableStateOf("") }
    var selectedEndDate by remember { mutableStateOf("") }
    var selectedGuests by remember { mutableIntStateOf(0) }

    // Apply location, date, and guest filters,
    // then search by campsite name or location.
    val hasAvailabilityDates = selectedStartDate.isNotBlank() || selectedEndDate.isNotBlank()
    val availabilityMessage = when {
        hasAvailabilityDates && reservationsError != null -> reservationsError
        hasAvailabilityDates && !reservationsLoaded -> "Checking reservation availability..."
        else -> null
    }
    val filteredCampsites = if (availabilityMessage == null) filterCampsites(
        campsites = campsites,
        location = selectedLocation,
        guests = selectedGuests,
        startDate = selectedStartDate,
        endDate = selectedEndDate,
        reservations = reservations
    ).filter { campsite ->
        signedInProfile?.id != campsite.campOwnerId &&
            (searchText.isBlank() || campsite.name.contains(
                    searchText.trim(),
                    ignoreCase = true
                ) || campsite.location.contains(
                    searchText.trim(),
                    ignoreCase = true
                ))
    } else emptyList()

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

            if (availabilityMessage != null) {
                Text(
                    text = availabilityMessage,
                    color = if (reservationsError != null) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            } else if (filteredCampsites.isEmpty()) {

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
                                navController.navigate(
                                    "campsiteDetails/${android.net.Uri.encode(campsite.id)}"
                                )
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = campsite.name,
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(text = "Location: ${campsite.location}")
                                    Text(text = "Maximum Occupancy: ${campsite.capacity} guests")
                                    Text(text = "Rating: ${campsite.rating ?: "Not rated"}")
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                CampsiteQrCodeImage(
                                    campsite = campsite,
                                    size = 72.dp
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
