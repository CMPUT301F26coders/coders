
package com.example.coders.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.coders.R

@Composable
fun EntrantMessagesScreen(navController: NavController) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Button(
            onClick = {
                navController.popBackStack()
            }
        ) {
            Text("Back")
        }

        Text(
            text = "Messages",
            fontSize = 35.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(25.dp))

        MessageBox(
            name = "Pine Lake Campsite",
            owner = "John Smith",
            message = "Congratulations!, You won the campsite lottery!",
            date = "10:30 AM",
            picture = R.drawable.ic_launcher_foreground,
            navController = navController
        )

        MessageBox(
            name = "Mountain View Camp",
            owner = "Sarah Lee",
            message = "Hello!, Thanks for your interest in this campsite!",
            date = "Yesterday",
            picture = R.drawable.ic_launcher_foreground,
            navController = navController
        )

        MessageBox(
            name = "Forest Hill Camp",
            owner = "Mike Brown",
            message = "Let me know if you have any questions!",
            date = "Oct 12",
            picture = R.drawable.ic_launcher_foreground,
            navController = navController
        )

        MessageBox(
            name = "Riverside Campsite",
            owner = "Emma Wilson",
            message = "Your reservations details are ready!",
            date = "Oct 10",
            picture = R.drawable.ic_launcher_foreground,
            navController = navController
        )
    }
}

@Composable
fun MessageBox(
    name: String,
    owner: String,
    message: String,
    date: String,
    picture: Int,
    navController: NavController
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {

        OutlinedButton(
            onClick = {
                ChatData.owner = owner
                navController.navigate("chat")
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Image(
                    painter = painterResource(picture),
                    contentDescription = name,
                    modifier = Modifier.size(60.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(name, fontWeight = FontWeight.Bold)
                    Text(owner)
                    Text(message)
                    Text(date)
                }
            }
        }

        if (name == "Pine Lake Campsite") {

            Button(
                onClick = {
                    navController.navigate("reservations")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Application →")
            }
        }
    }
}
