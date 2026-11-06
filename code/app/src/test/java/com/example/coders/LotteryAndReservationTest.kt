
/**
 * Contains unit tests for the application's lottery and reservation models.
 *
 * Tests that waitlists correctly store campsites and entrants,
 * lotteries store their campsite information, dates, and statuses,
 * and reservations store their campsite, guest count, and status.
 * Also verifies that lottery and reservation statuses
 * contain the expected values.
 *
 * Outstanding issues: None known.
 */

package com.example.coders

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Contains unit tests for the lottery and reservation features.
 *
 * These tests verify that:
 * - Waitlists store the correct campsite and entrants.
 * - Lotteries store their campsite, date, and status correctly.
 * - Reservations store their campsite, guest count, and status.
 * - Lottery and reservation statuses contain the expected values.
 */
class LotteryAndReservationTest {

    private lateinit var owner: CampOwner
    private lateinit var campsite: Campsite
    private lateinit var entrant: Entrant

    /**
     * Creates sample objects before each test.
     *
     * Initializes a campsite owner, campsite, and entrant.
     * This ensures each test starts with fresh data and
     * can run independently of the other tests.
     */
    @Before
    fun setUp() {
        owner = CampOwner("Owner", "owner@test.com", "123", emptyList(), emptyList(), emptyList(), emptyList())
        campsite = Campsite("Jasper Spot", "Jasper", "Desc", 4, "0,0", owner)
        entrant = Entrant("Jane", "jane@test.com", "456", emptyList(), emptyList())
    }

    /**
     * Tests that a waitlist stores its campsite and entrants correctly.
     *
     * Creates a waitlist containing one entrant and verifies that:
     * - The campsite name matches the expected value.
     * - The waitlist contains exactly one entrant.
     * - The entrant's name is correct.
     */
    @Test
    fun waitlist_creation_storesCampsiteAndEntrants() {
        val waitlist = Waitlist(
            campsite = campsite,
            entrantList = listOf(entrant)
        )

        assertEquals("Jasper Spot", waitlist.campsite.name)
        assertEquals(1, waitlist.entrantList.size)
        assertEquals("Jane", waitlist.entrantList[0].name)
    }

    /**
     * Tests that a lottery stores its information correctly.
     *
     * Creates an active lottery and verifies that:
     * - The lottery contains exactly one campsite.
     * - The lottery date matches the provided date.
     * - The lottery status is Active.
     */
    @Test
    fun lottery_creation_storesCampsiteAndStatus() {
        val lottery = Lottery(
            campsites = listOf(campsite),
            date = "2026-07-01",
            status = CampOwnerLotteryStatus.Active
        )

        assertEquals(1, lottery.campsites.size)
        assertEquals("2026-07-01", lottery.date)
        assertEquals(CampOwnerLotteryStatus.Active, lottery.status)
    }

    /**
     * Tests that a reservation stores its information correctly.
     *
     * Creates an active reservation and verifies that:
     * - The reservation is linked to the correct campsite.
     * - The number of guests is stored correctly.
     * - The reservation status is Active.
     */
    @Test
    fun reservation_creation_storesCampsiteAndStatus() {
        val reservation = Reservation(
            campsite = campsite,
            dates = "2026-08-15",
            guestNumber = 3,
            status = ReservationStatus.Active,
            campOwner = owner,
            entrants = listOf(entrant)
        )

        assertEquals("Jasper Spot", reservation.campsite.name)
        assertEquals(3, reservation.guestNumber)
        assertEquals(ReservationStatus.Active, reservation.status)
    }

    /**
     * Tests that the lottery and reservation statuses
     * contain the expected number of values.
     *
     * Verifies that:
     * - CampOwnerLotteryStatus contains 3 values.
     * - EntrantLotteryStatus contains 4 values.
     * - ReservationStatus contains 5 values.
     * - Pending is the last value in ReservationStatus.
     */
    @Test
    fun reservationStatus_values_areCorrect() {
        assertEquals(3, CampOwnerLotteryStatus.values().size)
        assertEquals(4, EntrantLotteryStatus.values().size)
        assertEquals(5, ReservationStatus.values().size)
        assertEquals(ReservationStatus.Pending, ReservationStatus.values().last())
    }
}
