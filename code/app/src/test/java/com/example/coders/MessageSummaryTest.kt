package com.example.coders

import org.junit.Assert.assertEquals
import org.junit.Test

class MessageSummaryTest {

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
            listOf(MessageSummary("Pine Lake", "Casey Owner", "Latest message", "Today")),
            summaries
        )
    }

    @Test
    fun campOwnerSummaryShowsEntrantAsConversationPartner() {
        val owner = CampOwner(name = "Casey Owner")
        val entrant = Entrant(name = "Alex Entrant")
        val conversation = conversation(entrant = entrant, owner = owner)

        val summaries = owner.copy(conversations = mutableListOf(conversation)).toMessageSummaries()

        assertEquals("Alex Entrant", summaries.single().otherPersonName)
    }

    @Test
    fun emptyConversationUsesPlaceholderInsteadOfCrashing() {
        val conversation = conversation(messages = emptyList())
        val entrant = conversation.entrant.copy(conversations = mutableListOf(conversation))

        val summary = entrant.toMessageSummaries().single()

        assertEquals("No messages yet", summary.latestMessage)
        assertEquals("", summary.latestDate)
    }

    @Test
    fun profilesWithoutConversationListsReturnNoSummaries() {
        assertEquals(emptyList<MessageSummary>(), null.toMessageSummaries())
        assertEquals(emptyList<MessageSummary>(), Admin().toMessageSummaries())
    }

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
