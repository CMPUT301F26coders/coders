
package com.example.coders.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Event
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

@Composable
fun EntrantMyStuffScreen(navController: NavController) {

    // ADDED: SAME HOTBAR AS HOME SCREEN
    Scaffold(
        bottomBar = {
            EntrantBottomBar(navController)
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(10.dp)
        ) {

            // BACK BUTTON
            Button(
                onClick = {
                    navController.popBackStack()
                }
            ) {
                Text("Back")
            }

            // PAGE TITLE
            Text(
                text = "My Stuff",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            // HEADER AND DESCRIPTION
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Manage Your Campsites",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.offset(y = (-30).dp)
                )

                Text(
                    text = "Your properties, lotteries, and reservations",
                    fontSize = 15.sp,
                    modifier = Modifier.offset(y = 0.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // MY BOOKINGS BUTTON
            Button(
                onClick = {
                    navController.navigate("bookings")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .offset(y = (-60).dp),
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
                        imageVector = Icons.Default.Landscape,
                        contentDescription = "Bookings",
                        modifier = Modifier.size(40.dp)
                    )

                    Spacer(modifier = Modifier.width(25.dp))

                    Column {
                        Text(
                            text = "My Bookings",
                            fontSize = 28.sp
                        )

                        Text(
                            text = "View and manage",
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // WAITLISTS BUTTON
            Button(
                onClick = {
                    navController.navigate("waitlists")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .offset(y = (-40).dp),
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
                        imageVector = Icons.Default.List,
                        contentDescription = "Waitlists",
                        modifier = Modifier.size(40.dp)
                    )

                    Spacer(modifier = Modifier.width(25.dp))

                    Column {
                        Text(
                            text = "Waitlists",
                            fontSize = 28.sp
                        )

                        Text(
                            text = "Active lotteries",
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // RESERVATIONS BUTTON
            Button(
                onClick = {
                    navController.navigate("reservations")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .offset(y = (-20).dp),
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
                        imageVector = Icons.Default.Event,
                        contentDescription = "Reservations",
                        modifier = Modifier.size(40.dp)
                    )

                    Spacer(modifier = Modifier.width(25.dp))

                    Column {
                        Text(
                            text = "Reservations",
                            fontSize = 28.sp
                        )

                        Text(
                            text = "View and manage",
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}
