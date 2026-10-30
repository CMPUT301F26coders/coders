
package com.example.coders.ui.screens

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun SignUpScreen(navController: NavController) {

    // Remember what the user types
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    // Remember selected account type
    var accountType by remember { mutableStateOf("Entrant") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // Title
        Text(
            text = "New User:",
            fontSize = 35.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(50.dp))

        // First Name
        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            placeholder = { Text("First name:") },
            modifier = Modifier.testTag("firstNameInput"),
            shape = RoundedCornerShape(30.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Last Name
        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            placeholder = { Text("Last name:") },
            modifier = Modifier.testTag("lastNameInput"),
            shape = RoundedCornerShape(30.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("Email:") },
            modifier = Modifier.testTag("emailInput"),
            shape = RoundedCornerShape(30.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Phone Number
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            placeholder = { Text("Phone number:") },
            modifier = Modifier.testTag("phoneInput"),
            shape = RoundedCornerShape(30.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(35.dp))

        // Account Type
        Text(
            text = "Account Type:",
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("Admin", "Entrant", "Campsite owner").forEach { type ->

                FilterChip(
                    selected = accountType == type,
                    onClick = {
                        accountType = type
                    },
                    label = {
                        Text(type, fontSize = 11.sp)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(25.dp))

        // Sign Up Button
        Button(
            onClick = {
                if (accountType == "Admin") {
                    navController.navigate("admin")
                } else if (accountType == "Entrant") {
                    navController.navigate("entrant")
                } else {
                    navController.navigate("owner")
                }
            },
            shape = RoundedCornerShape(30.dp)
        ) {
            Text(
                text = "Sign Up",
                fontSize = 18.sp
            )
        }
    }
}
