/**
 * Displays a campsite map using a static image.
 *
 * Outstanding issues: The map is hardcoded to Pine Lake Campground
 * rather than dynamically displaying the selected campsite's map.
 */

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
 * Displays the map and a back navigation button.
 *
 * @param navController Used to navigate back to the previous screen.
 */
@Composable
fun MiniMapScreen(navController: NavController) {
    var isNavigating by remember { mutableStateOf(false) }
    Scaffold(
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {

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

            Text(
                text = "Pine Lake Campground Map",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

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
