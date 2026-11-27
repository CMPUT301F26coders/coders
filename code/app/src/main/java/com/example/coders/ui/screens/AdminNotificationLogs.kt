package com.example.coders.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.coders.NotificationRepo
import com.example.coders.Notification
import com.example.coders.ProfileRepo
import com.example.coders.UserRole
import com.example.coders.SignedInProfile

/**
 * Displays a notification card containing its details.
 *
 * @param title The notification title.
 * @param content The notification content.
 * @param date The date the notification was sent.
 * @param recipient The recipient's name.
 * @param sender The sender's name.
 */
@Composable
fun NotificationCard(
    title: String,
    content: String,
    date: String,
    recipient: String,
    sender: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),

        shape = RoundedCornerShape(14.dp),

        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),

        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = content,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Date: $date",
                fontSize = 12.sp
            )

            Text(
                text = "Recipient: $recipient",
                fontSize = 12.sp
            )

            Text(
                text = "Sender: $sender",
                fontSize = 12.sp
            )
        }
    }
}

/**
 * Displays all notifications sent within the application
 * for administrative monitoring.
 *
 * @param navController The navigation controller.
 */
@Composable
fun AdminNotificationLogs(
    navController: NavController
) {
    val profile by SignedInProfile().rememberSignedInProfile()

    var notifications by remember {
        mutableStateOf<List<Notification>>(emptyList())
    }

    var profileNames by remember {
        mutableStateOf<Map<String, String>>(emptyMap())
    }

    var isLoadingNotifications by remember {
        mutableStateOf(true)
    }

    var notificationError by remember {
        mutableStateOf<Exception?>(null)
    }

    var backClicked by remember {
        mutableStateOf(false)
    }

    val isAdmin = profile?.role == UserRole.ADMIN

    DisposableEffect(isAdmin) {
        if (isAdmin) {
            val notificationRepo = NotificationRepo()
            val profileRepo = ProfileRepo()

            // Retrieve all profile names.
            val profileListener = profileRepo.listenForProfiles(
                onUpdate = { profiles ->
                    profileNames = profiles.associate { //means we can get names from the ids stored in a notification
                        it.id to it.name
                    }
                }
            )

            // Retrieve all notifications.
            val notificationListener =
                notificationRepo.listenForAllNotifications(
                    onUpdate = { result ->
                        notifications = result.sortedByDescending {
                            it.date
                        }

                        notificationError = null
                        isLoadingNotifications = false
                    },

                    onError = { error ->
                        notificationError = error
                        isLoadingNotifications = false
                    }
                )

            onDispose {
                notificationListener.remove()
                profileListener.remove()

                notificationRepo.close()
                profileRepo.close()
            }
        } else {
            onDispose { }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextButton(
            onClick = {
                if (!backClicked) {
                    backClicked = true

                    if (!navController.popBackStack()) {
                        backClicked = false
                    }
                }
            },
            enabled = !backClicked
        ) {
            Text("Back")
        }

        Text(
            text = "Notification Logs",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            profile == null -> {
                CircularProgressIndicator()
            }

            !isAdmin -> {
                Text("Access denied")
            }

            isLoadingNotifications -> {
                CircularProgressIndicator()
            }

            notificationError != null -> {
                Text("Failed to load notifications")
            }

            notifications.isEmpty() -> {
                Text("No notifications yet")
            }

            else -> {
                LazyColumn {
                    items(
                        items = notifications,
                        key = { it.id }
                    ) { notification ->

                        NotificationCard(
                            title = notification.title,
                            content = notification.content,
                            date = notification.date,

                            recipient = profileNames[
                                notification.recipientId
                            ] ?: "Unknown",

                            sender = profileNames[
                                notification.senderId
                            ] ?: "Unknown"
                        )
                    }
                }
            }
        }
    }
}