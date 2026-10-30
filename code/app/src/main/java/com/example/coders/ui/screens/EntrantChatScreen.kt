
package com.example.coders.ui.screens

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

object ChatData {
    var owner = ""
}

@Composable
fun EntrantChatScreen(navController: NavController) {

    var message by remember { mutableStateOf("") }

    val messages = remember(ChatData.owner) {
        mutableStateListOf<String>().apply {

            if (ChatData.owner == "John Smith") {
                add("Owner: Hi! Thanks for applying to Pine Lake Campsite.")
                add("You: Hi! I am excited. Can you tell me more about the campsite?")
                add("Owner: Of course! It has a lake view, hiking trails, and a fire pit!")
                add("You: Great! Is parking available?")
                add("Owner: Yes, there are parking spots available!")
                add("You: Amazing! Can't wait!")
            }

            if (ChatData.owner == "Sarah Lee") {
                add("Owner: Thanks for your interest in Mountain View Camp!")
                add("You: Is the campsite available next weekend?")
                add("Owner: Yes, it is!")
            }

            if (ChatData.owner == "Mike Brown") {
                add("Owner: Welcome to Forest Hill Camp!")
                add("You: Are there washrooms?")
                add("Owner: Yes, washrooms are available!")
            }

            if (ChatData.owner == "Emma Wilson") {
                add("Owner: Your reservation details are ready!")
                add("You: What time is check-in?")
                add("Owner: Check-in starts at 2 PM.")
            }
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
                navController.popBackStack()
            }
        ) {
            Text("Back", fontSize = 20.sp)
        }

        // Campsite and owner name
        Text(
            text = if (ChatData.owner == "John Smith") {
                "Pine Lake Campsite"
            } else if (ChatData.owner == "Sarah Lee") {
                "Mountain View Camp"
            } else if (ChatData.owner == "Mike Brown") {
                "Forest Hill Camp"
            } else {
                "Riverside Campsite"
            },
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = ChatData.owner + " (Campsite Owner)",
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

                if (chatMessage.startsWith("You:")) {

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
                                text = chatMessage.removePrefix("You: "),
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
                                text = chatMessage.removePrefix("Owner: "),
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
                onClick = {
                    if (message.isNotBlank()) {
                        messages.add("You: " + message)
                        message = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF625B71)
                )
            ) {
                Text("➜")
            }
        }
    }
}
