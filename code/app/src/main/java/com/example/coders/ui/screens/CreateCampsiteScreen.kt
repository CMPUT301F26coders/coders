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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.coders.CampOwner
import com.example.coders.Campsite
import com.example.coders.CampsiteRepo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.coders.SignedInProfile


/**
 * Displays a form to collect information to create a new campsite (name, location, description, capacity)
 * Records that campsite in the database
 *
 * @param navController navigation to return to previous screen
 * @param campsiteRepo the repo for database creation of the new campsite
 */
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


    var showMissingInfoError by remember { mutableStateOf(false) }


    // missing req info message screen
    if (showMissingInfoError) {
        AlertDialog(
            onDismissRequest = {  },
            title = { Text("Missing Information") },
            text = { Text("You did not provide all required information (Name, Location, Description, and Occupancy).") },
            confirmButton = {
                TextButton(onClick = {
                    showMissingInfoError = false
                }) {
                    Text("Retry")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showMissingInfoError = false
                    navController.popBackStack()
                }) {
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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Create Campsite",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // the collection form
            CampsiteInputField(
                label = "Enter Campsite Name",
                placeholder = "Name",
                value = name,
                onValueChange = { name = it },
                testTag = "input_Name"
            )
            Spacer(modifier = Modifier.height(12.dp))

            CampsiteInputField(
                label = "Enter Campsite Location",
                placeholder = "Location",
                value = location,
                onValueChange = { location = it },
                testTag = "input_Location"
            )
            Spacer(modifier = Modifier.height(12.dp))

            CampsiteInputField(
                label = "Enter Campsite Description",
                placeholder = "Description",
                value = description,
                onValueChange = { description = it },
                testTag = "input_Description"
            )
            Spacer(modifier = Modifier.height(12.dp))

            CampsiteInputField(
                label = "Enter Maximum Occupancy",
                placeholder = "Value",
                value = occupancy,
                onValueChange = { occupancy = it },
                testTag = "input_Occupancy"
            )

            CampsiteInputField(
                label = "Enter Campsite Rules & Policies",
                placeholder = "Rules & Policies",
                value = rules,
                onValueChange = { rules = it }
            )
            Spacer(modifier = Modifier.height(20.dp))

            // stuff for images and tags (buttons dont work right now):
            Text(text = "Optional: Campsite Image and Tags", fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Upload, contentDescription = "Upload", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Upload Images", color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))


                OutlinedButton(onClick = {}, shape = RoundedCornerShape(8.dp), modifier = Modifier.height(36.dp)) { Text("Tag", fontSize = 12.sp) }
                Spacer(modifier = Modifier.width(4.dp))
                OutlinedButton(onClick = {}, shape = RoundedCornerShape(8.dp), modifier = Modifier.height(36.dp)) { Text("Tag", fontSize = 12.sp) }
                Spacer(modifier = Modifier.width(4.dp))

                IconButton(onClick = {  }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Add Tag")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // submit button
            Button(
                onClick = {
                    val capacityInt = occupancy.toIntOrNull()

                    //making sure nothing left blank
                    if (name.isBlank() || location.isBlank() || description.isBlank() || capacityInt == null || campOwner == null) {
                        showMissingInfoError = true
                    } else {
                        // creating campsite
                        val newCampsite = Campsite(
                            name = name,
                            location = location,

                            description = if (rules.isNotBlank()) "$description\n\nRules: $rules" else description,
                            capacity = capacityInt,
                            mapLocation = "0.0,0.0", //place holder coords, logic not implemented yet
                            campOwner = campOwner,
                            campOwnerId = campOwner.id,
                            tags = tags
                        )
                        if (campsiteRepo == null) {
                            Toast.makeText(context, "Campsite repository unavailable", Toast.LENGTH_SHORT).show()
                        } else {
                            campsiteRepo.addCampsite(newCampsite) { error ->
                                if (error == null) {
                                    Toast.makeText(context, "Campsite Registered", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                } else {
                                    Toast.makeText(context, "Failed to save campsite", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface)
            ) {
                Text("Confirm Campsite", color = MaterialTheme.colorScheme.surface, fontSize = 16.sp)
            }
        }
    }
}

/**
 * text input field used in the form for campsite creation
 *
 * @param label The label text displayed above the input field.
 * @param placeholder the text thats inside the input field before the user types anything
 * @param value the string
 * @param onValueChange when the users input changes
 * @param testTag for testing
 */

//logic for input field assisted by Google, Gemini, "How do I create an input field for a campsite so that I am able to test it"
// 2026-11-06
@Composable
fun CampsiteInputField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    testTag: String = "" // for test
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )
    }
}

/**
 * UI preview of the CreateCampsiteScreen
 */
@Preview(showBackground = true)
@Composable
fun PreviewCreateCampsiteScreen() {
    //  dummy NavController, dummy owner, and null for the repo
    CreateCampsiteScreen(
        navController = rememberNavController(),
        campsiteRepo = null
    )
}