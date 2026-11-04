
package com.example.coders.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.coders.Campsite

/**
 * Displays all information about a selected campsite.
 *
 * Receives a Campsite object from Data.kt and displays
 * its information using Jetpack Compose.
 *
 * This screen only handles the user interface.
 *
 * @param navController Used to return to the previous screen.
 * @param campsite The selected campsite to display.
 */
@Composable
fun CampsiteDetailsScreen(
    navController: NavController,
    campsite: Campsite
) {

    /**
     * Main layout of the screen.
     *
     * Column places everything vertically.
     * fillMaxSize makes the layout fill the screen.
     * verticalScroll allows the user to scroll.
     * padding adds space around the content.
     */
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        /**
         * Back button.
         *
         * Returns the user to the previous screen.
         */
        TextButton(
            onClick = { navController.popBackStack() }
        ) {
            Text("Back")
        }

        Spacer(modifier = Modifier.height(12.dp))

        /**
         * Campsite name and location.
         *
         * Displays the name and location of the
         * selected campsite.
         */
        Text(
            text = campsite.name,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Text(campsite.location)

        Spacer(modifier = Modifier.height(20.dp))

        /**
         * About This Campsite section.
         *
         * Displays the campsite description
         * inside a Card.
         */
        Card(modifier = Modifier.fillMaxWidth()) {

            Column(modifier = Modifier.padding(16.dp)) {

                Text(
                    "About This Campsite",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(campsite.description)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        /**
         * Campsite Information section.
         *
         * Displays the campsite's maximum occupancy,
         * rating, map location, and owner.
         *
         * If the rating is null, the screen
         * displays "Not rated yet".
         */
        Card(modifier = Modifier.fillMaxWidth()) {

            Column(modifier = Modifier.padding(16.dp)) {

                Text(
                    "Campsite Information",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Maximum Occupancy: ${campsite.capacity} guests")

                Spacer(modifier = Modifier.height(8.dp))

                Text("Rating: ${campsite.rating ?: "Not rated yet"}")

                Spacer(modifier = Modifier.height(8.dp))

                Text("Map Location: ${campsite.mapLocation}")

                Spacer(modifier = Modifier.height(8.dp))

                Text("Campsite Owner: ${campsite.campOwner.name}")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        /**
         * Amenities and Features section.
         *
         * Checks whether the campsite has any tags.
         *
         * If the list is empty, displays a message.
         * Otherwise, loops through the tags and
         * displays each amenity on a separate line.
         */
        Card(modifier = Modifier.fillMaxWidth()) {

            Column(modifier = Modifier.padding(16.dp)) {

                Text(
                    "Amenities & Features",
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
