package com.example.coders

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

// The following test class was added using Anthropic, Claude Code, "Implement US 01.04.01: show
// lottery winners the available dates for the campsite they won", 2026-11-28.

class LotteryAvailableDatesTest {

    private val entrantId = "winner1"
    private val campsiteId = "campsite1"


    private fun wonLottery() = Lottery(
        campOwnerId = "owner1",
        campsiteIds = listOf(campsiteId),
        status = CampOwnerLotteryStatus.Drawn,
        drawCompleted = true,
        waitlistEntrantIds = listOf(entrantId, "entrant2"),
        winnerEntrantIds = listOf(entrantId),
        availabilityStartDate = "2027-01-05",
        availabilityEndDate = "2027-01-10"
    )

    private fun reservation(
        start: String,
        end: String,
        status: ReservationStatus = ReservationStatus.Active,
        entrants: List<String> = listOf("entrant2")
    ) = Reservation(
        campsiteId = campsiteId,
        startDate = start,
        endDate = end,
        status = status,
        entrantIds = entrants
    )

    private fun statusOn(day: String, reservations: List<Reservation> = emptyList()) =
        wonLottery().campsiteDateStatus(LocalDate.parse(day), campsiteId, reservations, entrantId)

    // AC: When an entrant wins the lottery, the available dates are shown.
    @Test
    fun winner_canViewAvailableDates() {
        assertNull(wonLottery().availableDatesError(entrantId))
    }

    @Test
    fun nonWinner_cannotViewAvailableDates() {
        assertEquals(
            LotteryAvailableDatesErrors.NOT_WINNER,
            wonLottery().availableDatesError("entrant2")
        )
    }

    @Test
    fun undrawnLottery_hidesAvailableDates() {
        val undrawn = wonLottery().copy(
            status = CampOwnerLotteryStatus.Active,
            drawCompleted = false,
            winnerEntrantIds = emptyList()
        )

        assertEquals(LotteryAvailableDatesErrors.NOT_WINNER, undrawn.availableDatesError(entrantId))
    }

    @Test
    fun lotteryWithoutAvailability_explainsWhy() {
        val noDates = wonLottery().copy(availabilityStartDate = "", availabilityEndDate = "")

        assertEquals(
            LotteryAvailableDatesErrors.NO_AVAILABILITY,
            noDates.availableDatesError(entrantId)
        )
    }

    // AC: The calendar displays dates that are still available.
    @Test
    fun datesInsideWindow_areAvailable() {
        assertEquals(CampsiteDateStatus.Available, statusOn("2027-01-05"))
        assertEquals(CampsiteDateStatus.Available, statusOn("2027-01-10"))
    }

    // AC: Dates that are already reserved or unavailable are clearly indicated.
    @Test
    fun datesOutsideWindow_areUnavailable() {
        assertEquals(CampsiteDateStatus.Unavailable, statusOn("2027-01-04"))
        assertEquals(CampsiteDateStatus.Unavailable, statusOn("2027-01-11"))
    }

    @Test
    fun activeReservation_booksItsNightsButNotCheckout() {
        val booked = listOf(reservation("2027-01-07", "2027-01-09"))

        assertEquals(CampsiteDateStatus.Booked, statusOn("2027-01-07", booked))
        assertEquals(CampsiteDateStatus.Booked, statusOn("2027-01-08", booked))
        assertEquals(CampsiteDateStatus.Available, statusOn("2027-01-09", booked))
    }

    @Test
    fun cancelledRejectedAndOtherCampsiteReservations_doNotBlockDates() {
        val ignored = listOf(
            reservation("2027-01-07", "2027-01-09", ReservationStatus.Cancelled),
            reservation("2027-01-07", "2027-01-09", ReservationStatus.Rejected),
            reservation("2027-01-07", "2027-01-09").copy(campsiteId = "otherCampsite")
        )

        assertEquals(CampsiteDateStatus.Available, statusOn("2027-01-07", ignored))
    }

    // AC: Entrants can see the dates of the campsite they have been awarded.
    @Test
    fun entrantsOwnReservation_isShownAsTheirs() {
        val own = listOf(
            reservation("2027-01-05", "2027-01-06", ReservationStatus.Pending, listOf(entrantId))
        )

        assertEquals(CampsiteDateStatus.Yours, statusOn("2027-01-05", own))
    }

    @Test
    fun lotteryOfferDatesText_isUsedWhenStartAndEndAreMissing() {
        val offer = Reservation(
            campsiteId = campsiteId,
            dates = "2027-01-06 - 2027-01-08",
            status = ReservationStatus.Pending,
            entrantIds = listOf(entrantId)
        )

        assertEquals(
            listOf(LocalDate.parse("2027-01-06"), LocalDate.parse("2027-01-07")),
            offer.reservedNights()
        )
        assertEquals(CampsiteDateStatus.Yours, statusOn("2027-01-07", listOf(offer)))
    }

    @Test
    fun invalidReservationDates_reserveNothing() {
        assertTrue(Reservation(dates = "next week").reservedNights().isEmpty())
        assertTrue(reservation("2027-01-09", "2027-01-07").reservedNights().isEmpty())
    }

    @Test
    fun availableDates_listsOnlyOpenDaysInTheWindow() {
        val booked = listOf(reservation("2027-01-07", "2027-01-09"))

        assertEquals(
            listOf("2027-01-05", "2027-01-06", "2027-01-09", "2027-01-10").map(LocalDate::parse),
            wonLottery().availableDates(campsiteId, booked, entrantId)
        )
    }
}
