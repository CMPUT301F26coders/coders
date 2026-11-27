/**
 * Defines the home screen for entrants and campsite owners.
 *
 *
 * Outstanding issue: The campsite owner's home screen is a placeholder.
 * Some tests regarding this page may be outdated, OwnerHomePageTest & EntrantHomePageTest
 */

package com.example.coders.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.example.coders.Notification
import com.example.coders.NotificationRepo
import com.example.coders.SignedInProfile

/**
 * Displays the appropriate home screen based on the current user's role.
 *
 * @param navController Used to navigate between screens.
 */
@Composable
fun HomeScreen(navController: NavController) {

    Scaffold(
        bottomBar = {
            AppBottomBar(navController = navController)
        }
    ) { innerPadding ->
        HomeContent(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}



/**
 * Displays the signed-in user's home screen with notifications and
 * a navigation option for policies and guidelines.
 *
 * @param navController Used to navigate to other screens.
 * @param modifier Modifier used to customize the layout.
 */
@Composable
fun HomeContent(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val profile by remember { SignedInProfile() }.rememberSignedInProfile()
    val notificationRepo = remember { NotificationRepo() }
    var notifications by remember(profile?.id) { mutableStateOf<List<Notification>>(emptyList()) }
    var notificationError by remember { mutableStateOf<String?>(null) }
    DisposableEffect(profile?.id, notificationRepo) {
        val userId = profile?.id
        val registration = if (!userId.isNullOrBlank()) {
            notificationRepo.listenForUserNotifications(
                userId = userId,
                onUpdate = { notifications = it },
                onError = { error -> notificationError = error.message ?: "Unable to load notifications." }
            )
        } else {
            notifications = emptyList()
            null
        }
        onDispose {
            registration?.remove()
            notificationRepo.close()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Home",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = { navController.navigate("policies") },
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Policies & Guidelines")
        }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = "Notifications",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        notificationError?.let { error ->
            Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (notifications.isEmpty() && notificationError == null) {
            Text("You don't have any notifications yet.", modifier = Modifier.fillMaxWidth())
        } else {
            notifications.forEach { notification ->
                EntrantNotification(
                    title = notification.title,
                    message = notification.content,
                    onDismiss = {
                        notificationRepo.deleteNotification(notification.id) { error ->
                            if (error != null) {
                                notificationError = error.message ?: "Unable to clear notification."
                            } else {
                                notificationError = null
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

/**
 * Displays a notification card containing a title and message.
 *
 * @param title The notification title.
 * @param message The notification content.
 * @param onDismiss Called when the user clears this notification.
 */
@Composable
fun EntrantNotification(
    title: String,
    message: String,
    onDismiss: () -> Unit = {}
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, top = 10.dp, end = 6.dp, bottom = 10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f).padding(top = 4.dp, bottom = 4.dp)) {
                Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Clear notification")
            }
        }
    }
}
