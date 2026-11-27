/**
 * Defines the data used to display conversation summaries in the messages screen.
 *
 * Contains the MessageSummary model and a helper function that converts
 * a user's conversations into summaries for display.
 *
 * Outstanding issues: None known.
 */

package com.example.coders

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.coders.ui.screens.ChatData

/**
 * The information needed to display one conversation in the messages list.
 *
 * @property campsiteName Name of the campsite for this conversation.
 * @property otherPersonName Name of the conversation partner.
 * @property latestMessage Text of the latest message, or a placeholder when there are no messages.
 * @property latestDate Date of the latest message, or an empty string when there are no messages.
 * @property conversationId Unique identifier of the associated conversation.
 */
data class MessageSummary(
    val campsiteName: String,
    val otherPersonName: String,
    val latestMessage: String,
    val latestDate: String,
    val conversationId: String
)

/**
 * Creates message-list rows for the signed-in user.
 * Determines the conversation partner using participant IDs.
 * Profiles without conversations produce no rows.
 *
 * @return One summary for each conversation associated with this profile.
 */
fun Profile?.toMessageSummaries(): List<MessageSummary> {
    if (this == null) return emptyList()

    return conversations.mapNotNull { conversation ->

        val otherPersonName = when (id) {
            conversation.campOwnerId -> conversation.entrant.name
            conversation.entrantId -> conversation.campOwner.name
            else -> return@mapNotNull null
        }

        val latestMessage = conversation.messages.lastOrNull()

        MessageSummary(
            campsiteName = conversation.campsite.name,
            otherPersonName = otherPersonName,
            latestMessage = latestMessage?.content ?: "No messages yet",
            latestDate = latestMessage?.date ?: "",
            conversationId = conversation.id
        )
    }
}


/** Displays one campsite conversation preview and opens its chat when selected. */
@Composable
fun MessageBox(
    name: String,
    otherPerson: String,
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
                ChatData.otherPerson = otherPerson
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
                    Text(otherPerson)
                    Text(message)
                    Text(date)
                }
            }
        }

    }
}
