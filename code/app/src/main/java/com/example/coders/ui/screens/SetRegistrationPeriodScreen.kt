
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
 * the registration period for a campsite lottery.
 *
 * The owner selects a registration start date and end date using
 * calendar date pickers. Both dates are displayed in YYYY-MM-DD format.
 *
 * The screen validates that:
 * - Both dates have been selected.
 * - The registration end date is later than the start date.
 *
 * The Save button is enabled only when both dates are valid.
 * When clicked, the selected dates are passed to [onSave].
 *
 * This screen handles date selection and validation only.
 * Saving the dates to Firebase must be handled by the code
 * that provides the [onSave] callback.
 *
 * @param navController Navigation controller used to return to the previous screen.
 * @param initialStartDate Previously saved registration start date, if editing a lottery.
 * @param initialEndDate Previously saved registration end date, if editing a lottery.
 * @param onSave Callback that receives the validated start and end dates as strings.
 */
@Composable
fun SetRegistrationPeriodScreen(
    navController: NavController,
    initialStartDate: String = "",
    initialEndDate: String = "",
    onSave: (String, String) -> Unit
) {
    val context = LocalContext.current

    // Stores the selected registration start date.
    // Uses the existing date if the owner is editing a lottery.
    var startDate by remember(initialStartDate) {
        mutableStateOf(initialStartDate)
    }

    // Stores the selected registration end date.
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
            text = "Set Registration Period",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Choose when entrants can register for your campsite lottery.",
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text("Registration Start Date:")

        // Opens a calendar for selecting the registration start date.
        Button(
            onClick = {
                val calendar = Calendar.getInstance()

                DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        // Formats the selected date as YYYY-MM-DD.
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
            // Displays the selected date or a default label.
            if (startDate.isEmpty()) {
                Text("Start Date")
            } else {
                Text(startDate)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text("Registration End Date:")

        // Opens a calendar for selecting the registration end date.
        Button(
            onClick = {
                val calendar = Calendar.getInstance()

                DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        // Formats the selected date as YYYY-MM-DD.
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
            // Displays the selected date or a default label.
            if (endDate.isEmpty()) {
                Text("End Date")
            } else {
                Text(endDate)
            }
        }

        // Displays an error if the end date is not later than the start date.
        if (startDate.isNotEmpty() && endDate.isNotEmpty()) {
            if (endDate <= startDate) {
                Text("Invalid dates")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sends the selected dates through the onSave callback.
        // The button remains disabled until both dates are valid.
        Button(
            onClick = {
                onSave(startDate, endDate)
            },
            enabled = startDate.isNotEmpty() &&
                    endDate.isNotEmpty() &&
                    endDate > startDate
        ) {
            Text("Save Registration Period")
        }
    }
}
