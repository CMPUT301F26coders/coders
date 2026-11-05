package com.example.coders

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RepositorySearchTest {

    private val entrant = Entrant(
        name = "Alice Smith",
        email = "alice@test.com",
        phone = "111"
    )

    private val owner = CampOwner(
        name = "Bob Owner",
        email = "bob@test.com",
        phone = "222"
    )

    private val admin = Admin(
        name = "Admin Sarah",
        email = "admin@test.com",
        phone = "333"
    )

    private val profiles: List<Profile> =
        listOf(entrant, owner, admin)

    private val edmonton = Campsite(
        name = "Edmonton Camp",
        location = "Edmonton",
        description = "Test campsite",
        capacity = 4,
        mapLocation = "Edmonton",
        campOwner = owner
    )

    private val calgary = Campsite(
        name = "Calgary Camp",
        location = "Calgary",
        description = "Test campsite",
        capacity = 8,
        mapLocation = "Calgary",
        campOwner = owner
    )

    private val campsites = listOf(edmonton, calgary)

    @Test
    fun searchProfiles_byName() {
        val results = filterProfiles(profiles, name = "alice")

        assertEquals(1, results.size)
        assertEquals("Alice Smith", results[0].name)
    }

    @Test
    fun searchProfiles_byType() {
        val results = filterProfiles(profiles, type = "CampOwner")

        assertEquals(1, results.size)
        assertEquals("Bob Owner", results[0].name)
    }

    @Test
    fun searchProfiles_combinedFilters() {
        val results = filterProfiles(
            profiles,
            name = "Sarah",
            type = "Admin"
        )

        assertEquals(1, results.size)
        assertEquals("Admin Sarah", results[0].name)
    }

    @Test
    fun searchCampsites_byLocation() {
        val results = filterCampsites(
            campsites,
            location = "edmonton"
        )

        assertEquals(1, results.size)
        assertEquals("Edmonton Camp", results[0].name)
    }

    @Test
    fun searchCampsites_byGuests() {
        val results = filterCampsites(
            campsites,
            guests = 6
        )

        assertEquals(1, results.size)
        assertEquals("Calgary Camp", results[0].name)
    }

    @Test
    fun searchCampsites_availableDate() {
        val results = filterCampsites(
            campsites,
            date = "2026-11-15"
        )

        // Neither campsite has reservations.
        assertEquals(2, results.size)
    }

    @Test
    fun searchCampsites_combinedFilters() {
        val results = filterCampsites(
            campsites,
            location = "Calgary",
            guests = 5
        )

        assertEquals(1, results.size)
        assertEquals("Calgary Camp", results[0].name)
    }

    @Test
    fun searchCampsites_invalidGuests() {
        val results = filterCampsites(
            campsites,
            guests = -1
        )

        assertTrue(results.isEmpty())
    }

    @Test
    fun searchCampsites_excludesBookedDate() {
        val reservation = Reservation(
            campsite = edmonton,
            date = "2026-11-15",
            guestNumber = 2,
            status = ReservationStatus.Active
        )

        val bookedCampsite = edmonton.copy(
            reservations = listOf(reservation)
        )

        val results = filterCampsites(
            listOf(bookedCampsite, calgary),
            date = "2026-11-15"
        )

        assertEquals(1, results.size)
        assertEquals("Calgary Camp", results[0].name)
    }

    @Test
    fun searchProfiles_noMatches() {
        val results = filterProfiles(
            profiles,
            name = "Nonexistent"
        )

        assertTrue(results.isEmpty())
    }

    @Test
    fun searchProfiles_noFilters() {
        val results = filterProfiles(profiles)

        assertEquals(3, results.size)
    }

    @Test
    fun searchCampsites_noMatches() {
        val results = filterCampsites(
            campsites,
            location = "Vancouver"
        )

        assertTrue(results.isEmpty())
    }

    @Test
    fun searchCampsites_noFilters() {
        val results = filterCampsites(campsites)

        assertEquals(2, results.size)
    }

    @Test
    fun searchCampsites_exceedsCapacity() {
        val results = filterCampsites(
            campsites,
            guests = 10
        )

        assertTrue(results.isEmpty())
    }

    @Test
    fun searchCampsites_cancelledReservationIsAvailable() {
        val reservation = Reservation(
            campsite = edmonton,
            date = "2026-11-15",
            guestNumber = 2,
            status = ReservationStatus.Cancelled
        )

        val campsiteWithCancelledReservation = edmonton.copy(
            reservations = listOf(reservation)
        )

        val results = filterCampsites(
            listOf(campsiteWithCancelledReservation),
            date = "2026-11-15"
        )

        assertEquals(1, results.size)
        assertEquals("Edmonton Camp", results[0].name)
    }

    @Test
    fun searchCampsites_dateRangeAvailable() {
        val results = filterCampsites(
            campsites,
            startDate = "2026-12-15",
            endDate = "2026-12-20"
        )

        assertEquals(2, results.size)
    }

    @Test
    fun searchCampsites_dateRangeExcludesBookedCampsite() {
        val reservation = Reservation(
            campsite = edmonton,
            date = "2026-12-17",
            guestNumber = 2,
            status = ReservationStatus.Active
        )

        val bookedCampsite = edmonton.copy(
            reservations = listOf(reservation)
        )

        val results = filterCampsites(
            listOf(bookedCampsite, calgary),
            startDate = "2026-12-15",
            endDate = "2026-12-20"
        )

        assertEquals(1, results.size)
        assertEquals("Calgary Camp", results[0].name)
    }

    @Test
    fun searchCampsites_checkoutDateIsExcluded() {
        val reservation = Reservation(
            campsite = edmonton,
            date = "2026-12-20",
            guestNumber = 2,
            status = ReservationStatus.Active
        )

        val bookedCampsite = edmonton.copy(
            reservations = listOf(reservation)
        )

        val results = filterCampsites(
            listOf(bookedCampsite),
            startDate = "2026-12-15",
            endDate = "2026-12-20"
        )

        assertEquals(1, results.size)
    }

    @Test
    fun searchCampsites_invalidDateRange() {
        val results = filterCampsites(
            campsites,
            startDate = "2026-12-20",
            endDate = "2026-12-15"
        )

        assertTrue(results.isEmpty())
    }

    @Test
    fun searchCampsites_invalidDateFormat() {
        val results = filterCampsites(
            campsites,
            startDate = "invalid-date",
            endDate = "2026-12-20"
        )

        assertTrue(results.isEmpty())
    }

    @Test
    fun searchCampsites_cancelledReservationInRange() {
        val reservation = Reservation(
            campsite = edmonton,
            date = "2026-12-17",
            guestNumber = 2,
            status = ReservationStatus.Cancelled
        )

        val campsiteWithCancelledReservation = edmonton.copy(
            reservations = listOf(reservation)
        )

        val results = filterCampsites(
            listOf(campsiteWithCancelledReservation),
            startDate = "2026-12-15",
            endDate = "2026-12-20"
        )

        assertEquals(1, results.size)
        assertEquals("Edmonton Camp", results[0].name)
    }

}
