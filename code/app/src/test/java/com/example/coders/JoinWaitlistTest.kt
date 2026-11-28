package com.example.coders

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

// The following test class was added using Anthropic, Claude Code, "Implement US 01.02.01: allow
// an entrant to join the waiting list for an active campsite lottery", 2026-11-27.
/**
 * Unit tests for US 01.02.01: an entrant joins the waiting list for a campsite lottery.
 *
 * Exercises [waitlistJoinError], the rule [LotteryRepo.joinWaitlist] enforces before adding an
 * entrant, against a fixed clock so the registration-period checks are deterministic.
 */
class JoinWaitlistTest {

    private val now = LocalDateTime.of(2026, 12, 1, 12, 0)
    private val entrantId = "entrant1"

    /** A lottery whose registration runs from Nov 30 through Dec 5, so it is open at [now]. */
    private fun activeLottery() = Lottery(
        campOwnerId = "owner1",
        status = CampOwnerLotteryStatus.Active,
        registrationStartDate = "2026-11-30",
        registrationEndDate = "2026-12-05"
    )

    // AC: The entrant can join the waiting list for an active campsite lottery.
    @Test
    fun activeLottery_entrantCanJoinWaitlist() {
        assertNull(activeLottery().waitlistJoinError(entrantId, now))
    }

    @Test
    fun activeLotteryWithRoomOnWaitlist_entrantCanJoin() {
        val lottery = activeLottery().copy(
            waitlistCapacity = 3,
            waitlistEntrantIds = listOf("entrant2", "entrant3")
        )

        assertNull(lottery.waitlistJoinError(entrantId, now))
    }

    // AC: The application prevents the entrant from joining the same waiting list more than once.
    @Test
    fun entrantAlreadyOnWaitlist_cannotJoinAgain() {
        val lottery = activeLottery().copy(waitlistEntrantIds = listOf("entrant2", entrantId))

        assertEquals(
            WaitlistJoinErrors.ALREADY_JOINED,
            lottery.waitlistJoinError(entrantId, now)
        )
    }

    @Test
    fun entrantAlreadyOnWaitlist_otherEntrantsCanStillJoin() {
        val lottery = activeLottery().copy(waitlistEntrantIds = listOf(entrantId))

        assertNull(lottery.waitlistJoinError("entrant2", now))
    }

    // AC: The entrant cannot join the waiting list if the lottery is inactive.
    @Test
    fun lotteryWithInactiveStatus_cannotBeJoined() {
        listOf(CampOwnerLotteryStatus.BookingPeriod, CampOwnerLotteryStatus.Drawn).forEach { status ->
            assertEquals(
                "status $status should block joining",
                WaitlistJoinErrors.INACTIVE,
                activeLottery().copy(status = status).waitlistJoinError(entrantId, now)
            )
        }
    }

    @Test
    fun drawnLottery_cannotBeJoined() {
        val lottery = activeLottery().copy(drawCompleted = true)

        assertEquals(WaitlistJoinErrors.INACTIVE, lottery.waitlistJoinError(entrantId, now))
    }

    @Test
    fun registrationNotYetOpen_cannotBeJoined() {
        val lottery = activeLottery().copy(registrationStartDate = "2026-12-02")

        assertEquals(WaitlistJoinErrors.INACTIVE, lottery.waitlistJoinError(entrantId, now))
    }

    @Test
    fun registrationEnded_cannotBeJoined() {
        val lottery = activeLottery().copy(
            registrationStartDate = "2026-11-20",
            registrationEndDate = "2026-11-30"
        )

        assertEquals(WaitlistJoinErrors.INACTIVE, lottery.waitlistJoinError(entrantId, now))
    }

    @Test
    fun registrationEndTime_isExclusive() {
        val closesAtNoon = activeLottery().copy(
            registrationEndDate = "2026-12-01",
            registrationEndTime = "12:00"
        )
        val closesAfterNoon = closesAtNoon.copy(registrationEndTime = "12:01")

        assertEquals(WaitlistJoinErrors.INACTIVE, closesAtNoon.waitlistJoinError(entrantId, now))
        assertNull(closesAfterNoon.waitlistJoinError(entrantId, now))
    }

    @Test
    fun malformedRegistrationDates_cannotBeJoined() {
        val lottery = activeLottery().copy(registrationEndDate = "not-a-date")

        assertEquals(WaitlistJoinErrors.INACTIVE, lottery.waitlistJoinError(entrantId, now))
    }

    // Supporting rules enforced alongside the acceptance criteria.
    @Test
    fun fullWaitlist_cannotBeJoined() {
        val lottery = activeLottery().copy(
            waitlistCapacity = 2,
            waitlistEntrantIds = listOf("entrant2", "entrant3")
        )

        assertEquals(WaitlistJoinErrors.FULL, lottery.waitlistJoinError(entrantId, now))
    }

    @Test
    fun lotteryOwnerAndParticipatingOwners_cannotJoinTheirOwnWaitlist() {
        val lottery = activeLottery().copy(participatingOwnerIds = listOf("owner1", "owner2"))

        assertEquals(
            WaitlistJoinErrors.LOTTERY_MANAGER,
            lottery.waitlistJoinError("owner1", now)
        )
        assertEquals(
            WaitlistJoinErrors.LOTTERY_MANAGER,
            lottery.waitlistJoinError("owner2", now)
        )
    }

    @Test
    fun blankEntrantId_cannotJoin() {
        assertEquals(
            WaitlistJoinErrors.MISSING_ENTRANT,
            activeLottery().waitlistJoinError("", now)
        )
    }

    // Waitlists screen: where the entrant stands on a waiting list they joined.
    @Test
    fun joinedWaitlistWhileRegistrationOpen_showsRegistrationOpen() {
        val lottery = activeLottery().copy(waitlistEntrantIds = listOf(entrantId))

        assertEquals(
            EntrantWaitlistStatus.RegistrationOpen,
            lottery.entrantWaitlistStatus(entrantId, now)
        )
    }

    @Test
    fun joinedWaitlistAfterRegistrationCloses_showsAwaitingDraw() {
        val lottery = activeLottery().copy(
            registrationStartDate = "2026-11-20",
            registrationEndDate = "2026-11-30",
            waitlistEntrantIds = listOf(entrantId)
        )

        assertEquals(
            EntrantWaitlistStatus.AwaitingDraw,
            lottery.entrantWaitlistStatus(entrantId, now)
        )
    }

    @Test
    fun drawnLottery_showsSelectedForWinnersAndNotSelectedForOthers() {
        val lottery = activeLottery().copy(
            status = CampOwnerLotteryStatus.Drawn,
            drawCompleted = true,
            waitlistEntrantIds = listOf(entrantId, "entrant2"),
            winnerEntrantIds = listOf(entrantId)
        )

        assertEquals(
            EntrantWaitlistStatus.Selected,
            lottery.entrantWaitlistStatus(entrantId, now)
        )
        assertEquals(
            EntrantWaitlistStatus.NotSelected,
            lottery.entrantWaitlistStatus("entrant2", now)
        )
    }
}
