
package com.example.coders.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.coders.Campsite

/**
 * Displays detailed information about a selected campsite.
 *
 * Receives a Campsite object and displays its name, location,
 * description, maximum occupancy, rating, map location,
 * campsite owner, and available amenities.
 *
 * The screen uses a scrollable layout to allow users to view
 * all campsite information and provides a Back button to
 * return to the previous screen.
 *
 * The Back button prevents repeated navigation requests,
 * and the layout includes safe screen padding.
 *
 * This composable only handles the user interface and does
 * not perform database operations or modify campsite data.
 *
 * @param navController Handles navigation to the previous screen.
 * @param campsite The selected Campsite object containing
 * the information to display.
 */
@Composable
fun CampsiteDetailsScreen(
    navController: NavController,
    campsite: Campsite
) {
    // Prevent multiple back navigation requests from this screen.
    var backClicked by remember { mutableStateOf(false) }
//    var editClicked by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {

        // Back button
        TextButton(
            onClick = {
                if (!backClicked) {
                    backClicked = true
                    navController.popBackStack()
                }
            },
            enabled = !backClicked,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
        ) {
            Text("Back")
        }

//        Button(
//            onClick = {
//                if (!editClicked) {
//                    editClicked = true
//                    navController.navigate("edit_campsite/${campsite.id}")
//                }
//            },
//            enabled = !editClicked
//        ) {
//            Text("Edit Campsite")
//        }
//        Spacer(modifier = Modifier.height(12.dp))


        // Campsite name and location
        Text(
            text = campsite.name,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(campsite.location)

        Spacer(modifier = Modifier.height(20.dp))

        // About This Campsite
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "About This Campsite",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(campsite.description)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Campsite Information
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Campsite Information",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Maximum Occupancy: ${campsite.capacity} guests"
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Rating: ${campsite.rating ?: "Not rated yet"}"
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Map Location: ${campsite.mapLocation}"
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Campsite Owner: ${campsite.campOwner.name}"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Amenities & Features
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Amenities & Features",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (campsite.tags.isEmpty()) {
                    Text("No amenities listed")
                } else {
                    for (tag in campsite.tags) {
                        Text("• $tag")
                    }
                }
            }
        }
    }
}
