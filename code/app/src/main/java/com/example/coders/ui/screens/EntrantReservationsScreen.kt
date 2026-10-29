
package com.example.coders.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun EntrantReservationsScreen(navController: NavController) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        TextButton(
            onClick = {
                navController.popBackStack()
            }
        ) {
            Text("Back")
        }

        Text(
            text = "Your Reservations",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {

            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text("Campsite Name", fontWeight = FontWeight.Bold)
                Text("Pine Lake Campsite")

                Spacer(modifier = Modifier.height(20.dp))

                Text("Location", fontWeight = FontWeight.Bold)
                Text("Jasper, Alberta")

                Spacer(modifier = Modifier.height(20.dp))

                Text("Dates", fontWeight = FontWeight.Bold)
                Text("September 10 - 12, 2026")

                Spacer(modifier = Modifier.height(20.dp))

                Text("Guests", fontWeight = FontWeight.Bold)
                Text("2")

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        // Map screen will be added later
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View On Map")
                }
            }
        }

        Spacer(modifier = Modifier.height(25.dp))

        Text(
            text = "Campsite Owner",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "John Smith",
            fontSize = 20.sp
        )

        Button(
            onClick = {
                ChatData.owner = "John Smith"
                navController.navigate("chat")
            }
        ) {
            Text("Message")
        }
    }
}
