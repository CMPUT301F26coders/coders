
/**
 * Contains unit tests for the application's profile and campsite search features.
 *
 * Tests searching and filtering profiles by name and type, and campsites
 * by location, guest capacity, and available reservation dates.
 * Also tests combined filters, date ranges, cancelled reservations,
 * invalid inputs, and searches with no matching results.
 *
 * Outstanding issues: None known.
 */

package com.example.coders

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Contains unit tests for profile and campsite search functionality.
 *
 * These tests verify that:
 * - Profiles can be filtered by name and user type.
 * - Campsites can be filtered by location and guest capacity.
 * - Campsite availability is checked using reservation dates.
 * - Multiple search filters can be applied together.
 * - Invalid inputs and searches with no matches return empty results.
 * - Cancelled reservations do not block campsite availability.
 */
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

    /**
     * Tests searching for profiles by name.
     *
     * Searches for "alice" and verifies that:
     * - Exactly one profile is found.
     * - The matching profile belongs to Alice Smith.
     */
    @Test
    fun searchProfiles_byName() {
        val results = filterProfiles(profiles, name = "alice")

        assertEquals(1, results.size)
        assertEquals("Alice Smith", results[0].name)
    }

    /**
     * Tests searching for profiles by user type.
     *
     * Filters profiles by CampOwner and verifies that:
     * - Exactly one profile is found.
     * - The matching profile belongs to Bob Owner.
     */
    @Test
    fun searchProfiles_byType() {
        val results = filterProfiles(profiles, type = "CampOwner")

        assertEquals(1, results.size)
        assertEquals("Bob Owner", results[0].name)
    }

    /**
     * Tests searching for profiles using multiple filters.
     *
     * Searches by name and user type together.
     * Verifies that only Admin Sarah matches both filters.
     */
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

    /**
     * Tests searching for campsites by location.
     *
     * Searches for campsites in Edmonton and verifies
     * that only Edmonton Camp is returned.
     */
    @Test
    fun searchCampsites_byLocation() {
        val results = filterCampsites(
            campsites,
            location = "edmonton"
        )

        assertEquals(1, results.size)
        assertEquals("Edmonton Camp", results[0].name)
    }

    /**
     * Tests filtering campsites by the number of guests.
     *
     * Searches for campsites that can accommodate 6 guests.
     * Verifies that only Calgary Camp meets the requirement.
     */
    @Test
    fun searchCampsites_byGuests() {
        val results = filterCampsites(
            campsites,
            guests = 6
        )

        assertEquals(1, results.size)
        assertEquals("Calgary Camp", results[0].name)
    }

    /**
     * Tests campsite availability for a specific date.
     *
     * Searches for campsites available on November 15, 2026.
     * Verifies that both campsites are returned because
     * neither campsite has an existing reservation.
     */
    @Test
    fun searchCampsites_availableDate() {
        val results = filterCampsites(
            campsites,
            date = "2026-11-15"
        )

        // Neither campsite has reservations.
        assertEquals(2, results.size)
    }

    /**
     * Tests searching for campsites using multiple filters.
     *
     * Filters campsites by Calgary location and 5 guests.
     * Verifies that only Calgary Camp matches both conditions.
     */
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

    /**
     * Tests campsite searching with an invalid guest count.
     *
     * Searches using a negative number of guests.
     * Verifies that the search returns an empty list.
     */
    @Test
    fun searchCampsites_invalidGuests() {
        val results = filterCampsites(
            campsites,
            guests = -1
        )

        assertTrue(results.isEmpty())
    }

    /**
     * Tests that booked campsites are excluded from search results.
     *
     * Creates an active reservation for Edmonton Camp.
     * Searches for campsites on the reserved date and verifies that:
     * - Only one campsite is available.
     * - Calgary Camp is the available campsite.
     */
    @Test
    fun searchCampsites_excludesBookedDate() {
        val reservation = Reservation(
            campsite = edmonton,
            dates = "2026-11-15",
            guestNumber = 2,
            status = ReservationStatus.Active,
            campOwner = owner,
            entrants = listOf(entrant)

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

    /**
     * Tests searching for profiles when no names match.
     *
     * Searches for a nonexistent profile name and verifies
     * that an empty list is returned.
     */
    @Test
    fun searchProfiles_noMatches() {
        val results = filterProfiles(
            profiles,
            name = "Nonexistent"
        )

        assertTrue(results.isEmpty())
    }

    /**
     * Tests searching for profiles without applying any filters.
     *
     * Verifies that all three profiles are returned
     * when no search conditions are provided.
     */
    @Test
    fun searchProfiles_noFilters() {
        val results = filterProfiles(profiles)

        assertEquals(3, results.size)
    }

    /**
     * Tests searching for campsites when no locations match.
     *
     * Searches for campsites in Vancouver and verifies
     * that an empty list is returned.
     */
    @Test
    fun searchCampsites_noMatches() {
        val results = filterCampsites(
            campsites,
            location = "Vancouver"
        )

        assertTrue(results.isEmpty())
    }

    /**
     * Tests searching for campsites without applying any filters.
     *
     * Verifies that both campsites are returned
     * when no search conditions are provided.
     */
    @Test
    fun searchCampsites_noFilters() {
        val results = filterCampsites(campsites)

        assertEquals(2, results.size)
    }

    /**
     * Tests searching for campsites when the guest count
     * exceeds the capacity of every campsite.
     *
     * Searches for 10 guests and verifies that no
     * campsites are returned.
     */
    @Test
    fun searchCampsites_exceedsCapacity() {
        val results = filterCampsites(
            campsites,
            guests = 10
        )

        assertTrue(results.isEmpty())
    }

    /**
     * Tests that cancelled reservations do not block campsite availability.
     *
     * Creates a cancelled reservation for Edmonton Camp.
     * Verifies that Edmonton Camp is still returned
     * when searching for the cancelled reservation date.
     */
    @Test
    fun searchCampsites_cancelledReservationIsAvailable() {
        val reservation = Reservation(
            campsite = edmonton,
            dates = "2026-11-15",
            guestNumber = 2,
            status = ReservationStatus.Cancelled,
            campOwner = owner,
            entrants = listOf(entrant)
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

    /**
     * Tests campsite availability within a date range.
     *
     * Searches for campsites available from December 15
     * to December 20, 2026.
     * Verifies that both campsites are available.
     */
    @Test
    fun searchCampsites_dateRangeAvailable() {
        val results = filterCampsites(
            campsites,
            startDate = "2026-12-15",
            endDate = "2026-12-20"
        )

        assertEquals(2, results.size)
    }

    /**
     * Tests that a campsite booked during a requested date range
     * is excluded from the search results.
     *
     * Creates an active reservation for Edmonton Camp
     * within the requested date range.
     * Verifies that only Calgary Camp is returned.
     */
    @Test
    fun searchCampsites_dateRangeExcludesBookedCampsite() {
        val reservation = Reservation(
            campsite = edmonton,
            dates = "2026-12-17",
            guestNumber = 2,
            status = ReservationStatus.Active,
            campOwner = owner,
            entrants = listOf(entrant)
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

    /**
     * Tests that the checkout date is excluded from the booking range.
     *
     * Creates a reservation on December 20, 2026,
     * which is the requested checkout date.
     * Verifies that the campsite remains available
     * for the requested date range.
     */
    @Test
    fun searchCampsites_checkoutDateIsExcluded() {
        val reservation = Reservation(
            campsite = edmonton,
            dates = "2026-12-20",
            guestNumber = 2,
            status = ReservationStatus.Active,
            campOwner = owner,
            entrants = listOf(entrant)
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

    /**
     * Tests searching with an invalid date range.
     *
     * Uses a start date that occurs after the end date.
     * Verifies that the search returns an empty list.
     */
    @Test
    fun searchCampsites_invalidDateRange() {
        val results = filterCampsites(
            campsites,
            startDate = "2026-12-20",
            endDate = "2026-12-15"
        )

        assertTrue(results.isEmpty())
    }

    /**
     * Tests searching with an invalid date format.
     *
     * Provides an incorrectly formatted start date.
     * Verifies that the search returns an empty list.
     */
    @Test
    fun searchCampsites_invalidDateFormat() {
        val results = filterCampsites(
            campsites,
            startDate = "invalid-date",
            endDate = "2026-12-20"
        )

        assertTrue(results.isEmpty())
    }

    /**
     * Tests that cancelled reservations do not block
     * campsite availability within a date range.
     *
     * Creates a cancelled reservation during the requested period.
     * Verifies that Edmonton Camp remains available
     * and is included in the search results.
     */
    @Test
    fun searchCampsites_cancelledReservationInRange() {
        val reservation = Reservation(
            campsite = edmonton,
            dates = "2026-12-17",
            guestNumber = 2,
            status = ReservationStatus.Cancelled,
            campOwner = owner,
            entrants = listOf(entrant)
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
