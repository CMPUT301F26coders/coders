package com.example.coders

import org.junit.Assert.assertEquals
import org.junit.Test

class CommunicationTest {

    @Test
    fun notification_storesTitleAndContent() {
        val notification = Notification(
            title = "Lottery Won!",
            content = "You won the campsite lottery.",
            date = "2026-10-26"
        )

        assertEquals("Lottery Won!", notification.title)
        assertEquals("You won the campsite lottery.", notification.content)
        assertEquals("2026-10-26", notification.date)
    }

    @Test
    fun message_storesSenderAndContent() {
        val entrant = Entrant("Alice", "alice@test.com", "123", emptyList(), emptyList())
        val message = Message(
            sender = entrant,
            content = "What time is check-in?",
            date = "2026-10-26 10:00 AM"
        )

        assertEquals("Alice", message.sender.name)
        assertEquals("What time is check-in?", message.content)
    }

    @Test
    fun messageChat_storesMessagesAndCampOwnerAndEntrant() {
        val owner = CampOwner("Bob", "bob@test.com", "123", emptyList(), emptyList(), emptyList(), emptyList())
        val entrant = Entrant("Alice", "alice@test.com", "456", emptyList(), emptyList())
        val message = Message(entrant, "Hello!", "2026-10-26")

        val chat = MessageChat(
            messages = listOf(message),
            campOwner = owner,
            entrant = entrant
        )

        assertEquals("Bob", chat.campOwner.name)
        assertEquals("Alice", chat.entrant.name)
        assertEquals(1, chat.messages.size)
        assertEquals("Hello!", chat.messages[0].content)
    }
}