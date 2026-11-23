
package com.example.coders.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

import com.example.coders.CampOwner
import com.example.coders.CampsiteRepo
import com.example.coders.Reservation
import com.example.coders.ReservationRepo
import com.example.coders.ReservationStatus
import com.example.coders.SignedInProfile

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * US 02.06.01: View Booked Dates.
 *
 * Displays active reservations for a campsite using Firebase.
 * Booked dates appear red and other dates appear green.
 * The calendar updates automatically when reservations change.
 *
 * Only the campsite owner may view the reservation details.
 *
 * @param navController Used for navigation.
 * @param campsiteId ID of the selected campsite.
 * @param bookedDates Kept for compatibility with MainActivity.
 */
@Composable
fun ViewBookedDatesScreen(
    navController: NavController,
    campsiteId: String,
    bookedDates: Map<String, String> = emptyMap()
) {
    val reservationRepo = remember { ReservationRepo() }
    val campsiteRepo = remember { CampsiteRepo() }

    val signedInProfile = remember { SignedInProfile() }
    val profile by signedInProfile.rememberSignedInProfile()
    val ownerId = (profile as? CampOwner)?.id

    var calendar by remember {
        mutableStateOf(Calendar.getInstance())
    }

    var selectedDate by remember {
        mutableStateOf("")
    }

    var reservations by remember {
        mutableStateOf<List<Reservation>>(emptyList())
    }

    var isOwner by remember {
        mutableStateOf(false)
    }

    var loading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    /**
     * Verify that the selected campsite belongs
     * to the signed-in campsite owner.
     */
    DisposableEffect(campsiteId, ownerId) {
        var disposed = false

        loading = true
        isOwner = false
        errorMessage = null
        reservations = emptyList()

        if (ownerId.isNullOrBlank() || campsiteId.isBlank()) {
            loading = false
            errorMessage = "Sign in as a campsite owner."
        } else {
            campsiteRepo.getCampsiteById(campsiteId) { campsite, error ->
                if (!disposed) {
                    val campsiteOwnerId = campsite?.campOwnerId
                        ?.ifBlank { campsite.campOwner.id }

                    when {
                        error != null ->
                            errorMessage = error.message

                        campsite == null ->
                            errorMessage = "Campsite not found."

                        campsiteOwnerId != ownerId ->
                            errorMessage = "You do not own this campsite."

                        else ->
                            isOwner = true
                    }

                    loading = false
                }
            }
        }

        onDispose {
            disposed = true
        }
    }

    /**
     * Listen for real-time Firebase reservation updates.
     * The listener is removed when the screen closes.
     */
    DisposableEffect(campsiteId, isOwner) {
        val listener = if (isOwner) {
            loading = true

            reservationRepo.listenForCampsiteReservations(
                campsiteId = campsiteId,
                onUpdate = { updatedReservations ->
                    reservations = updatedReservations
                    errorMessage = null
                    loading = false
                },
                onError = { error ->
                    errorMessage = error.message
                        ?: "Unable to load reservations."
                    loading = false
                }
            )
        } else {
            null
        }

        onDispose {
            listener?.remove()
        }
    }

    // Only active reservations count as booked.
    val activeReservations = reservations.filter {
        it.status == ReservationStatus.Active
    }

    // Convert each active reservation into individual booked dates.
    val firebaseBookedDates = remember(activeReservations) {
        val dates = mutableMapOf<String, String>()

        activeReservations.forEach { reservation ->
            val reservationDates = getReservationDates(
                reservation.startDate,
                reservation.endDate
            )

            reservationDates.forEach { date ->
                val details = """
                    Reservation ID: ${reservation.id}
                    Guests: ${reservation.guestNumber}
                    Check-in: ${reservation.startDate}
                    Check-out: ${reservation.endDate}
                """.trimIndent()

                dates[date] = listOfNotNull(
                    dates[date],
                    details
                ).joinToString("\n\n")
            }
        }

        dates.toMap()
    }

    val hasInvalidDates = activeReservations.any {
        getReservationDates(it.startDate, it.endDate).isEmpty()
    }

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    val monthName = SimpleDateFormat(
        "MMMM yyyy",
        Locale.getDefault()
    ).format(calendar.time)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        TextButton(
            onClick = { navController.popBackStack() }
        ) {
            Text("Back")
        }

        Text(
            text = "Booked Dates",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text("View booked and available dates for your campsite.")

        Spacer(modifier = Modifier.height(20.dp))

        when {
            loading -> {
                CircularProgressIndicator()
            }

            errorMessage != null -> {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }

            !isOwner -> {
                Text("You cannot view this campsite's bookings.")
            }

            else -> {
                Text("Campsite ID: $campsiteId")

                Spacer(modifier = Modifier.height(20.dp))

                // Navigate between calendar months.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            val newCalendar = calendar.clone() as Calendar
                            newCalendar.add(Calendar.MONTH, -1)
                            calendar = newCalendar
                            selectedDate = ""
                        }
                    ) {
                        Text("<")
                    }

                    Text(
                        text = monthName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = {
                            val newCalendar = calendar.clone() as Calendar
                            newCalendar.add(Calendar.MONTH, 1)
                            calendar = newCalendar
                            selectedDate = ""
                        }
                    ) {
                        Text(">")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                val firstDay = calendar.clone() as Calendar
                firstDay.set(Calendar.DAY_OF_MONTH, 1)

                val emptyDays = firstDay.get(Calendar.DAY_OF_WEEK) - 1

                // Display the calendar.
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.height(330.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                        item {
                            Text(day)
                        }
                    }

                    items(emptyDays) {
                        Spacer(modifier = Modifier.size(40.dp))
                    }

                    items(daysInMonth) { index ->
                        val day = index + 1

                        val date = "%04d-%02d-%02d".format(
                            Locale.US,
                            year,
                            month + 1,
                            day
                        )

                        val isBooked = firebaseBookedDates.containsKey(date)

                        val backgroundColor = if (isBooked) {
                            Color(0xFFFFCDD2)
                        } else {
                            Color(0xFFC8E6C9)
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    backgroundColor,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedDate = date
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.toString(),
                                color = Color.Black
                            )
                        }
                    }
                }

                Text("Red = Booked")
                Text("Green = Available")

                Spacer(modifier = Modifier.height(15.dp))

                // Show message when no dates are booked.
                if (firebaseBookedDates.isEmpty() && !hasInvalidDates) {
                    Text("This campsite has no bookings.")
                }

                if (hasInvalidDates) {
                    Text(
                        text = "Some active reservations have invalid or missing dates.",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // Display reservation details for selected date.
                if (selectedDate.isNotEmpty()) {
                    Text(
                        text = "Selected Date: $selectedDate",
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val details = firebaseBookedDates[selectedDate]

                    if (details != null) {
                        Text(
                            text = "Status: Booked",
                            color = Color.Red
                        )

                        Text(details)
                    } else {
                        Text(
                            text = "Status: Available",
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Converts reservation dates into individual booked nights.
 *
 * Expected format: yyyy-MM-dd.
 * The check-in date is included and checkout is excluded.
 *
 * Example:
 * November 11 to November 13 books November 11 and 12.
 */
private fun getReservationDates(
    startDate: String,
    endDate: String
): List<String> {

    val formatter = SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.US
    ).apply {
        isLenient = false
    }

    val start = try {
        formatter.parse(startDate)
    } catch (e: Exception) {
        null
    } ?: return emptyList()

    val end = try {
        formatter.parse(endDate)
    } catch (e: Exception) {
        null
    } ?: return emptyList()

    if (!start.before(end)) {
        return emptyList()
    }

    val current = Calendar.getInstance().apply {
        time = start
    }

    val endCalendar = Calendar.getInstance().apply {
        time = end
    }

    val result = mutableListOf<String>()

    while (current.before(endCalendar)) {
        result.add(formatter.format(current.time))
        current.add(Calendar.DAY_OF_MONTH, 1)
    }

    return result
}
