
package com.example.coders.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.runtime.*


/**
 * Displays the app's policies and guidelines.
 *
 * Allows users to scroll through the information
 * and return to the previous screen.
 *
 * User Story: US 01.11.01
 *
 * @param navController Handles navigation between screens.
 */
@Composable
fun PoliciesScreen(navController: NavController) {
    var isNavigating by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {

        TextButton(onClick = {
            if(!isNavigating) {
                isNavigating = true
                navController.popBackStack()
            }
        }) {
            Text("Back")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Displays the main screen title.
        Text(
            text = "Policies & Guidelines",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Displays the policies section heading.
        Text(
            text = "Policies",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Shows the app's policies and user information.
        Text(
            text = "What Data We Collect: We collect your name, email address, phone number, and location (if you allow it) to set up your account. For campsite owners, we also collect campsite details and photos.\n\n" +
                    "How We Use Your Data: We use your information to manage bookings, connect entrants with campsite owners, and send you booking confirmations.\n\n" +
                    "Data Deletion: You can delete your account at any time through the app settings, which will permanently remove your personal data.\n\n" +
                    "Liability: Our app is a marketplace that connects campers with landowners. We do not own, inspect, or manage the campsites. We are not responsible for property damage, injuries, or disputes between campsite owners and entrants.\n\n" +
                    "Eligibility: You must be 18 years or older to create an account, list a campsite, or make a booking.",
            fontSize = 14.sp,
            lineHeight = 21.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Displays the guidelines section heading.
        Text(
            text = "Guidelines",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Shows the rules that campsite users must follow.
        Text(
            text = "No Illegal Activity: Do not list land you don't own or have permission to use.\n\n" +
                    "Be Truthful: No fake reviews or misleading campsite photos.\n\n" +
                    "Respect Nature: Always follow \"Leave No Trace\" principles to protect the campsites and surrounding environment.",
            fontSize = 14.sp,
            lineHeight = 21.sp
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}
