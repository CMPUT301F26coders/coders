package com.example.coders


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
                    java.time.LocalDate.parse(reservation.date.trim())
                }.getOrNull()

                reservedDate != null &&
                        !reservedDate.isBefore(checkIn) &&
                        reservedDate.isBefore(checkOut)

            } else {
                date.isNotBlank() &&
                        reservation.date == date.trim()
            }
        }
        matchesLocation && matchesGuests && matchesDate
    }
}
