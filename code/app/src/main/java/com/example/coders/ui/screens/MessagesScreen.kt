
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
import com.example.coders.Conversation
import com.google.firebase.firestore.ListenerRegistration
import android.util.Log
import androidx.compose.runtime.*
import com.example.coders.ConversationRepo
import com.example.coders.CampsiteRepo
import com.example.coders.ProfileRepo
import com.example.coders.CampOwner
import com.example.coders.Entrant
import com.example.coders.MessageSummary





/**
 * Displays the messages screen. Allows users to navigate to the chat screen.
 *
 * @param navController The navigation controller.
 * @return The messages screen.
 */

@Composable
fun MessagesScreen(navController: NavController) {

    val profile by SignedInProfile().rememberSignedInProfile()
    val conversationRepo = remember { ConversationRepo() }
    val profileRepo = remember { ProfileRepo() }
    val campsiteRepo = remember { CampsiteRepo() }

    var conversations by remember(profile?.id) {
        mutableStateOf<List<Conversation>>(emptyList())
    }

    var isLoading by remember(profile?.id) {
        mutableStateOf(true)
    }

    var loadError by remember(profile?.id) {
        mutableStateOf<String?>(null)
    }

    // Listen for conversations belonging to the signed-in user
    DisposableEffect(profile?.id, conversationRepo) {

        val userId = profile?.id

        val listener = if (!userId.isNullOrBlank()) {

            conversationRepo.listenForUserConversations(
                userId = userId,

                onUpdate = { updatedConversations ->
                    conversations = updatedConversations
                    isLoading = false
                    loadError = null
                },

                onError = { error ->
                    Log.e(
                        "MessagesScreen",
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

    DisposableEffect(conversations, profile, profileRepo, campsiteRepo) {

        var disposed = false
        val summaries = MutableList<MessageSummary?>(conversations.size) { null }

        conversations.forEachIndexed { index, conversation ->

            val otherPersonId = when (profile) {
                is CampOwner -> conversation.entrantId
                is Entrant -> conversation.campOwnerId
                else -> ""
            }

            //if (otherPersonId.isBlank() || conversation.campsiteId.isBlank()) {
            //    return@forEachIndexed
            //}

            profileRepo.getProfileById(otherPersonId) { otherPerson, _ ->

                if (!disposed) {
                    campsiteRepo.getCampsiteById(conversation.campsiteId) { campsite, _ ->

                        if (!disposed) {
                            val latestMessage = conversation.messages.lastOrNull()

                            summaries[index] = MessageSummary(
                                campsiteName = campsite?.name ?: "Unknown campsite",
                                otherPersonName = otherPerson?.name ?: "Unknown user",
                                latestMessage = latestMessage?.content ?: "No messages yet",
                                latestDate = latestMessage?.date ?: "",
                                conversationId = conversation.id
                            )

                            messageSummaries = summaries.filterNotNull()
                        }
                    }
                }
            }
        }

        onDispose {
            disposed = true
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
                text = "Messages",
                fontSize = 35.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(25.dp))

            when {
                profile == null || isLoading -> {
                    CircularProgressIndicator()
                }

                loadError != null -> {
                    Text(
                        text = loadError.orEmpty(),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                messageSummaries.isEmpty() -> {
                    Text("No conversations yet.")
                }

                else -> {
                    messageSummaries.forEach { summary ->

                        MessageBox(
                            name = summary.campsiteName,
                            owner = summary.otherPersonName,
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


/** Displays one campsite conversation preview and opens its chat when selected. */
@Composable
fun MessageBox(
    name: String,
    owner: String,
    message: String,
    date: String,
    picture: Int,
    conversationId: String,
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
                ChatData.conversationId = conversationId
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
