
package com.example.coders.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.coders.R
import com.example.coders.SignedInProfile
import com.example.coders.Conversation
import android.util.Log
import androidx.compose.runtime.*
import com.example.coders.ConversationRepo
import com.example.coders.CampsiteRepo
import com.example.coders.ProfileRepo
import com.example.coders.MessageSummary
import com.example.coders.MessageBox
import com.example.coders.UserRole



/**
 * Displays the messages screen. Allows users to navigate to the chat screen.
 *
 * @param navController The navigation controller.
 * @return The messages screen.
 */

@Composable
fun AdminMessageLogs(
    navController: NavController
) {
    val profile by SignedInProfile().rememberSignedInProfile()
    val conversationRepo = remember { ConversationRepo() }
    val profileRepo = remember { ProfileRepo() }
    val campsiteRepo = remember { CampsiteRepo() }

    var conversations by remember {
        mutableStateOf<List<Conversation>>(emptyList())
    }

    var isLoading by remember(profile?.id) {
        mutableStateOf(true)
    }

    var loadError by remember(profile?.id) {
        mutableStateOf<String?>(null)
    }

    val isAdmin = profile?.role == UserRole.ADMIN

    // Listen for conversations belonging to the signed-in user
    DisposableEffect(profile?.id, isAdmin, conversationRepo) {

        conversations = emptyList()
        isLoading = true
        loadError = null

        val listener = if (isAdmin) {

            conversationRepo.listenForAllConversations(
                onUpdate = { updatedConversations ->
                    conversations = updatedConversations
                    isLoading = false
                    loadError = null
                },
                onError = { error ->
                    Log.e(
                        "MessageLogs",
                        "Failed to load conversations",
                        error
                    )
                    loadError = "Failed to load conversations."
                    isLoading = false
                }
            )

        } else {
            null
        }

        onDispose {
            listener?.remove()
        }
    }

    // Convert Firestore conversations into display summaries
    var messageSummaries by remember(profile?.id) {
        mutableStateOf<List<MessageSummary>>(emptyList())
    }

    DisposableEffect(conversations, profileRepo, campsiteRepo) {

        var disposed = false

        val summaries = MutableList<MessageSummary?>(
            conversations.size
        ) { null }

        messageSummaries = emptyList()

        conversations.forEachIndexed { index, conversation ->

            if (
                conversation.campOwnerId.isBlank() ||
                conversation.entrantId.isBlank() ||
                conversation.campsiteId.isBlank()
            ) {
                val latestMessage = conversation.messages.lastOrNull()

                summaries[index] = MessageSummary(
                    campsiteName = "Unknown campsite",
                    otherPersonName = "Unknown participants",
                    latestMessage = latestMessage?.content
                        ?: "No messages yet",
                    latestDate = latestMessage?.date ?: "",
                    conversationId = conversation.id
                )

                messageSummaries = summaries.filterNotNull()

                return@forEachIndexed
            }

            profileRepo.getProfileById(conversation.campOwnerId) { owner, _ ->

                if (!disposed) {
                    profileRepo.getProfileById(conversation.entrantId) { entrant, _ ->

                        if (!disposed) {
                            campsiteRepo.getCampsiteById(conversation.campsiteId) { campsite, _ ->

                                if (!disposed) {
                                    val latestMessage =
                                        conversation.messages.lastOrNull()

                                    val ownerName =
                                        owner?.name ?: "Unknown owner"

                                    val entrantName =
                                        entrant?.name ?: "Unknown entrant"

                                    summaries[index] = MessageSummary(
                                        campsiteName = campsite?.name
                                            ?: "Unknown campsite",

                                        otherPersonName = "$ownerName and $entrantName",

                                        latestMessage = latestMessage?.content
                                            ?: "No messages yet",

                                        latestDate = latestMessage?.date ?: "",

                                        conversationId = conversation.id
                                    )

                                    messageSummaries = summaries.filterNotNull()
                                }
                            }
                        }
                    }
                }
            }
        }

        onDispose {
            disposed = true
        }
    }

    DisposableEffect(profileRepo, campsiteRepo) {
        onDispose {
            profileRepo.close()
            campsiteRepo.close()
        }
    }

    Scaffold(
        bottomBar = {
            AppBottomBar(navController = navController)
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
                text = "Message Logs",
                fontSize = 35.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(25.dp))

            when {
                profile == null -> {
                    CircularProgressIndicator()
                }

                !isAdmin -> {
                    Text("Access denied")
                }

                isLoading -> {
                    CircularProgressIndicator()
                }

                loadError != null -> {
                    Text(
                        text = loadError.orEmpty(),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                conversations.isEmpty() -> {
                    Text("No conversations yet.")
                }

                messageSummaries.isEmpty() -> {
                    CircularProgressIndicator()
                }

                else -> {
                    messageSummaries.forEach { summary ->
                        MessageBox(
                            name = summary.campsiteName,
                            otherPerson = summary.otherPersonName,
                            message = summary.latestMessage,
                            date = summary.latestDate,
                            picture = R.drawable.ic_launcher_foreground,
                            conversationId = summary.conversationId,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}
