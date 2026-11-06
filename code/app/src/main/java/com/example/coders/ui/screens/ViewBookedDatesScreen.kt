
package com.example.coders.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Displays a calendar of booked and available dates for a campsite.
 *
 * Campsite owners can:
 * - View booked dates in red and available dates in green.
 * - Move between months using the navigation buttons.
 * - Select a date to check its booking status.
 * - View reservation details for a booked date.
 * - See a message when the campsite has no bookings.
 *
 * This screen only handles the user interface.
 * Booking information is provided by another part of the app.
 *
 * @param navController Controls navigation back to the previous screen.
 * @param campsiteId The ID of the campsite being viewed.
 * @param bookedDates A map containing booked dates and their reservation details.
 *                    Dates must use the YYYY-MM-DD format.
 */
@Composable
fun ViewBookedDatesScreen(
    navController: NavController,
    campsiteId: String,
    bookedDates: Map<String, String>
) {

    // Remember which month and year the owner is viewing.
    var calendar by remember {
        mutableStateOf(Calendar.getInstance())
    }

    // Remember the date selected by the owner.
    var selectedDate by remember {
        mutableStateOf("")
    }

    // Get the year, month, and number of days in the current month.
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    // Convert the month into a readable name, such as "November 2026".
    val monthName = SimpleDateFormat(
        "MMMM yyyy",
        Locale.getDefault()
    ).format(calendar.time)

    // Main layout containing the calendar and reservation information.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(20.dp)
    ) {

        // Return to the previous screen.
        TextButton(
            onClick = {
                navController.popBackStack()
            }
        ) {
            Text("Back")
        }

        // Screen title.
        Text(
            text = "Booked Dates",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text("View booked and available dates for your campsite.")

        Spacer(modifier = Modifier.height(20.dp))

        // Show which campsite the owner is viewing.
        Text("Campsite ID: $campsiteId")

        Spacer(modifier = Modifier.height(20.dp))

        // Display the current month with buttons to change months.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Move to the previous month.
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

            // Display the month and year.
            Text(
                text = monthName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            // Move to the next month.
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

        // Find which weekday the first day of the month falls on.
        // This helps position dates correctly in the calendar.
        val firstDay = calendar.clone() as Calendar
        firstDay.set(Calendar.DAY_OF_MONTH, 1)

        // Calculate how many empty spaces are needed before day 1.
        val emptyDays = firstDay.get(Calendar.DAY_OF_WEEK) - 1

        // Display the calendar using seven columns, one for each weekday.
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(330.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // Display weekday headings from Sunday to Saturday.
            item { Text("S") }
            item { Text("M") }
            item { Text("T") }
            item { Text("W") }
            item { Text("T") }
            item { Text("F") }
            item { Text("S") }

            // Add empty spaces to align the first day of the month.
            items(emptyDays) {
                Spacer(modifier = Modifier.size(40.dp))
            }

            // Create a calendar box for every day in the month.
            items(daysInMonth) { index ->

                // Convert the index into the actual day number.
                val day = index + 1

                // Format the date as YYYY-MM-DD.
                // Example: 2026-11-10
                val date = "%04d-%02d-%02d".format(
                    Locale.US,
                    year,
                    month + 1,
                    day
                )

                // Check whether the date exists in the booked dates map.
                val isBooked = bookedDates.containsKey(date)

                // Use red for booked dates and green for available dates.
                val backgroundColor = if (isBooked) {
                    Color(0xFFFFCDD2)
                } else {
                    Color(0xFFC8E6C9)
                }

                // Display the date as a clickable calendar box.
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

                    // Show the day number inside the box.
                    Text(
                        text = day.toString(),
                        color = Color.Black
                    )
                }
            }
        }

        // Explain the calendar colors.
        Text("Red = Booked")
        Text("Green = Available")

        Spacer(modifier = Modifier.height(15.dp))

        // Show a message when there are no booked dates.
        if (bookedDates.isEmpty()) {
            Text("This campsite has no bookings.")
        }

        // Display booking information after the owner selects a date.
        if (selectedDate.isNotEmpty()) {

            // Show the selected date.
            Text(
                text = "Selected Date: $selectedDate",
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Check whether the selected date is booked.
            if (bookedDates.containsKey(selectedDate)) {

                // Show the booked status.
                Text(
                    text = "Status: Booked",
                    color = Color.Red
                )

                // Display the reservation details provided by the app.
                Text(
                    text = bookedDates[selectedDate] ?: ""
                )

            } else {

                // Show available status when no booking exists.
                Text(
                    text = "Status: Available",
                    color = Color(0xFF2E7D32)
                )
            }
        }
    }
}
