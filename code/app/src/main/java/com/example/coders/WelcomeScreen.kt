
package com.example.coders

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Welcome screen for new users, existing users, and admins.
 *
 * @param onNewUser Opens the registration page.
 * @param onExistingUser Opens the user login page.
 * @param onAdminLogin Opens the admin login page.
 */
@Composable
fun WelcomeScreen(
    onNewUser: () -> Unit,
    onExistingUser: () -> Unit,
    onAdminLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to Coders Campsites",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNewUser,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("New User - Sign Up")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onExistingUser,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Existing User - Log In")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onAdminLogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Admin - Log In")
        }
    }
}
