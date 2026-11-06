
package com.example.coders.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.coders.ConversationRepo
import com.example.coders.Message
import com.example.coders.SignedInProfile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ChatData {
    var owner = ""
    var conversationId = ""
}

@Composable
fun ChatScreen(navController: NavController) {


    var conversationExists by remember(ChatData.conversationId) {
        mutableStateOf(false)
    }

    val conversationRepo = remember { ConversationRepo() }
    val profile by SignedInProfile().rememberSignedInProfile()

    var isSending by remember { mutableStateOf(false) }
    var sendError by remember { mutableStateOf<String?>(null) }

    var isNavigating by remember { mutableStateOf(false) }

    var message by remember { mutableStateOf("") }

    val messages = remember(ChatData.conversationId) {
        mutableStateListOf<Message>()
    }

    var campsiteName by remember(ChatData.conversationId) {
        mutableStateOf("")
    }

    var isLoading by remember(ChatData.conversationId) {
        mutableStateOf(true)
    }

    DisposableEffect(ChatData.conversationId, conversationRepo) {
        val conversationId = ChatData.conversationId


        val listener = if (conversationId.isNotBlank()) {
            conversationRepo.listenForConversation(
                conversationId = conversationId,
                onUpdate = { conversation ->
                    conversationExists = conversation != null

                    messages.clear()
                    messages.addAll(conversation?.messages.orEmpty())
                    campsiteName = conversation?.campsite?.name.orEmpty()
                    isLoading = false
                },
                onError = { error ->
                    Log.e("ChatScreen", "Failed to load messages", error)
                    isLoading = false
                }
            )
        } else {
            isLoading = false
            null
        }

        onDispose {
            listener?.remove()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // Back button
        TextButton(
            onClick = {
                if (!isNavigating) {
                    isNavigating = true
                    navController.popBackStack()
                }
            }
        ) {
            Text("Back", fontSize = 20.sp)
        }

        // Campsite and owner name
        Text(
            text = campsiteName,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = ChatData.owner,
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        HorizontalDivider()

        Spacer(modifier = Modifier.height(16.dp))

        // All chat messages
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {

            for (chatMessage in messages) {
                if (chatMessage.senderId == profile?.id) {

                    // Your message on the right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Card(
                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                .padding(bottom = 12.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF625B71)
                            )
                        ) {
                            Text(
                                text = chatMessage.content,
                                color = Color.White,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }

                } else {

                    // Owner message on the left
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Card(
                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                .padding(bottom = 12.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFECECEC)
                            )
                        ) {
                            Text(
                                text = chatMessage.content,
                                color = Color.Black,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider()

        Spacer(modifier = Modifier.height(12.dp))

        // Type and send a message
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            OutlinedTextField(
                value = message,
                onValueChange = {
                    message = it
                },
                placeholder = {
                    Text("Type a message...")
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))


            Button(
                enabled = !isSending &&
                        profile != null &&
                        conversationExists &&
                        ChatData.conversationId.isNotBlank(),
                onClick = {
                    val sender = profile
                    val messageText = message.trim()

                    if (messageText.isNotEmpty() &&
                        sender != null &&
                        ChatData.conversationId.isNotBlank()
                    ) {
                        val newMessage = Message(
                            sender = sender,
                            content = messageText,
                            date = SimpleDateFormat(
                                "yyyy-MM-dd HH:mm:ss",
                                Locale.getDefault()
                            ).format(Date())
                        )

                        isSending = true
                        sendError = null

                        conversationRepo.addMessage(
                            conversationId = ChatData.conversationId,
                            message = newMessage,
                            onComplete = { error ->
                                isSending = false

                                if (error == null) {
                                    message = ""
                                } else {
                                    Log.e(
                                        "ChatScreen",
                                        "Failed to send message",
                                        error
                                    )
                                    sendError = "Failed to send message."
                                }
                            }
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF625B71)
                )
            ) {
                Text(if (isSending) "Sending..." else "➜")
            }

        }
        sendError?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
