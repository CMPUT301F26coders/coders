
package com.example.coders.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun PoliciesScreen(navController: NavController) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Button(onClick = {
            navController.popBackStack()
        }) {
            Text("Back")
        }

        Text(
            text = "Policies & Guidelines",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Policies\n" +
                    "What Data We Collect: We collect your name, email address, phone number, and location (if you allow it) to set up your account. For campsite owners, we also collect campsite details and photos.\n" +
                    "How We Use Your Data: We use your information to manage bookings, connect entrants with campsite owners, and send you booking confirmations.\n" +
                    "Data Deletion: You can delete your account at any time through the app settings, which will permanently remove your personal data.\n" +
                    "Liability: Our app is a marketplace that connects campers with landowners. We do not own, inspect, or manage the campsites. We are not responsible for property damage, injuries, or disputes between campsite owners and entrants\n" +
                    "Eligibility: You must be 18 years or older to create an account, list a campsite, or make a booking\n" +
                    "\n" +
                    "Guidelines\n" +
                    "No Illegal Activity: Do not list land you don't own or have permission to use.\n" +
                    "Be Truthful: No fake reviews or misleading campsite photos.\n" +
                    "Respect Nature: Always follow \"Leave No Trace\" principles to protect the campsites and surrounding environment."
        )
    }
}
