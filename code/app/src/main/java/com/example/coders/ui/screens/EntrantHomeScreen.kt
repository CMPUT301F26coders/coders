
package com.example.coders.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun EntrantHomeScreen(navController: NavController) {

    Scaffold(
        bottomBar = {
            EntrantBottomBar(navController)
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                onClick = {
                    navController.navigate("policies")
                },
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
}

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

@Composable
fun EntrantBottomBar(navController: NavController) {

    NavigationBar {

        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = {
                Icon(
                    Icons.Default.Home,
                    contentDescription = "Home"
                )
            }
        )

        NavigationBarItem(
            selected = false,
            onClick = {
                navController.navigate("messages")
            },
            icon = {
                Icon(
                    Icons.Default.Email,
                    contentDescription = "Messages"
                )
            }
        )

        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = {
                Icon(
                    Icons.Default.Inventory2,
                    contentDescription = "My Stuff"
                )
            }
        )

        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search"
                )
            }
        )

        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Profile"
                )
            }
        )
    }
}
