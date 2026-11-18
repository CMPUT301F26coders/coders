
package com.example.coders

import org.junit.Assert.*
import org.junit.Test

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
