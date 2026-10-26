package com.example.coders

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CampsiteTest {

    private lateinit var owner: CampOwner

    @Before
    fun setUp() {
        owner = CampOwner(
            name = "Owner Mark",
            email = "mark@test.com",
            phone = "123",
            campsites = emptyList(),
            lotteries = emptyList(),
            reservations = emptyList(),
            notifications = emptyList()
        )
    }

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

    @Test
    fun campsite_ids_autoIncrementOnCreation() {
        val site1 = Campsite("Site 1", "Loc 1", "Desc 1", 2, "0,0", owner)
        val site2 = Campsite("Site 2", "Loc 2", "Desc 2", 4, "0,0", owner)

        assertNotEquals(site1.id, site2.id)
        assertEquals(site1.id + 1, site2.id)
    }
}