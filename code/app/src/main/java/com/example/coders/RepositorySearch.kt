/**
 * Provides utility functions for filtering profiles and campsites.
 *
 * Supports searching profiles by name and role, and filtering campsites
 * by location, guest capacity, and reservation availability.
 *
 * Outstanding issues: None known.
 */

package com.example.coders

import java.time.LocalDate

/**
 * Filters profiles by name and role type.
 *
 * @param profiles The list of profiles to filter.
 * @param name The name to search for.
 * @param type The profile role to filter by (Entrant, CampOwner, or Admin).
 * @return A list of profiles matching the specified criteria.
 */
fun filterProfiles(
    profiles: List<Profile>,
    name: String = "",
    type: String = ""
): List<Profile> {
    return profiles.filter { profile ->
        val matchesName = name.isBlank() ||
                profile.name.contains(name.trim(), ignoreCase = true)

        val matchesType = when (type.trim().lowercase()) {
            "", "user" -> true
            "normal" -> profile.role == UserRole.NORMAL
            "admin" -> profile.role == UserRole.ADMIN
            else -> false
        }

        matchesName && matchesType
    }
}

/**
 * Filters campsites by location, guest capacity, and reservation availability.
 *
 * @param campsites The list of campsites to filter.
 * @param location The location to search for, ignoring case.
 * @param date A specific reservation date to check for availability.
 * @param guests The minimum number of guests the campsite must accommodate.
 * @param startDate The check-in date.
 * @param endDate The check-out date.
 * @return A list of campsites matching the specified criteria.
 */
fun filterCampsites(
    campsites: List<Campsite>,
    location: String = "",
    date: String = "",
    guests: Int = 0,
    startDate: String = "",
    endDate: String = "",
    reservations: List<Reservation> = emptyList()
): List<Campsite> {

    if (guests < 0) return emptyList()

    val hasDateRange =
        startDate.isNotBlank() || endDate.isNotBlank()

    // A date range needs both check-in and check-out.
    if (hasDateRange &&
        (startDate.isBlank() || endDate.isBlank())
    ) {
        return emptyList()
    }

    val checkIn = if (hasDateRange) {
        parseIsoDate(startDate) ?: return emptyList()
    } else {
        null
    }

    val checkOut = if (hasDateRange) {
        parseIsoDate(endDate) ?: return emptyList()
    } else {
        null
    }

    if (checkIn != null && checkOut != null &&
        !checkOut.isAfter(checkIn)
    ) {
        return emptyList()
    }

    return campsites.filter { campsite ->

        val matchesLocation = location.isBlank() ||
                campsite.location.contains(
                    location.trim(),
                    ignoreCase = true
                )

        val matchesGuests = campsite.capacity >= guests

        val campsiteReservations = (campsite.reservations + reservations.filter {
            it.campsiteId == campsite.id || it.campsite.id == campsite.id
        }).distinctBy { it.id }
        val requestedSingleDate = if (!hasDateRange && date.isNotBlank()) {
            parseIsoDate(date)
        } else {
            null
        }

        val matchesDate = campsiteReservations.none { reservation ->
            if (reservation.status != ReservationStatus.Active &&
                reservation.status != ReservationStatus.Pending
            ) {
                false
            } else {
                val reservedPeriod = reservationPeriod(reservation)
                when {
                    checkIn != null && checkOut != null && reservedPeriod != null ->
                        checkIn.isBefore(reservedPeriod.second) &&
                            reservedPeriod.first.isBefore(checkOut)

                    requestedSingleDate != null && reservedPeriod != null ->
                        !requestedSingleDate.isBefore(reservedPeriod.first) &&
                            requestedSingleDate.isBefore(reservedPeriod.second)

                    !hasDateRange && date.isNotBlank() && requestedSingleDate == null ->
                        reservation.dates.trim() == date.trim()

                    else -> false
                }
            }
        }
        matchesLocation && matchesGuests && matchesDate
    }
}

private val reservationDatePattern = Regex("\\d{4}-\\d{2}-\\d{2}")

private fun parseIsoDate(value: String): LocalDate? =
    runCatching { LocalDate.parse(value.trim()) }.getOrNull()

/** Returns a reservation's occupied interval as [check-in, check-out). */
private fun reservationPeriod(reservation: Reservation): Pair<LocalDate, LocalDate>? {
    val dates = reservationDatePattern.findAll(reservation.dates)
        .mapNotNull { parseIsoDate(it.value) }
        .toList()
    val start = parseIsoDate(reservation.startDate) ?: dates.firstOrNull() ?: return null
    val end = parseIsoDate(reservation.endDate)
        ?: dates.getOrNull(1)
        ?: start.plusDays(1)

    return if (end.isAfter(start)) start to end else null
}
