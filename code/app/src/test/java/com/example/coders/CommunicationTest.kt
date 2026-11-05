package com.example.coders

import com.example.coders.Notification
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
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
    fun conversation_storesMessagesAndCampOwnerAndEntrant() {
        val owner = CampOwner("Bob", "bob@test.com", "123", emptyList(), emptyList(), emptyList(), emptyList())
        val entrant = Entrant("Alice", "alice@test.com", "456", emptyList(), emptyList())
        val message = Message(entrant, "Hello!", "2026-10-26")

        val chat = Conversation(
            messages = listOf(message),
            campOwner = owner,
            entrant = entrant
        )

        assertEquals("Bob", chat.campOwner.name)
        assertEquals("Alice", chat.entrant.name)
        assertEquals(1, chat.messages.size)
        assertEquals("Hello!", chat.messages[0].content)
    }

    @Test
    fun notification_ids_autoIncrementOnCreation() {
        val notification1 = Notification(
            title = "Lottery Won!",
            content = "You won the campsite lottery.",
            date = "2026-10-26"
        )
        val notification2 = Notification(
            title = "Lottery Lost!",
            content = "You lost the campsite lottery.",
            date = "1993-11-03"
        )
        assertNotEquals(notification1.id, notification2.id)
        assertEquals(notification1.id + 1, notification2.id)
    }

    @Test
    fun conversation_ids_autoIncrementOnCreation() {
        val owner = CampOwner("Bob", "bob@test.com", "123", emptyList(), emptyList(), emptyList(), emptyList())
        val entrant = Entrant("Alice", "alice@test.com", "456", emptyList(), emptyList())
        val message = Message(entrant, "Hello!", "2026-10-26")

        val chat = Conversation(
            messages = listOf(message),
            campOwner = owner,
            entrant = entrant
        )

        val owner2 = CampOwner("Bob", "bob@test.com", "123", emptyList(), emptyList(), emptyList(), emptyList())
        val entrant2 = Entrant("Alice", "alice@test.com", "456", emptyList(), emptyList())
        val message2 = Message(entrant, "Hello!", "2026-10-26")

        val chat2 = Conversation(
            messages = listOf(message2),
            campOwner = owner2,
            entrant = entrant2
        )

        assertNotEquals(chat.id, chat2.id)
        assertEquals(chat.id + 1, chat2.id)
    }


}