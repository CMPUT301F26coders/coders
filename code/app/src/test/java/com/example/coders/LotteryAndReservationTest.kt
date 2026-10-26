package com.example.coders

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class LotteryAndReservationTest {

    private lateinit var owner: CampOwner
    private lateinit var campsite: Campsite
    private lateinit var entrant: Entrant

    @Before
    fun setUp() {
        owner = CampOwner("Owner", "owner@test.com", "123", emptyList(), emptyList(), emptyList(), emptyList())
        campsite = Campsite("Jasper Spot", "Jasper", "Desc", 4, "0,0", owner)
        entrant = Entrant("Jane", "jane@test.com", "456", emptyList(), emptyList())
    }

    @Test
    fun waitlist_creation_storesCampsiteAndEntrants() {
        val waitlist = Waitlist(
            campsite = campsite,
            entrantList = listOf(entrant)
        )

        assertEquals("Jasper Spot", waitlist.campsite.name)
        assertEquals(1, waitlist.entrantList.size)
        assertEquals("Jane", waitlist.entrantList[0].name)
    }

    @Test
    fun lottery_creation_storesCampsiteAndStatus() {
        val lottery = Lottery(
            campsites = listOf(campsite),
            date = "2026-07-01",
            status = CampOwnerLotteryStatus.Active
        )

        assertEquals(1, lottery.campsites.size)
        assertEquals("2026-07-01", lottery.date)
        assertEquals(CampOwnerLotteryStatus.Active, lottery.status)
    }

    @Test
    fun reservation_creation_storesCampsiteAndStatus() {
        val reservation = Reservation(
            campsite = campsite,
            date = "2026-08-15",
            guestNumber = 3,
            status = ReservationStatus.Active
        )

        assertEquals("Jasper Spot", reservation.campsite.name)
        assertEquals(3, reservation.guestNumber)
        assertEquals(ReservationStatus.Active, reservation.status)
    }

    @Test
    fun reservationStatus_values_areCorrect() {
        assertEquals(3, CampOwnerLotteryStatus.values().size)
        assertEquals(4, EntrantLotteryStatus.values().size)
        assertEquals(4, ReservationStatus.values().size)
    }
}