/**
 * Defines the home screen for entrants and campsite owners.
 *
 *
 * Outstanding issues: Notifications are currently hardcoded,
 * and the campsite owner's home screen is a placeholder.
 */

package com.example.coders.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.coders.UserSession

/**
 * Displays the appropriate home screen based on the current user's role.
 *
 * @param navController Used to navigate between screens.
 */
@Composable
fun HomeScreen(navController: NavController) {
    val isOwner = UserSession.isOwner

    Scaffold(
        bottomBar = {
            AppBottomBar(navController = navController)
        }
    ) { innerPadding ->

        if (isOwner) {
            OwnerHomeContent(
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            EntrantHomeContent(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

/**
 * Displays the campsite owner's home screen content.
 *
 * @param modifier Modifier used to customize the layout.
 */
@Composable
fun OwnerHomeContent(modifier: Modifier = Modifier) {
    Text(
        text = "Campsite Owner Home",
        modifier = modifier
    )
}

/**
 * Displays the entrant's home screen with notifications and
 * a navigation option for policies and guidelines.
 *
 * @param navController Used to navigate to other screens.
 * @param modifier Modifier used to customize the layout.
 */
@Composable
fun EntrantHomeContent(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Home",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = { navController.navigate("policies") },
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Policies & Guidelines")
        }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = "Notifications",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        EntrantNotification(
            title = "You Won!",
            message = "You won the Pine Lake Campsite lottery."
        )

        Spacer(modifier = Modifier.height(20.dp))

        EntrantNotification(
            title = "Reservation Confirmed",
            message = "Your Lakeview reservation is confirmed."
        )

        Spacer(modifier = Modifier.height(20.dp))

        EntrantNotification(
            title = "Booking Window Closing!",
            message = "Your booking deadline is approaching."
        )

        Spacer(modifier = Modifier.height(20.dp))

        EntrantNotification(
            title = "Waitlist at Max Capacity",
            message = "The Pine Lake waiting list is full."
        )

        Spacer(modifier = Modifier.height(20.dp))

        EntrantNotification(
            title = "Lost Lottery",
            message = "You were not selected for Lakeview."
        )
    }
}

/**
 * Displays a notification card containing a title and message.
 *
 * @param title The notification title.
 * @param message The notification content.
 */
@Composable
fun EntrantNotification(
    title: String,
    message: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),

        shape = RoundedCornerShape(14.dp),

        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),

        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = message,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}
