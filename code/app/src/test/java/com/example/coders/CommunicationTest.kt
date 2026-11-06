/**
 * Contains unit tests for the application's communication models.
 *
 * Tests that notifications correctly store their title, content, and date,
 * messages store their sender and content, and conversations store their
 * participants and messages. Also verifies that notifications and
 * conversations receive unique and non-empty IDs.
 *
 * Outstanding issues: None known.
 */

package com.example.coders

import com.example.coders.Notification
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Contains unit tests for the communication features of the app.
 *
 * These tests verify that:
 * - Notifications store their title, content, and date correctly.
 * - Messages store the correct sender and content.
 * - Conversations store messages, campsite owners, and entrants.
 * - Notifications receive unique and non-empty IDs.
 * - Conversations receive unique and non-empty IDs.
 */
class CommunicationTest {

    /**
     * Tests that a notification stores its information correctly.
     *
     * Creates a notification and verifies that its title,
     * content, and date match the values provided.
     */
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

    /**
     * Tests that a message stores its sender and content correctly.
     *
     * Creates an entrant and a message, then verifies that:
     * - The sender's name is correct.
     * - The message content matches the provided text.
     */
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

    /**
     * Tests that a conversation stores its participants and messages correctly.
     *
     * Creates a campsite owner, entrant, and message.
     * Then creates a conversation and verifies that:
     * - The campsite owner's name is correct.
     * - The entrant's name is correct.
     * - The conversation contains one message.
     * - The stored message contains the expected text.
     */
    @Test
    fun conversation_storesMessagesAndCampOwnerAndEntrant() {
        val owner = CampOwner("Bob", "bob@test.com", "123", emptyList(), emptyList(), emptyList(), emptyList())
        val entrant = Entrant("Alice", "alice@test.com", "456", emptyList(), emptyList())
        val message = Message(entrant, "Hello!", "2026-10-26")

        val chat = Conversation(
            messages = listOf(message),
            campOwner = owner,
            entrant = entrant,
            campsite = Campsite(name = "Pine Lake")
        )

        assertEquals("Bob", chat.campOwner.name)
        assertEquals("Alice", chat.entrant.name)
        assertEquals(1, chat.messages.size)
        assertEquals("Hello!", chat.messages[0].content)
    }

    /**
     * Tests that different notifications receive unique and non-empty IDs.
     *
     * Creates two notifications and verifies that:
     * - Their generated IDs are different.
     * - Neither notification has a blank ID.
     *
     * This helps ensure each notification can be identified separately.
     */
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
        assertTrue(notification1.id.isNotBlank())
        assertTrue(notification2.id.isNotBlank())
    }

    /**
     * Tests that different conversations receive unique and non-empty IDs.
     *
     * Creates two separate conversations and verifies that:
     * - Their generated IDs are different.
     * - Neither conversation has a blank ID.
     *
     * This helps ensure each conversation can be identified individually.
     */
    @Test
    fun conversation_ids_autoIncrementOnCreation() {
        val owner = CampOwner("Bob", "bob@test.com", "123", emptyList(), emptyList(), emptyList(), emptyList())
        val entrant = Entrant("Alice", "alice@test.com", "456", emptyList(), emptyList())
        val message = Message(entrant, "Hello!", "2026-10-26")

        val chat = Conversation(
            messages = listOf(message),
            campOwner = owner,
            entrant = entrant,
            campsite = Campsite(name = "Pine Lake")
        )

        val owner2 = CampOwner("Bob", "bob@test.com", "123", emptyList(), emptyList(), emptyList(), emptyList())
        val entrant2 = Entrant("Alice", "alice@test.com", "456", emptyList(), emptyList())
        val message2 = Message(entrant, "Hello!", "2026-10-26")

        val chat2 = Conversation(
            messages = listOf(message2),
            campOwner = owner2,
            entrant = entrant2,
            campsite = Campsite(name = "Pine Lake")
        )

        assertNotEquals(chat.id, chat2.id)
        assertTrue(chat.id.isNotBlank())
        assertTrue(chat2.id.isNotBlank())
    }


}
