
package com.example.coders.ui.screens

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.coders.Admin
import com.example.coders.AuthManager
import com.example.coders.CampOwner
import com.example.coders.Entrant
import com.example.coders.Profile
import com.example.coders.ProfileRepo
import com.example.coders.UserSession
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/** Collects profile details, saves the profile, then remembers its ID on this device. */
@Composable
fun SignUpScreen(navController: NavController) {

    // Remember what the user types
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    val profileRepo = remember { ProfileRepo() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

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
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            enabled = !isSaving,
            onClick = {
                if (firstName.isBlank() || lastName.isBlank() || email.isBlank() || phone.isBlank()) {
                    errorMessage = buildList {
                        if (firstName.isBlank()) add("First name is empty")
                        if (lastName.isBlank()) add("Last name is empty")
                        if (email.isBlank()) add("Email is empty")
                        if (phone.isBlank()) add("Phone is empty")
                    }.joinToString("\n")
                } else {
                    val fullName = "${firstName.trim()} ${lastName.trim()}"
                    val profile: Profile = when (accountType) {
                        "Admin" -> Admin(
                            name = fullName,
                            email = email.trim(),
                            phone = phone.trim()
                        )
                        "Entrant" -> Entrant(
                            name = fullName,
                            email = email.trim(),
                            phone = phone.trim()
                        )
                        else -> CampOwner(
                            name = fullName,
                            email = email.trim(),
                            phone = phone.trim()
                        )
                    }

                    errorMessage = ""
                    isSaving = true
                    profileRepo.addProfile(profile) { error ->
                        coroutineScope.launch(Dispatchers.Main.immediate) {
                            isSaving = false
                            if (error != null) {
                                errorMessage = "Couldn't save your profile. Please try again."
                            } else {
                                try {
                                    AuthManager(context.applicationContext).saveUserId(profile.id)
                                    when (profile) {
                                        is Admin -> {
                                            UserSession.isOwner = false
                                            navController.navigate("admin")
                                        }
                                        is CampOwner -> {
                                            UserSession.isOwner = true
                                            navController.navigate("home")
                                        }
                                        is Entrant -> {
                                            UserSession.isOwner = false
                                            navController.navigate("home")
                                        }
                                    }
                                } catch (error: Exception) {
                                    errorMessage = "Profile saved, but this device couldn't remember the sign-in."
                                }
                            }
                        }
                    }
                }
            },
            shape = RoundedCornerShape(30.dp)
        ) {
            Text(text = if (isSaving) "Saving..." else "Sign Up", fontSize = 18.sp)
        }
    }
}
