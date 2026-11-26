package com.example.coders.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.coders.CampOwner
import com.example.coders.Notification
import com.example.coders.NotificationRepo
import com.example.coders.SignedInProfile

private data class SentNotificationGroup(
    val id: String,
    val notification: Notification,
    val recipientCount: Int
)

private fun groupSentNotifications(notifications: List<Notification>): List<SentNotificationGroup> =
    notifications
        .groupBy { notification ->
            notification.sendGroupId.takeIf(String::isNotBlank)
                ?.let { "send:$it" }
                ?: "legacy:${notification.senderId}:${notification.date}:${notification.title}:${notification.content}"
        }
        .map { (id, group) ->
            SentNotificationGroup(
                id = id,
                notification = group.first(),
                recipientCount = group.map(Notification::recipientId)
                    .filter(String::isNotBlank)
                    .distinct()
                    .size
            )
        }
        .sortedByDescending { it.notification.date }

/** Shows notifications authored by the signed-in campsite owner. */
@Composable
fun OwnerSentNotificationsScreen(navController: NavController) {
    val profile by remember { SignedInProfile() }.rememberSignedInProfile()
    val notificationRepo = remember { NotificationRepo() }
    val ownerId = (profile as? CampOwner)?.id

    var notifications by remember(ownerId) { mutableStateOf<List<Notification>?>(null) }
    var errorMessage by remember(ownerId) { mutableStateOf<String?>(null) }

    DisposableEffect(ownerId, notificationRepo) {
        val registration = ownerId?.takeIf(String::isNotBlank)?.let { id ->
            notificationRepo.listenForSentNotifications(
                senderId = id,
                onUpdate = {
                    notifications = it
                    errorMessage = null
                },
                onError = { error ->
                    errorMessage = error.message ?: "Unable to load sent notifications."
                }
            )
        }
        onDispose {
            registration?.remove()
            notificationRepo.close()
        }
    }

    Scaffold(bottomBar = { AppBottomBar(navController = navController) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Sent Notifications",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Notifications sent by you to lottery entrants.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            when {
                profile == null -> LoadingSentNotifications()
                profile !is CampOwner -> Text(
                    text = "This page is available to campsite owners.",
                    modifier = Modifier.padding(20.dp)
                )
                errorMessage != null -> Text(
                    text = errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(20.dp)
                )
                notifications == null -> LoadingSentNotifications()
                notifications.isNullOrEmpty() -> Text(
                    text = "You haven't sent any notifications yet.",
                    modifier = Modifier.padding(20.dp)
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        groupSentNotifications(notifications.orEmpty()),
                        key = SentNotificationGroup::id
                    ) { group ->
                        SentNotificationCard(group)
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingSentNotifications() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun SentNotificationCard(group: SentNotificationGroup) {
    val notification = group.notification
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(notification.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(notification.content, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sent to ${group.recipientCount} ${if (group.recipientCount == 1) "person" else "people"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = notification.date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
