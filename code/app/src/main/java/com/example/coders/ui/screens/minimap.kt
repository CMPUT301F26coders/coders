
package com.example.coders.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.coders.R

/**
 * Displays a map image of Pine Lake Campground.
 *
 * This screen allows users to:
 * - View the campground map.
 * - Return to the previous screen using the Back button.
 *
 * The map image is loaded from the app's drawable resources.
 * The screen uses a navigation state to prevent the Back button
 * from triggering multiple navigation actions.
 *
 * @param navController Controls navigation between screens
 *                      and allows the user to return to the previous screen.
 */
@Composable
fun MiniMapScreen(navController: NavController) {

    // Tracks whether the Back button has already been pressed.
    var isNavigating by remember { mutableStateOf(false) }

    // Creates the main screen layout.
    Scaffold(
    ) { innerPadding ->

        // Arranges the Back button, title, and map vertically.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {

            // Returns to the previous screen.
            // Prevents multiple navigation actions from repeated clicks.
            Button(
                onClick = {
                    if (!isNavigating) {
                        isNavigating = true
                        navController.popBackStack()
                    }
                }
            ) {
                Text("Back")
            }

            // Displays the campground map title.
            Text(
                text = "Pine Lake Campground Map",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Displays the campground map image from drawable resources.
            Image(
                painter = painterResource(R.drawable.pine_lake_map),
                contentDescription = "Pine Lake Campground Map",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentScale = ContentScale.Fit
            )
        }
    }
}
