
package com.example.coders.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.coders.R
import com.example.coders.SignedInProfile
import com.example.coders.toMessageSummaries

/**
 * Displays the messages screen. Allows users to navigate to the chat screen.
 *
 * @param navController The navigation controller.
 * @return The messages screen.
 */
@Composable
fun MessagesScreen(navController: NavController) {

    val profile by SignedInProfile().rememberSignedInProfile()
    val messageSummaries = profile.toMessageSummaries()

    Scaffold(
        bottomBar = {
            AppBottomBar(
                navController = navController
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {


            Text(
                text = "Messages",
                fontSize = 35.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(25.dp))

            when {
                profile == null -> CircularProgressIndicator()
                messageSummaries.isEmpty() -> Text("No conversations yet.")
                else -> messageSummaries.forEach { summary ->
                    MessageBox(
                        name = summary.campsiteName,
                        owner = summary.otherPersonName,
                        message = summary.latestMessage,
                        date = summary.latestDate,
                        picture = R.drawable.ic_launcher_foreground,
                        navController = navController
                    )
                }
            }
        }
    }
}

/** Displays one campsite conversation preview and opens its chat when selected. */
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

    }
}
