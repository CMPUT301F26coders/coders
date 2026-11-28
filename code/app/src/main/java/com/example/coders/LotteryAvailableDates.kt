/**
 * Works out which days of a lottery campsite a winning entrant can still reserve.
 *
 * A day is available when it falls inside the lottery's availability window and no active
 * reservation for that campsite covers it. Shared by the entrant's available-dates screen
 * and its unit tests.
 *
 * Outstanding issues: None known.
 */

package com.example.coders

import java.time.LocalDate

// The following enum, messages, and functions were added using Anthropic, Claude Code,
// "Implement US 01.04.01: show lottery winners the available dates for the campsite they won",
// 2026-11-28.

/** How one day of a lottery campsite is shown to a winning entrant. */
enum class CampsiteDateStatus(val label: String) {
    /** Inside the availability window and not reserved. */
    Available("Available"),
    /** Covered by another entrant's active reservation. */
    Booked("Booked"),
    /** Outside the lottery's availability window. */
    Unavailable("Unavailable"),
    /** Covered by the signed-in entrant's own pending or active reservation. */
    Yours("Your reservation")
}

/** Reasons the available-dates calendar is not shown, displayed to the entrant as-is. */
object LotteryAvailableDatesErrors {
    const val NOT_WINNER = "Available dates are shown once you have won this lottery."
    const val NO_AVAILABILITY = "The owner has not set available dates for this lottery yet."
}

/**
 * Returns why [entrantId] cannot view this lottery's available dates, or `null` when they can.
 *
 * Only winners see the calendar, decided by [entrantWaitlistStatus].
 *
 * @param entrantId The profile ID of the signed-in entrant.
 */
fun Lottery.availableDatesError(entrantId: String): String? = when {
    entrantId.isBlank() || entrantWaitlistStatus(entrantId) != EntrantWaitlistStatus.Selected ->
        LotteryAvailableDatesErrors.NOT_WINNER
    availabilityRange() == null -> LotteryAvailableDatesErrors.NO_AVAILABILITY
    else -> null
}

/**
 * Returns the days the lottery's campsites can be reserved, with both ends included, or
 * `null` when the availability dates are missing, invalid, or reversed.
 */
fun Lottery.availabilityRange(): ClosedRange<LocalDate>? {
    val start = runCatching { LocalDate.parse(availabilityStartDate) }.getOrNull() ?: return null
    val end = runCatching { LocalDate.parse(availabilityEndDate) }.getOrNull() ?: return null
    return if (start.isAfter(end)) null else start..end
}

/**
 * Returns the nights this reservation occupies: check-in is included and checkout is not,
 * so November 11 to November 13 occupies November 11 and 12.
 *
 * Uses [Reservation.startDate] and [Reservation.endDate], falling back to the
 * `"yyyy-MM-dd - yyyy-MM-dd"` [Reservation.dates] text that lottery offers store.
 * Missing or invalid dates occupy no nights.
 */
fun Reservation.reservedNights(): List<LocalDate> {
    val (startText, endText) = if (startDate.isNotBlank() && endDate.isNotBlank()) {
        startDate to endDate
    } else {
        val parts = dates.split(" - ")
        if (parts.size != 2) return emptyList()
        parts[0].trim() to parts[1].trim()
    }
    val start = runCatching { LocalDate.parse(startText) }.getOrNull() ?: return emptyList()
    val end = runCatching { LocalDate.parse(endText) }.getOrNull() ?: return emptyList()

    return generateSequence(start) { it.plusDays(1) }
        .takeWhile { it.isBefore(end) }
        .toList()
}

/**
 * Returns how [date] of [campsiteId] should be shown to [entrantId].
 *
 * The entrant's own pending or active reservation wins over everything, then other active
 * reservations, then the availability window. Cancelled, rejected, and past reservations
 * never block a date.
 *
 * @param date The day being shown.
 * @param campsiteId The lottery campsite being viewed.
 * @param reservations Reservations for that campsite.
 * @param entrantId The signed-in entrant's profile ID.
 */
fun Lottery.campsiteDateStatus(
    date: LocalDate,
    campsiteId: String,
    reservations: List<Reservation>,
    entrantId: String
): CampsiteDateStatus {
    val covering = reservations.filter { it.campsiteId == campsiteId && date in it.reservedNights() }

    return when {
        covering.any {
            entrantId.isNotBlank() && entrantId in it.entrantIds &&
                (it.status == ReservationStatus.Active || it.status == ReservationStatus.Pending)
        } -> CampsiteDateStatus.Yours
        covering.any { it.status == ReservationStatus.Active } -> CampsiteDateStatus.Booked
        availabilityRange()?.let { date in it } != true -> CampsiteDateStatus.Unavailable
        else -> CampsiteDateStatus.Available
    }
}

/**
 * Returns every day in the availability window that is still [CampsiteDateStatus.Available].
 *
 * @param campsiteId The lottery campsite being viewed.
 * @param reservations Reservations for that campsite.
 * @param entrantId The signed-in entrant's profile ID.
 */
fun Lottery.availableDates(
    campsiteId: String,
    reservations: List<Reservation>,
    entrantId: String
): List<LocalDate> {
    val range = availabilityRange() ?: return emptyList()
    return generateSequence(range.start) { it.plusDays(1) }
        .takeWhile { !it.isAfter(range.endInclusive) }
        .filter {
            campsiteDateStatus(it, campsiteId, reservations, entrantId) ==
                CampsiteDateStatus.Available
        }
        .toList()
}
