package com.example.coders.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

/**
 * Displays the "My Stuff" navigation hub screen for a Campsite Owner.
 */
@Composable
fun OwnerMyStuffScreen(navController: NavController) {
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
                .padding(horizontal = 8.dp, vertical = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            Surface(
                shape = RoundedCornerShape(2.dp),
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Text(
                    text = "Campsite owner",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "My Stuff",
                fontSize = 38.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(1.dp))

            Text(
                text = "Manage your campsites. Your properties, lotteries and reservations.",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.error,
                lineHeight = 12.sp
            )

            Spacer(modifier = Modifier.height(35.dp))

            OwnerMenuCard(
                title = "My Campsites",
                subtitle = "View and manage",
                onClick = { navController.navigate("owner_campsites") }
            )

            Spacer(modifier = Modifier.height(4.dp))

            OwnerMenuCard(
                title = "Lotteries",
                subtitle = "Registration, entrants and results",
                onClick = { navController.navigate("owner_lotteries") }
            )

            Spacer(modifier = Modifier.height(22.dp))

            OwnerMenuCard(
                title = "Reservations",
                subtitle = "Active, past and cancelled bookings",
                onClick = { navController.navigate("owner_reservations") }
            )

            Spacer(modifier = Modifier.height(9.dp))

            OwnerMenuCard(
                title = "Sent notifications",
                subtitle = "Your notification history",
                onClick = { navController.navigate("owner_sent_notifications") }
            )
        }
    }
}

/**
 * Renders a raw clickable menu card item for the checkpoint with a title, and an onclick

 */
@Composable
fun OwnerMenuCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Light
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}