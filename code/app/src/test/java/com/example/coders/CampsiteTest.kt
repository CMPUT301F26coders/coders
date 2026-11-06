/**
 * Contains unit tests for the application's Campsite model.
 *
 * Tests that campsite properties are stored correctly when created,
 * including the campsite name, capacity, and default values.
 * Also verifies that each campsite receives a unique and non-empty ID.
 *
 * Outstanding issues: None known.
 */

package com.example.coders

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Contains unit tests for the Campsite class.
 *
 * These tests verify that:
 * - A campsite stores its properties correctly when created.
 * - Optional properties have the correct default values.
 * - Each campsite receives a unique and non-empty ID.
 */
class CampsiteTest {

    private lateinit var owner: CampOwner

    /**
     * Creates a sample campsite owner before each test.
     *
     * This ensures every test starts with a new owner
     * and does not depend on the results of other tests.
     */
    @Before
    fun setUp() {
        owner = CampOwner(
            name = "Owner Mark",
            email = "mark@test.com",
            phone = "123",
            campsites = emptyList(),
            lotteries = emptyList(),
            reservations = emptyList(),
            notificationsSent = emptyList(),
            notificationsReceived = emptyList()
        )
    }

    /**
     * Tests that a newly created campsite stores its properties correctly.
     *
     * Verifies that:
     * - The campsite name and capacity match the provided values.
     * - The rating is initially null.
     * - Images, tags, waitlist, reservations, and notifications are empty.
     * - The QR code is initially an empty string.
     */
    @Test
    fun campsite_creation_storesPropertiesCorrectly() {
        val campsite = Campsite(
            name = "Elk Island Site A",
            location = "Elk Island",
            description = "Quiet lakefront site",
            capacity = 4,
            mapLocation = "53.6015, -112.8683",
            campOwner = owner
        )

        assertEquals("Elk Island Site A", campsite.name)
        assertEquals(4, campsite.capacity)
        assertNull(campsite.rating)
        assertTrue(campsite.images.isEmpty())
        assertTrue(campsite.tags.isEmpty())
        assertTrue(campsite.waitlist.isEmpty())
        assertTrue(campsite.reservations.isEmpty())
        assertTrue(campsite.notifications.isEmpty())
        assertEquals("", campsite.qr)
    }

    /**
     * Tests that each campsite receives a unique and non-empty ID.
     *
     * Creates two different campsites and verifies that:
     * - Their generated IDs are not equal.
     * - Neither ID is blank.
     *
     * This helps ensure campsites can be identified individually.
     */
    @Test
    fun campsite_idsAreUniqueAndNonEmpty() {
        val site1 = Campsite("Site 1", "Loc 1", "Desc 1", 2, "0,0", owner)
        val site2 = Campsite("Site 2", "Loc 2", "Desc 2", 4, "0,0", owner)

        assertNotEquals(site1.id, site2.id)
        assertTrue(site1.id.isNotBlank())
        assertTrue(site2.id.isNotBlank())
    }
}
