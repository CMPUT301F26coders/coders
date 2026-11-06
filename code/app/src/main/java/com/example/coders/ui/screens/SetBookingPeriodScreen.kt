
package com.example.coders.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import java.util.Calendar

/**
 * Displays a screen where a campsite owner can set or edit
 * the booking period for a campsite lottery.
 *
 * Lottery winners can book their campsite during this period.
 * The owner selects a start date and end date using calendar pickers.
 *
 * The screen checks that:
 * - Both dates have been selected.
 * - The end date is later than the start date.
 *
 * The Save button is only enabled when the dates are valid.
 * Clicking Save passes the selected dates to [onSave].
 *
 * This screen handles date selection and validation.
 * Saving the dates to Firebase is handled by the code
 * that provides the [onSave] callback.
 *
 * @param navController Used to return to the previous screen.
 * @param initialStartDate Previously selected booking start date, if any.
 * @param initialEndDate Previously selected booking end date, if any.
 * @param onSave Receives the selected start and end dates.
 */
@Composable
fun SetBookingPeriodScreen(
    navController: NavController,
    initialStartDate: String = "",
    initialEndDate: String = "",
    onSave: (String, String) -> Unit
) {
    val context = LocalContext.current

    // Stores the booking start date.
    // Uses the existing date if the owner is editing a lottery.
    var startDate by remember(initialStartDate) {
        mutableStateOf(initialStartDate)
    }

    // Stores the booking end date.
    // Uses the existing date if the owner is editing a lottery.
    var endDate by remember(initialEndDate) {
        mutableStateOf(initialEndDate)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(20.dp)
    ) {

        // Returns the owner to the previous screen.
        TextButton(
            onClick = { navController.popBackStack() }
        ) {
            Text("Back")
        }

        Text(
            text = "Set Booking Period",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Choose when lottery winners can claim their spot and book your campsite.",
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text("Booking Start Date:")

        // Opens a calendar to select the booking start date.
        Button(
            onClick = {
                val calendar = Calendar.getInstance()

                DatePickerDialog(
                    context,
                    { _, year, month, day ->

                        // Stores the date in YYYY-MM-DD format.
                        startDate = "%04d-%02d-%02d".format(
                            year, month + 1, day
                        )
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        ) {

            // Shows the selected date or the default button label.
            if (startDate.isEmpty()) {
                Text("Start Date")
            } else {
                Text(startDate)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text("Booking End Date:")

        // Opens a calendar to select the booking end date.
        Button(
            onClick = {
                val calendar = Calendar.getInstance()

                DatePickerDialog(
                    context,
                    { _, year, month, day ->

                        // Stores the date in YYYY-MM-DD format.
                        endDate = "%04d-%02d-%02d".format(
                            year, month + 1, day
                        )
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        ) {

            // Shows the selected date or the default button label.
            if (endDate.isEmpty()) {
                Text("End Date")
            } else {
                Text(endDate)
            }
        }

        // Displays an error if the end date is not after the start date.
        if (startDate.isNotEmpty() && endDate.isNotEmpty()) {
            if (endDate <= startDate) {
                Text("Invalid dates")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sends valid dates to the onSave callback.
        // Save is disabled until both dates are selected and valid.
        Button(
            onClick = {
                onSave(startDate, endDate)
            },
            enabled = startDate.isNotEmpty() &&
                    endDate.isNotEmpty() &&
                    endDate > startDate
        ) {
            Text("Save Booking Period")
        }
    }
}
