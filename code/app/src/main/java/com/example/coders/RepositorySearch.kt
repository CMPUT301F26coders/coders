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
    guests: Int = 0
): List<Campsite> {
    if (guests < 0) return emptyList()

    return campsites.filter { campsite ->
        val matchesLocation = location.isBlank() ||
                campsite.location.contains(
                    location.trim(),
                    ignoreCase = true
                )

        val matchesGuests = campsite.capacity >= guests

        val matchesDate = date.isBlank() ||
                campsite.reservations.none { reservation ->
                    reservation.date == date.trim() &&
                            reservation.status == ReservationStatus.Active
                }

        matchesLocation && matchesGuests && matchesDate
    }
}
