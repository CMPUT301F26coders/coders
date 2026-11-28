package com.example.coders

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

// The following test class was added using Anthropic, Claude Code, "Implement US 01.02.02: allow
// an entrant to leave a campsite lottery waiting list", 2026-11-28.
/**
 * Unit tests for US 01.02.02: an entrant leaves a campsite lottery waiting list.
 *
 * Exercises [waitlistLeaveError], the rule [LotteryRepo.leaveWaitlist] enforces before
 * removing an entrant, which also decides whether the Waitlists screen shows Leave Lottery.
 */
class LeaveWaitlistTest {

    private val now = LocalDateTime.of(2026, 12, 1, 12, 0)
    private val entrantId = "entrant1"

    /** An open lottery that [entrantId] has joined alongside one other entrant. */
    private fun joinedLottery() = Lottery(
        campOwnerId = "owner1",
        status = CampOwnerLotteryStatus.Active,
        registrationStartDate = "2026-11-30",
        registrationEndDate = "2026-12-05",
        waitlistEntrantIds = listOf(entrantId, "entrant2"),
        waitlistJoinedDates = mapOf(entrantId to "2026-11-30", "entrant2" to "2026-11-30")
    )

    // AC: Entrant can select "Leave Lottery" for a waiting list they joined.
    @Test
    fun entrantOnWaitlist_canLeave() {
        assertNull(joinedLottery().waitlistLeaveError(entrantId))
    }

    @Test
    fun entrantOnWaitlist_canLeaveAfterRegistrationClosesBeforeTheDraw() {
        val lottery = joinedLottery().copy(
            registrationStartDate = "2026-11-20",
            registrationEndDate = "2026-11-30"
        )

        assertEquals(EntrantWaitlistStatus.AwaitingDraw, lottery.entrantWaitlistStatus(entrantId, now))
        assertNull(lottery.waitlistLeaveError(entrantId))
    }

    // AC: Entrant cannot leave a waiting list they are not part of.
    @Test
    fun entrantNotOnWaitlist_cannotLeave() {
        assertEquals(
            WaitlistLeaveErrors.NOT_ON_WAITLIST,
            joinedLottery().waitlistLeaveError("entrant3")
        )
    }

    @Test
    fun entrantWhoAlreadyLeft_cannotLeaveAgain() {
        val afterLeaving = joinedLottery().copy(waitlistEntrantIds = listOf("entrant2"))

        assertEquals(
            WaitlistLeaveErrors.NOT_ON_WAITLIST,
            afterLeaving.waitlistLeaveError(entrantId)
        )
    }

    @Test
    fun blankEntrantId_cannotLeave() {
        assertEquals(WaitlistLeaveErrors.MISSING_ENTRANT, joinedLottery().waitlistLeaveError(""))
    }

    // Supporting rule: the waiting list is locked once winners are drawn.
    @Test
    fun drawnLottery_cannotBeLeft() {
        val drawnByFlag = joinedLottery().copy(drawCompleted = true)
        val drawnByStatus = joinedLottery().copy(status = CampOwnerLotteryStatus.Drawn)

        assertEquals(WaitlistLeaveErrors.ALREADY_DRAWN, drawnByFlag.waitlistLeaveError(entrantId))
        assertEquals(WaitlistLeaveErrors.ALREADY_DRAWN, drawnByStatus.waitlistLeaveError(entrantId))
    }

    // After leaving, the entrant is treated as a new entrant and may rejoin while registration is open.
    @Test
    fun entrantWhoLeft_canRejoinWhileRegistrationIsOpen() {
        val afterLeaving = joinedLottery().copy(
            waitlistEntrantIds = listOf("entrant2"),
            waitlistJoinedDates = mapOf("entrant2" to "2026-11-30")
        )

        assertNull(afterLeaving.waitlistJoinError(entrantId, now))
    }
}
