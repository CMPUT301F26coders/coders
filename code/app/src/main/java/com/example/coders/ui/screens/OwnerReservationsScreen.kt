package com.example.coders.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun OwnerReservationsScreen(navController: NavController) {
    var isNavigating by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        TextButton(
            onClick = {
                if (!isNavigating) {
                    isNavigating = true
                    navController.popBackStack()

                }
            }
        ) {
            Text("Back")
        }
        var selectedTab by remember { mutableStateOf("Present") }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Past", "Present", "Cancelled").forEach { tab ->
                Button(
                    onClick = { selectedTab = tab },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == tab)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = if (selectedTab == tab)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text(tab)
                }
            }
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

            when(selectedTab){
                "Past" -> {
                    Column(
                    modifier = Modifier
                        .padding(20.dp)
                ) {
                        Text("Entrant: ", fontWeight = FontWeight.Bold)
                        Text("Mark Smith")

                        Text("Campsite Name", fontWeight = FontWeight.Bold)
                        Text("Pine Lake Campsite")

                        Spacer(modifier = Modifier.height(20.dp))

                        Text("Location", fontWeight = FontWeight.Bold)
                        Text("Jasper, Alberta")

                        Spacer(modifier = Modifier.height(20.dp))

                        Text("Dates", fontWeight = FontWeight.Bold)
                        Text("September 10 - 12, 2026")
                    }
                }
            }
        }
    }
}
