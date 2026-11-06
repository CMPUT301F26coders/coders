/**
 * Provides utility functions for filtering profiles and campsites.
 *
 * Supports searching profiles by name and role, and filtering campsites
 * by location, guest capacity, and reservation availability.
 *
 * Outstanding issues: None known.
 */

package com.example.coders

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
            "" -> true
            "entrant" -> profile is Entrant
            "campowner", "camp owner" -> profile is CampOwner
            "admin" -> profile is Admin
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
    endDate: String = ""
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
        runCatching {
            java.time.LocalDate.parse(startDate.trim())
        }.getOrNull() ?: return emptyList()
    } else {
        null
    }

    val checkOut = if (hasDateRange) {
        runCatching {
            java.time.LocalDate.parse(endDate.trim())
        }.getOrNull() ?: return emptyList()
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

        val matchesDate = campsite.reservations.none { reservation ->

            if (reservation.status != ReservationStatus.Active) {
                false
            } else if (checkIn != null && checkOut != null) {

                val reservedDate = runCatching {
                    java.time.LocalDate.parse(reservation.dates.trim())
                }.getOrNull()

                reservedDate != null &&
                        !reservedDate.isBefore(checkIn) &&
                        reservedDate.isBefore(checkOut)

            } else {
                date.isNotBlank() &&
                        reservation.dates == date.trim()
            }
        }
        matchesLocation && matchesGuests && matchesDate
    }
}
