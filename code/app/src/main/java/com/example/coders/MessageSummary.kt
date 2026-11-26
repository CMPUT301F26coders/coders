/**
 * Defines the data used to display conversation summaries in the messages screen.
 *
 * Contains the MessageSummary model and a helper function that converts
 * a user's conversations into summaries for display.
 *
 * Outstanding issues: None known.
 */

package com.example.coders

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
