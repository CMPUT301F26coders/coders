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
import com.example.coders.ProfileRepo
import com.example.coders.ImageRepo
import com.example.coders.Profile
import com.example.coders.SignedInProfile
import com.example.coders.Image as ImageRecord
import com.example.coders.ProfileImageRepo
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.coders.dataUrlToImageBitmap
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.Alignment

/**
 * Displays searchable and filterable list of all available campsites for entrants
 *
 * The screen combines a direct text search (matching against campsite names and locations)
 * with a detailed filter dialog (location, dates, guests). Result dataset is
 * displayed in a lis. Allows the user to tap a campsite for more details.
 *
 * @param navController Navigation controller used to route to the campsite details screen
 */

private enum class SearchTab(
    val label: String,
) {
    Campsites("Campsites"),
    Profiles("Profiles"),
    Images("Images")
}


@Composable
fun DeleteButton(onDelete: () -> Unit) {
    IconButton(onClick = onDelete) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete",
            tint = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun DeleteConfirmationDialog(
    itemName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Confirm Deletion")
        },
        text = {
            Text("Are you sure you want to delete $itemName?")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SearchScreen(navController: NavController) {



    val campsiteRepo = remember { CampsiteRepo() }
    val profileRepo = remember { ProfileRepo() }
    val imageRepo = remember { ImageRepo() }
    val profileImageRepo = remember { ProfileImageRepo() }


    val campsites = campsiteRepo.getAllCampsites()
    val images = imageRepo.getAllImages()

    var profiles by remember {
        mutableStateOf<List<Profile>>(emptyList())
    }

    var itemToDelete by remember {
        mutableStateOf<Pair<String, String>?>(null)
    }

    val signedInProfile by SignedInProfile().rememberSignedInProfile()

    LaunchedEffect(Unit) {
        profileRepo.getAllProfiles { result, error ->
            if (error == null && result != null) {
                profiles = result
            }
        }
    }



    var searchText by remember { mutableStateOf("") }

    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf("") }
    var selectedStartDate by remember { mutableStateOf("") }
    var selectedEndDate by remember { mutableStateOf("") }
    var selectedGuests by remember { mutableIntStateOf(0) }

    var selectedTab by remember {
        mutableStateOf(SearchTab.Campsites)
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
                text = "Browse ${selectedTab.label}",
                style = MaterialTheme.typography.headlineMedium
            )


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SearchTab.entries.forEach { tab ->
                    Button(
                        onClick = { selectedTab = tab },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == tab) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.secondaryContainer
                            },
                            contentColor = if (selectedTab == tab) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            }
                        )
                    ) {
                        Text(tab.label)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                SearchTab.Campsites -> {

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
                                        navController.navigate(
                                            "campsiteDetails/${android.net.Uri.encode(campsite.id)}"
                                        )
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = campsite.name,
                                                style = MaterialTheme.typography.titleMedium
                                            )

                                            Text("Location: ${campsite.location}")
                                            Text("Maximum Occupancy: ${campsite.capacity} guests")
                                            Text("Rating: ${campsite.rating ?: "Not rated"}")
                                        }

                                        DeleteButton(
                                            onDelete = {
                                                itemToDelete = "campsite" to campsite.id
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                SearchTab.Profiles -> {
                    // Apply location, date, and guest filters,
                    // then search by campsite name or location.
                    val filteredProfiles = profiles.filter { profile ->
                        profile.id != signedInProfile?.id && //doesnt show yourself
                        searchText.isBlank() ||
                                profile.name.contains(
                                    searchText.trim(),
                                    ignoreCase = true
                                )
                    }
                    // Profile search UI

                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        label = {
                            Text("Search Profiles...")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )


                    Spacer(modifier = Modifier.height(16.dp))

                    if (filteredProfiles.isEmpty()) {

                        Text(
                            text = "No profiles available.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                    } else {

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            items(
                                items = filteredProfiles
                            ) { profile ->

                                var profileImage by remember(profile.id) {
                                    mutableStateOf<ImageRecord?>(null)
                                }

                                LaunchedEffect(profile.id) {
                                    profileImageRepo.getProfileImage(profile.id) { image, error ->
                                        if (error == null) {
                                            profileImage = image
                                        }
                                    }
                                }

                                val bitmap = remember(profileImage?.url) {
                                    profileImage?.url?.let(::dataUrlToImageBitmap)
                                }

                                Card(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            if (bitmap != null) {
                                                Image(
                                                    bitmap = bitmap,
                                                    contentDescription = "Profile picture",
                                                    modifier = Modifier.size(80.dp),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.AccountCircle,
                                                    contentDescription = "Default profile picture",
                                                    modifier = Modifier.size(80.dp)
                                                )
                                            }

                                            Text(
                                                text = profile.name,
                                                style = MaterialTheme.typography.titleMedium
                                            )

                                            Text("Email: ${profile.email}")
                                            Text("Phone Number: ${profile.phone}")
                                            Text("Type: ${profile.type}")
                                        }

                                        DeleteButton(
                                            onDelete = {
                                                itemToDelete = "profile" to profile.id
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                SearchTab.Images -> {
                    if (images.isEmpty()) {

                        Text(
                            text = "No images available.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                    } else {

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            items(
                                items = images
                            ) { image ->


                                val bitmap = remember(image.url) {
                                    image.url.let(::dataUrlToImageBitmap)
                                }

                                Card(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (bitmap != null) {
                                            Image(
                                                bitmap = bitmap,
                                                contentDescription = "Image",
                                                modifier = Modifier.size(80.dp),
                                                contentScale = ContentScale.Crop
                                            )
                                        }

                                        DeleteButton(
                                            onDelete = {
                                                itemToDelete = "image" to image.id
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    itemToDelete?.let { (type, id) ->
        DeleteConfirmationDialog(
            itemName = type,
            onConfirm = {
                when (type) {
                    "campsite" -> campsiteRepo.deleteCampsite(id)
                    "profile" -> profileRepo.deleteProfile(id)
                    "image" -> imageRepo.deleteImage(id)
                }

                itemToDelete = null
            },
            onDismiss = {
                itemToDelete = null
            }
        )
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
