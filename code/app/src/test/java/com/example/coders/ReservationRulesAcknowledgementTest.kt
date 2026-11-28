package com.example.coders

import com.example.coders.ui.screens.rulesAcknowledgementLabel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

// The following test class was added using Anthropic, Claude Code, "Implement US 02.11.01:
// require entrants to acknowledge campsite rules and policies before confirming a reservation",
// 2026-11-28.
/**
 * Unit tests for US 02.11.01: entrants acknowledge a campsite's rules and policies before
 * confirming a reservation.
 *
 * Exercises [rulesAndPolicies], [confirmationError] (the rule [ReservationRepo.confirmReservation]
 * enforces and the entrant reservations screen uses to enable Confirm Reservation),
 * [confirmedWithRulesAcknowledgement], and the owner-facing [rulesAcknowledgementLabel].
 */
class ReservationRulesAcknowledgementTest {

    private val entrantId = "entrant1"
    private val acknowledgedAt = LocalDateTime.of(2026, 11, 28, 14, 3, 12, 987_000_000)

    /** A lottery offer awaiting [entrantId]'s confirmation. */
    private fun pendingOffer() = Reservation(
        id = "reservation1",
        campsiteId = "campsite1",
        campOwnerId = "owner1",
        entrantIds = listOf(entrantId),
        dates = "2026-12-10 - 2026-12-12",
        status = ReservationStatus.Pending
    )

    // AC: The system displays the campsite's rules and policies before confirmation.
    @Test
    fun rulesAndPolicies_extractsRulesFromRegisteredDescription() {
        val campsite = buildCampsiteForRegistration(
            owner = Profile(id = "owner1"),
            name = "Lake Site",
            location = "Jasper",
            description = "Lakefront spot",
            rules = " No campfires after 10pm ",
            capacity = 4,
            latitude = 53.5,
            longitude = -113.5,
            tags = emptyList()
        )

        assertEquals("No campfires after 10pm", campsite.rulesAndPolicies())
    }

    @Test
    fun rulesAndPolicies_isEmptyWhenCampsiteHasNoRules() {
        assertEquals("", Campsite(description = "Lakefront spot").rulesAndPolicies())
    }

    @Test
    fun rulesAndPolicies_usesTheLatestRulesSection() {
        val campsite = Campsite(
            description = "Lakefront spot\n\nRules: Old rules\n\nRules: Quiet hours at 11pm"
        )

        assertEquals("Quiet hours at 11pm", campsite.rulesAndPolicies())
    }

    // AC: Confirm Reservation is disabled until the box is checked, and the entrant cannot
    // complete a reservation without checking it.
    @Test
    fun uncheckedRules_blockConfirmation() {
        assertEquals(
            ReservationConfirmationErrors.RULES_NOT_ACKNOWLEDGED,
            pendingOffer().confirmationError(entrantId, rulesAccepted = false)
        )
    }

    // AC: The reservation is completed once the entrant checks the box and confirms.
    @Test
    fun checkedRules_allowConfirmation() {
        assertNull(pendingOffer().confirmationError(entrantId, rulesAccepted = true))
    }

    // Supporting rules: only the entrant's own pending offer can be confirmed.
    @Test
    fun otherEntrant_cannotConfirm() {
        assertEquals(
            ReservationConfirmationErrors.NOT_OWNER,
            pendingOffer().confirmationError("entrant2", rulesAccepted = true)
        )
        assertEquals(
            ReservationConfirmationErrors.NOT_OWNER,
            pendingOffer().confirmationError("", rulesAccepted = true)
        )
    }

    @Test
    fun nonPendingReservation_cannotBeConfirmed() {
        val active = pendingOffer().copy(status = ReservationStatus.Active)

        assertEquals(
            ReservationConfirmationErrors.NOT_PENDING,
            active.confirmationError(entrantId, rulesAccepted = true)
        )
    }

    // AC: The system saves the entrant's acknowledgement and the time it was given.
    @Test
    fun confirmedReservation_recordsAcknowledgementAndTime() {
        val offer = pendingOffer()
        assertFalse(offer.rulesAcknowledged)
        assertEquals("", offer.rulesAcknowledgedAt)

        val confirmed = offer.confirmedWithRulesAcknowledgement(acknowledgedAt)

        assertEquals(ReservationStatus.Active, confirmed.status)
        assertTrue(confirmed.rulesAcknowledged)
        assertEquals("2026-11-28T14:03:12", confirmed.rulesAcknowledgedAt)
        assertEquals(offer.copy(status = ReservationStatus.Active), confirmed.copy(
            rulesAcknowledged = false,
            rulesAcknowledgedAt = ""
        ))
    }

    // AC: The owner can see that the entrant confirmed the rules and policies.
    @Test
    fun ownerLabel_showsAcknowledgementTime() {
        val confirmed = pendingOffer().confirmedWithRulesAcknowledgement(acknowledgedAt)

        assertEquals(
            "Rules & Policies: Acknowledged on 2026-11-28 14:03",
            rulesAcknowledgementLabel(confirmed)
        )
    }

    @Test
    fun ownerLabel_showsNotAcknowledgedForLegacyReservations() {
        val legacy = pendingOffer().copy(status = ReservationStatus.Active)

        assertEquals("Rules & Policies: Not acknowledged", rulesAcknowledgementLabel(legacy))
    }
}
