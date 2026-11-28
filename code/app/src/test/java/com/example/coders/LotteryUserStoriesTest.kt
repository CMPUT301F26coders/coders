
package com.example.coders

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class LotteryUserStoriesTest {

    @Test
    fun multipleOwnersCanShareLottery() {
        val lottery = Lottery(
            campOwnerId = "owner1",
            campsiteIds = listOf("camp1", "camp2", "camp3"),
            participatingOwnerIds = listOf("owner1", "owner2"),
            allowOtherOwners = true
        )

        assertEquals(3, lottery.campsiteIds.size)
        assertEquals(2, lottery.participatingOwnerIds.size)
        assertTrue(lottery.allowOtherOwners)
    }

    
    @Test
    fun setNumberOfWinners() {
        val lottery = Lottery(winnerCount = 5)
    
        assertEquals(5, lottery.winnerCount)
        assertTrue(lottery.winnerCount > 0)
    }

    
    @Test
    fun setWaitlistCapacity() {
        val lottery = Lottery(waitlistCapacity = 10)
    
        assertEquals(10, lottery.waitlistCapacity)
    }
    
    @Test
    fun unlimitedWaitlistCapacity() {
        val lottery = Lottery(waitlistCapacity = null)
    
        assertNull(lottery.waitlistCapacity)
    }

    
    @Test
    fun updateLotterySettings() {
        val lottery = Lottery(
            winnerCount = 3,
            registrationEndDate = "2026-12-20"
        )
    
        val updatedLottery = lottery.copy(
            winnerCount = 5
        )
    
        assertEquals(5, updatedLottery.winnerCount)
        assertEquals(
            "2026-12-20",
            updatedLottery.registrationEndDate
        )
    }

    
    @Test
    fun viewLotteryWaitlist() {
        val lottery = Lottery(
            waitlistEntrantIds = listOf(
                "entrant1",
                "entrant2",
                "entrant3"
            )
        )
    
        assertEquals(3, lottery.waitlistEntrantIds.size)
        assertTrue(
            lottery.waitlistEntrantIds.contains("entrant2")
        )
    }
    
    @Test
    fun emptyLotteryWaitlist() {
        val lottery = Lottery()
    
        assertTrue(lottery.waitlistEntrantIds.isEmpty())
    }

    // The following function was added using OpenAI, Codex, "Implement US 01.01.06: allow an
    // entrant to join an active lottery from campsite details", 2026-11-27.
    @Test
    fun registrationOpen_acceptsAnActiveUndrawnLotteryWithinItsInclusivePeriod() {
        val today = LocalDate.of(2026, 11, 27)
        val now = today.atTime(12, 0)
        val lottery = Lottery(
            status = CampOwnerLotteryStatus.Active,
            registrationStartDate = today.toString(),
            registrationEndDate = today.toString()
        )

        assertTrue(lottery.isRegistrationOpen(now))
    }

    // The following function was added using OpenAI, Codex, "Implement US 01.01.06: allow an
    // entrant to join an active lottery from campsite details", 2026-11-27.
    @Test
    fun registrationOpen_rejectsInactiveDrawnClosedAndInvalidLotteries() {
        val today = LocalDate.of(2026, 11, 27)
        val now = today.atTime(12, 0)
        val baseLottery = Lottery(
            status = CampOwnerLotteryStatus.Active,
            registrationStartDate = "2026-11-26",
            registrationEndDate = "2026-11-28"
        )

        assertFalse(
            baseLottery.copy(status = CampOwnerLotteryStatus.BookingPeriod)
                .isRegistrationOpen(now)
        )
        assertFalse(baseLottery.copy(drawCompleted = true).isRegistrationOpen(now))
        assertFalse(
            baseLottery.copy(registrationStartDate = "2026-11-28")
                .isRegistrationOpen(now)
        )
        assertFalse(
            baseLottery.copy(registrationEndDate = "2026-11-26")
                .isRegistrationOpen(now)
        )
        assertFalse(
            baseLottery.copy(registrationStartDate = "not-a-date")
                .isRegistrationOpen(now)
        )
    }

    @Test
    fun winnerChoosesCampsiteAndDates() {
        val reservation = Reservation(
            lotteryId = "lottery1",
            campsiteId = "camp2",
            startDate = "2026-12-10",
            endDate = "2026-12-12",
            status = ReservationStatus.Active
        )
    
        assertEquals("lottery1", reservation.lotteryId)
        assertEquals("camp2", reservation.campsiteId)
        assertEquals("2026-12-10", reservation.startDate)
        assertEquals("2026-12-12", reservation.endDate)
    }




}
