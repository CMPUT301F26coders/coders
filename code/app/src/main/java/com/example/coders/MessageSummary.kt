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
 * Creates message-list rows for the signed-in profile.
 * Entrants see the camp owner as the conversation partner; camp owners see the entrant.
 * Profiles without a conversation list, including admins, produce no rows.
 *
 * @return One summary for each conversation associated with this profile.
 */
fun Profile?.toMessageSummaries(): List<MessageSummary> {
    val conversationsAndPartner = when (this) {
        is Entrant -> conversations.map { it to it.campOwner.name }
        is CampOwner -> conversations.map { it to it.entrant.name }
        else -> emptyList()
    }

    return conversationsAndPartner.map { (conversation, otherPersonName) ->
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
