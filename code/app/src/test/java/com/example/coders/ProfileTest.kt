package com.example.coders

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileTest {

    @Test
    fun entrant_creation_storesPropertiesCorrectly() {
        val entrant = Entrant(
            name = "Alice",
            email = "alice@example.com",
            phone = "780-111-2222",
            bookings = emptyList(),
            waitlists = emptyList()
        )

        assertEquals("Alice", entrant.name)
        assertEquals("alice@example.com", entrant.email)
        assertEquals("780-111-2222", entrant.phone)
        assertTrue(entrant.bookings.isEmpty())
        assertTrue(entrant.waitlists.isEmpty())
    }

    @Test
    fun campOwner_creation_storesPropertiesAndListsCorrectly() {
        val owner = CampOwner(
            name = "Bob Owner",
            email = "bob@campsite.ca",
            phone = "780-333-4444",
            campsites = emptyList(),
            lotteries = emptyList(),
            reservations = emptyList(),
            notifications = emptyList()
        )

        assertEquals("Bob Owner", owner.name)
        assertEquals("bob@campsite.ca", owner.email)
        assertTrue(owner.campsites.isEmpty())
        assertTrue(owner.lotteries.isEmpty())
    }

    @Test
    fun admin_creation_storesAuditLogsCorrectly() {
        val notification = Notification("Title", "Content", "2026-10-26")
        val admin = Admin(
            name = "Admin Sarah",
            email = "admin@campsite.ca",
            phone = "780-999-0000",
            waitlists = emptyList(),
            campsites = emptyList(),
            lotteries = emptyList(),
            reservations = emptyList(),
            notificationLogs = listOf(notification),
            messageLogs = emptyList()
        )

        assertEquals("Admin Sarah", admin.name)
        assertEquals(1, admin.notificationLogs.size)
        assertEquals("Title", admin.notificationLogs[0].title)
    }

    @Test
    fun all_profile_types_creation_storesPropertiesCorrectly() {
        val entrant: Profile = Entrant("User1", "u1@test.com", "111", emptyList(), emptyList())
        val owner: Profile = CampOwner("User2", "u2@test.com", "222", emptyList(), emptyList(), emptyList(), emptyList())
        val admin: Profile = Admin("User3", "u3@test.com", "333", emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList())

        val userList: List<Profile> = listOf(entrant, owner, admin)

        assertEquals(3, userList.size)
        assertEquals("User1", userList[0].name)
        assertEquals("User2", userList[1].name)
        assertEquals("User3", userList[2].name)
    }
}