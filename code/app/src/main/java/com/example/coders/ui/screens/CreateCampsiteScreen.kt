package com.example.coders.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.tooling.preview.Preview
import com.example.coders.CampOwner
import com.example.coders.Campsite
import com.example.coders.CampsiteRepo
import com.example.coders.SignedInProfile
import kotlinx.coroutines.launch

@Composable
fun CreateCampsiteScreen(
    navController: NavController,
    campsiteRepo: CampsiteRepo?
) {
    val profile by remember { SignedInProfile() }.rememberSignedInProfile()
    val campOwner = profile as? CampOwner
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var occupancy by remember { mutableStateOf("") }
    var rules by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf(listOf<String>()) }

    // Selected map coordinates
    var selectedLatitude by remember { mutableStateOf<Double?>(null) }
    var selectedLongitude by remember { mutableStateOf<Double?>(null) }

    // Address search states
    var isSearchingAddress by remember { mutableStateOf(false) }
    var addressSearchMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    var showMissingInfoError by remember {
        mutableStateOf(false)
    }

    // Missing information dialog
    if (showMissingInfoError) {
        AlertDialog(
            onDismissRequest = {
                showMissingInfoError = false
            },
            title = {
                Text("Missing Information")
            },
            text = {
                Text(
                    "Please provide the campsite name, location, description, " +
                            "occupancy, and select a location on the map."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showMissingInfoError = false
                    }
                ) {
                    Text("Retry")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showMissingInfoError = false
                        navController.popBackStack()
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        bottomBar = {
            AppBottomBar(navController = navController)
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {

            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = {
                        navController.popBackStack()
                    }
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                Text(
                    text = "Create Campsite",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campsite Name
            CampsiteInputField(
                label = "Enter Campsite Name",
                placeholder = "Name",
                value = name,
                onValueChange = { name = it },
                testTag = "input_Name"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Campsite Address
            CampsiteInputField(
                label = "Enter Campsite Location",
                placeholder = "e.g. Pine Lake, Alberta, Canada",
                value = location,
                onValueChange = {
                    location = it

                    // Address changed: old coordinates are no longer valid
                    selectedLatitude = null
                    selectedLongitude = null
                    addressSearchMessage = null
                },
                testTag = "input_Location"
            )

            // Find Address Button
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {

                    isSearchingAddress = true
                    addressSearchMessage = null

                    coroutineScope.launch {

                        try {
                            val coordinates =
                                AddressGeocoder.findCoordinates(
                                    context = context,
                                    address = location
                                )

                            if (coordinates != null) {

                                selectedLatitude = coordinates.first
                                selectedLongitude = coordinates.second

                                addressSearchMessage =
                                    "Address found! You can adjust the location on the map."

                            } else {

                                selectedLatitude = null
                                selectedLongitude = null

                                addressSearchMessage =
                                    "Address not found. Try adding the city and province."
                            }

                        } catch (e: Exception) {

                            selectedLatitude = null
                            selectedLongitude = null

                            addressSearchMessage =
                                "Unable to find this address. Please try again."

                        } finally {
                            isSearchingAddress = false
                        }
                    }
                },
                enabled = location.isNotBlank() && !isSearchingAddress
            ) {

                if (isSearchingAddress) {

                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text("Searching...")

                } else {
                    Text("Find on Map")
                }
            }

            addressSearchMessage?.let {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = it,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Description
            CampsiteInputField(
                label = "Enter Campsite Description",
                placeholder = "Description",
                value = description,
                onValueChange = { description = it },
                testTag = "input_Description"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Maximum Occupancy
            CampsiteInputField(
                label = "Enter Maximum Occupancy",
                placeholder = "Value",
                value = occupancy,
                onValueChange = { occupancy = it },
                testTag = "input_Occupancy"
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Map
            Text(
                text = "Select Campsite Location",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Search for an address first, then tap the map to adjust the exact campsite location.",
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            CampsiteMapPicker(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                initialLatitude = selectedLatitude,
                initialLongitude = selectedLongitude,
                onLocationSelected = { latitude, longitude ->
                    selectedLatitude = latitude
                    selectedLongitude = longitude
                }
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Map data © OpenStreetMap contributors",
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (selectedLatitude != null && selectedLongitude != null) {

                Text(
                    text = "Selected Location: %.6f, %.6f".format(
                        selectedLatitude!!,
                        selectedLongitude!!
                    ),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )

            } else {

                Text(
                    text = "No location selected yet.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Rules and Policies
            CampsiteInputField(
                label = "Enter Campsite Rules & Policies",
                placeholder = "Rules & Policies",
                value = rules,
                onValueChange = { rules = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Images and Tags
            Text(
                text = "Optional: Campsite Image and Tags",
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {

                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Default.Upload,
                        contentDescription = "Upload",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        "Upload Images",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = {},
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Tag", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(4.dp))

                OutlinedButton(
                    onClick = {},
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Tag", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = {},
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Tag"
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Confirm Campsite
            Button(
                onClick = {

                    val capacityInt = occupancy.toIntOrNull()

                    if (
                        name.isBlank() ||
                        location.isBlank() ||
                        description.isBlank() ||
                        capacityInt == null ||
                        capacityInt <= 0 ||
                        campOwner == null ||
                        selectedLatitude == null ||
                        selectedLongitude == null
                    ) {
                        showMissingInfoError = true

                    } else {

                        val newCampsite = Campsite(
                            name = name,
                            location = location,
                            description = if (rules.isNotBlank()) {
                                "$description\n\nRules: $rules"
                            } else {
                                description
                            },
                            capacity = capacityInt,

                            // Save the actual selected coordinates
                            mapLocation = "${selectedLatitude},${selectedLongitude}",

                            campOwner = campOwner,
                            campOwnerId = campOwner.id,
                            tags = tags
                        )

                        if (campsiteRepo == null) {

                            Toast.makeText(
                                context,
                                "Campsite repository unavailable",
                                Toast.LENGTH_SHORT
                            ).show()

                        } else {

                            campsiteRepo.addCampsite(newCampsite) { error ->

                                if (error == null) {

                                    Toast.makeText(
                                        context,
                                        "Campsite Registered",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    navController.popBackStack()

                                } else {

                                    Toast.makeText(
                                        context,
                                        "Failed to save campsite",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(
                    "Confirm Campsite",
                    color = MaterialTheme.colorScheme.surface,
                    fontSize = 16.sp
                )
            }
        }
    }
}

// Reusable input field
@Composable
fun CampsiteInputField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    testTag: String = ""
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        Text(
            text = label,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    placeholder,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = 0.5f
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )
    }
}

// Preview
@Preview(showBackground = true)
@Composable
fun PreviewCreateCampsiteScreen() {
    CreateCampsiteScreen(
        navController = rememberNavController(),
        campsiteRepo = null
    )
}