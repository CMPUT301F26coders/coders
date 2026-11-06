
package com.example.coders

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Contains unit tests for the message summary feature.
 *
 * These tests verify that:
 * - Entrants see the campsite owner and latest message.
 * - Campsite owners see the entrant as their conversation partner.
 * - Empty conversations display a placeholder message.
 * - Profiles without conversations return an empty summary list.
 */
class MessageSummaryTest {

    /**
     * Tests that an entrant's message summary displays the correct information.
     *
     * Creates a conversation containing two messages and verifies that:
     * - The campsite name is correct.
     * - The conversation partner is the campsite owner.
     * - The latest message and date are displayed correctly.
     * - The summary contains the correct conversation ID.
     */
    @Test
    fun entrantSummaryShowsCampOwnerAndLatestMessage() {
        val owner = CampOwner(name = "Casey Owner")
        val entrant = Entrant(name = "Alex Entrant")
        val conversation = conversation(
            entrant = entrant,
            owner = owner,
            messages = listOf(
                Message(entrant, "Earlier message", "Yesterday"),
                Message(owner, "Latest message", "Today")
            )
        )

        val summaries = entrant.copy(conversations = mutableListOf(conversation)).toMessageSummaries()

        assertEquals(
            listOf(
                MessageSummary(
                    campsiteName = "Pine Lake",
                    otherPersonName = "Casey Owner",
                    latestMessage = "Latest message",
                    latestDate = "Today",
                    conversationId = conversation.id
                )
            ),
            summaries
        )
    }

    /**
     * Tests that a campsite owner's message summary shows the correct entrant.
     *
     * Creates a conversation between a campsite owner and an entrant.
     * Verifies that the entrant's name appears as the conversation partner
     * in the owner's message summary.
     */
    @Test
    fun campOwnerSummaryShowsEntrantAsConversationPartner() {
        val owner = CampOwner(name = "Casey Owner")
        val entrant = Entrant(name = "Alex Entrant")
        val conversation = conversation(entrant = entrant, owner = owner)

        val summaries = owner.copy(conversations = mutableListOf(conversation)).toMessageSummaries()

        assertEquals("Alex Entrant", summaries.single().otherPersonName)
    }

    /**
     * Tests that an empty conversation displays a placeholder message.
     *
     * Creates a conversation without any messages and verifies that:
     * - The latest message displays "No messages yet".
     * - The latest message date is an empty string.
     *
     * This ensures empty conversations can be summarized without crashing.
     */
    @Test
    fun emptyConversationUsesPlaceholderInsteadOfCrashing() {
        val conversation = conversation(messages = emptyList())
        val entrant = conversation.entrant.copy(conversations = mutableListOf(conversation))

        val summary = entrant.toMessageSummaries().single()

        assertEquals("No messages yet", summary.latestMessage)
        assertEquals("", summary.latestDate)
    }

    /**
     * Tests that profiles without conversation lists return no message summaries.
     *
     * Verifies that:
     * - A null profile returns an empty list.
     * - An admin profile returns an empty list.
     *
     * This ensures unsupported or missing profiles do not produce summaries.
     */
    @Test
    fun profilesWithoutConversationListsReturnNoSummaries() {
        assertEquals(emptyList<MessageSummary>(), null.toMessageSummaries())
        assertEquals(emptyList<MessageSummary>(), Admin().toMessageSummaries())
    }

    /**
     * Creates a sample conversation for use in the unit tests.
     *
     * Uses default entrant and campsite owner information when
     * no specific values are provided. The conversation is linked
     * to Pine Lake campsite.
     *
     * @param entrant The entrant participating in the conversation.
     * @param owner The campsite owner participating in the conversation.
     * @param messages The messages included in the conversation.
     * @return A Conversation object containing the provided information.
     */
    private fun conversation(
        entrant: Entrant = Entrant(name = "Alex Entrant"),
        owner: CampOwner = CampOwner(name = "Casey Owner"),
        messages: List<Message> = emptyList()
    ) = Conversation(
        messages = messages,
        campOwner = owner,
        entrant = entrant,
        campsite = Campsite(name = "Pine Lake")
    )
}
