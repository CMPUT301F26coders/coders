/**
 * Contains unit tests for the application's user profile models.
 *
 * Tests the creation and properties of Entrant, CampOwner, and Admin profiles.
 * Verifies that profile information is stored correctly, different profile
 * types can be used together, and profile IDs are unique and stable.
 *
 * Outstanding issues: None known.
 */

package com.example.coders

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Contains unit tests for the different user profile types.
 *
 * These tests verify that:
 * - Entrants store their personal information correctly.
 * - Campsite owners store their information and lists correctly.
 * - Admins store their information and notification logs.
 * - Different profile types can be stored together.
 * - Profile IDs are unique, stable, and non-empty.
 */
class ProfileTest {

    /**
     * Tests that an entrant stores its properties correctly.
     *
     * Creates an entrant and verifies that:
     * - The name, email, and phone number are correct.
     * - The reservations list is initially empty.
     * - The waitlists list is initially empty.
     */
    @Test
    fun entrant_creation_storesPropertiesCorrectly() {
        val entrant = Entrant(
            name = "Alice",
            email = "alice@example.com",
            phone = "780-111-2222",
            reservations = emptyList(),
            waitlists = emptyList()
        )

        assertEquals("Alice", entrant.name)
        assertEquals("alice@example.com", entrant.email)
        assertEquals("780-111-2222", entrant.phone)
        assertTrue(entrant.reservations.isEmpty())
        assertTrue(entrant.waitlists.isEmpty())
    }

    /**
     * Tests that a campsite owner stores its properties correctly.
     *
     * Creates a campsite owner and verifies that:
     * - The owner's name and email are correct.
     * - The campsites list is initially empty.
     * - The lotteries list is initially empty.
     */
    @Test
    fun campOwner_creation_storesPropertiesAndListsCorrectly() {
        val owner = CampOwner(
            name = "Bob Owner",
            email = "bob@campsite.ca",
            phone = "780-333-4444",
            campsites = emptyList(),
            lotteries = emptyList(),
            reservations = emptyList(),
            notificationsSent = emptyList(),
            notificationsReceived = emptyList()
        )

        assertEquals("Bob Owner", owner.name)
        assertEquals("bob@campsite.ca", owner.email)
        assertTrue(owner.campsites.isEmpty())
        assertTrue(owner.lotteries.isEmpty())
    }

    /**
     * Tests that an admin stores notification logs correctly.
     *
     * Creates an admin with one notification and verifies that:
     * - The admin's name is correct.
     * - The notification log contains exactly one notification.
     * - The stored notification has the correct title.
     */
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

    /**
     * Tests that different user profile types can be stored together.
     *
     * Creates an entrant, campsite owner, and admin as Profile objects.
     * Verifies that:
     * - The list contains exactly three profiles.
     * - Each profile stores the correct name.
     * - All three profile types can be accessed through the Profile class.
     */
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

    /**
     * Tests that profile IDs are unique, stable, and non-empty.
     *
     * Creates an entrant and campsite owner and verifies that:
     * - The two profiles have different IDs.
     * - The entrant's ID remains unchanged when accessed again.
     * - The entrant's ID is not blank.
     *
     * This helps ensure profiles can be identified individually.
     */
    @Test
    fun profile_idsAreUniqueAndStable() {
        val entrant: Profile = Entrant("User1", "u1@test.com", "111", emptyList(), emptyList())
        val owner: Profile = CampOwner("User2", "u2@test.com", "222", emptyList(), emptyList(), emptyList(), emptyList())
        val entrantId = entrant.id

        assertNotEquals(entrant.id, owner.id)
        assertEquals(entrantId, entrant.id)
        assertTrue(entrant.id.isNotBlank())
    }
}
