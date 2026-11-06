/*
 * Purpose:
 * UI that allows Camp Owners to modify existing campsite details
 * A form with the existing state of a Campsite object and pushes updates
 * to the Firestore database using CampsiteRepo
 *
 * Outstanding Issues:
 * - "Upload Images" button is a visual placeholder, Firebase integration is missing.
 * - Tag addition functionality ("+") is just a placeholder
 * - No logic to manage the campsites QR code if details change
 */

package com.example.coders.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.coders.Campsite
import com.example.coders.CampsiteRepo


//very similar functionality to CreateCampsiteScreen.kt
//This function was assisted by Google, Gemini,
// (Given the context of CreateCampsiteScreen.kt, Data.kt)
// "Help me create a file which now updates the details of a campsite"
//2026-11-05

/**
 * displays screen to edit details of a campsite that already exists
 * The form is already filled with the details the owner entered when initially creating the campsite,
 * they can edit any detail and it updates the campsite details
 *
 * @param navController to navigate back after
 * @param campsite the campsite object that has the details of the campsite before editing
 * @param campsiteRepo repo for database campsite updates
 */
@Composable
fun EditCampsiteScreen(
    navController: NavController,
    campsite: Campsite,
    campsiteRepo: CampsiteRepo? = null
) {
    var name by remember { mutableStateOf(campsite.name) }
    var location by remember { mutableStateOf(campsite.location) }
    var description by remember { mutableStateOf(campsite.description) }
    var occupancy by remember { mutableStateOf(campsite.capacity.toString()) }
    var rules by remember { mutableStateOf("") }

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
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Edit Campsite",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))


            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    EditCampsiteInputField(
                        label = "Enter Campsite Name",
                        value = name,
                        onValueChange = { name = it },
                        testTag = "edit_Name"
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    EditCampsiteInputField(
                        label = "Enter Campsite Location",
                        value = location,
                        onValueChange = { location = it },
                        testTag = "edit_Location"
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    EditCampsiteInputField(
                        label = "Enter Campsite Description",
                        value = description,
                        onValueChange = { description = it },
                        testTag = "edit_Description"
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    EditCampsiteInputField(
                        label = "Enter Maximum Occupancy",
                        value = occupancy,
                        onValueChange = { occupancy = it },
                        testTag = "edit_Occupancy"
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    EditCampsiteInputField(
                        label = "Enter Campsite Rules & Policies",
                        value = rules,
                        onValueChange = { rules = it },
                        testTag = "edit_Rules"
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Optional: Campsite Image and Tags", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { /* Image Upload placeholder */ },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = "Upload", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Upload Images", color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        AssistChip(
                            onClick = { },
                            label = { Text("Lake") }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(onClick = { /* Add tag placeholder */ }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Tag")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            //validate the input, if its not right, revert to the previous existing
                            val updatedCampsite = campsite.copy(
                                name = name,
                                location = location,
                                description = if (rules.isNotBlank()) "$description\n\nRules: $rules" else description,
                                capacity = occupancy.toIntOrNull() ?: campsite.capacity
                            )
                            campsiteRepo?.updateCampsite(updatedCampsite)


                            navController.popBackStack("ownercampsites", inclusive = false)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Confirm Campsite", fontSize = 16.sp, color = MaterialTheme.colorScheme.surface)
                    }
                }
            }
        }
    }
}

//This function was assisted by Google, Gemini,
// (Given the context of CreateCampsiteScreen.kt, Data.kt)
// "Help me create a file which now updates the details of a campsite"
//2026-11-05

/**
 * renders the text input field for the campsite edit form
 *
 * @param label the label thats above the input field
 * @param value the current input
 * @param onValueChange call back when the input is changed
 * @param testTag string tag for testing
 */
@Composable
private fun EditCampsiteInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    testTag: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )
    }
}

