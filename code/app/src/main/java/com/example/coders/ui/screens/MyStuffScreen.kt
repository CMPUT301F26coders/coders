/**
 * Defines the My Stuff screen for entrants and campsite owners.
 *
 * Displays role-specific navigation options
 * for managing campsites, bookings, and reservations.
 *
 * Outstanding issues: Entrant waitlist navigation is currently
 * commented out cuz its not implemented.
 */

package com.example.coders.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.coders.UserSession


/**
 * Displays the My Stuff screen with navigation options 
 * based on the current user's role.
 *
 * @param navController Used to navigate to the appropriate management screens.
 */
@Composable
fun MyStuffScreen(navController: NavController) {

    val isOwner = UserSession.isOwner

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

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "My Stuff",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isOwner) {
                    "Manage your campsites and reservations"
                } else {
                    "Manage your bookings, waitlists and reservations"
                },
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(30.dp))


            if (isOwner) {

                // CAMPSITE OWNER BUTTONS

                MyStuffMenuButton(
                    title = "My Campsites",
                    subtitle = "View and manage",
                    icon = Icons.Default.Landscape,
                    onClick = {
                        navController.navigate("ownercampsites")
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                MyStuffMenuButton(
                    title = "Reservations",
                    subtitle = "View and manage",
                    icon = Icons.Default.Event,
                    onClick = {
                        navController.navigate("ownerreservations")
                    }
                )

            } else {

                // ENTRANT BUTTONS

                MyStuffMenuButton(
                    title = "My Bookings",
                    subtitle = "View and manage",
                    icon = Icons.Default.Landscape,
                    onClick = {
                        navController.navigate("entrantreservations")
                    }
                )

                //Spacer(modifier = Modifier.height(20.dp))

                /*MyStuffMenuButton(
                    title = "Waitlists",
                    subtitle = "Active lotteries",
                    icon = Icons.Default.List,
                    onClick = {
                        navController.navigate("waitlists")
                    }
                )*/

                //Spacer(modifier = Modifier.height(20.dp))

            }
        }
    }
}


/**
 * Displays a menu button with an icon, title, and subtitle.
 *
 * @param title The main text displayed on the button.
 * @param subtitle The supporting text displayed below the title.
 * @param icon The icon displayed beside the text.
 * @param onClick Action performed when the button is clicked.
 */
@Composable
fun MyStuffMenuButton(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        shape = RoundedCornerShape(25.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.LightGray,
            contentColor = Color.Black
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(25.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = subtitle,
                    fontSize = 14.sp
                )
            }
        }
    }
}
