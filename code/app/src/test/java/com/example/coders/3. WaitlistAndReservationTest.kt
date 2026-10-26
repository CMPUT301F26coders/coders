package com.example.coders

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class WaitlistAndReservationTest {

    private lateinit var owner: CampOwner
    private lateinit var campsite: Campsite
    private lateinit var entrant: Entrant

    @Before
    fun setUp() {
        owner = CampOwner(
            name = "Owner Bob",
            email = "owner@test.com",
            phone = "123",
            campsites = emptyList(),
            lotteries = emptyList(),
            reservations = emptyList(),
            notifications = emptyList()
        )

        campsite = Campsite(
            name = "Jasper Spot",
            location = "Jasper",
            description = "Mountain view",
            capacity = 6,
            mapLocation = "0,0",
            campOwner = owner
        )

        entrant = Entrant(
            name = "Jane",
            email = "jane@test.com",
            phone = "456",
            bookings = emptyList(),
            waitlists = emptyList()
        )
    }

    @Test
    fun waitlist_creation_storesPropertiesCorrectly() {
        val waitlist = Waitlist(
            campsite = campsite,
            entrantList = listOf(entrant)
        )

        assertEquals(1, waitlist.entrantList.size)
        assertEquals("Jane", waitlist.entrantList[0].name)
        assertEquals("Jasper Spot", waitlist.campsite.name)
    }

    @Test
    fun reservation_creation_storesPropertiesCorrectly() {
        val reservation = Reservation(
            campsite = campsite,
            date = "2026-07-01",
            guestNumber = 4,
            status = ReservationStatus.Active
        )

        assertEquals(ReservationStatus.Active, reservation.status)
        assertEquals(4, reservation.guestNumber)
    }
}