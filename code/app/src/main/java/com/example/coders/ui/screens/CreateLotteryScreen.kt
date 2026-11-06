
package com.example.coders.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.coders.Campsite
import java.util.Calendar
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

/**
 * Displays the Create Lottery screen for campsite owners.
 *
 * Allows a campsite owner to select a campsite and choose its
 * available start and end dates using calendar date pickers.
 *
 * The Create Lottery button is enabled only when a campsite
 * is selected, both dates are provided, and the end date is
 * not earlier than the start date.
 *
 * @param navController Handles navigation back to the previous screen.
 * @param campsites List of campsites available for selection.
 * @param onCreateLottery Callback invoked when Create Lottery is clicked.
 * Receives the selected campsite, start date, and end date.
 */
@Composable
fun CreateLotteryScreen(
    navController: NavController,
    campsites: List<Campsite>,
    onCreateLottery: (Campsite, String, String) -> Unit
) {
    val context = LocalContext.current

    var selectedCampsite by remember { mutableStateOf<Campsite?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    /**
     * Opens a calendar dialog for selecting a date.
     *
     * Uses the current date as the initial calendar selection.
     * Formats the selected date as yyyy-MM-dd.
     *
     * @param saveDate Callback that receives the formatted date
     * after the user selects a date from the calendar.
     */
    fun openCalendar(saveDate: (String) -> Unit) {
        val today = Calendar.getInstance()

        DatePickerDialog(
            context,
            { _, year, month, day ->
                val date = "%04d-%02d-%02d".format(year, month + 1, day)
                saveDate(date)
            },
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Column(modifier = Modifier.padding(20.dp)) {

        // 1. Back button
        Button(onClick = { navController.popBackStack() }) {
            Text("Back")
        }

        // 2. Screen title
        Text(
            text = "Create Lottery",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(20.dp))

        // 3. Select campsite text
        Text("Select Campsite")

        // 4. Campsite dropdown
        Box {
            Button(onClick = { showMenu = true }) {
                val campsite = selectedCampsite

                if (campsite == null) {
                    Text("Choose Campsite")
                } else {
                    Text(campsite.name)
                }
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                campsites.forEach { campsite ->
                    DropdownMenuItem(
                        text = { Text(campsite.name) },
                        onClick = {
                            selectedCampsite = campsite
                            showMenu = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // 5. Available dates text
        Text("Available Dates")

        // 6. Start date picker
        Button(onClick = {
            openCalendar { date ->
                startDate = date
            }
        }) {
            if (startDate == "") {
                Text("Start Date")
            } else {
                Text(startDate)
            }
        }

        // 7. End date picker
        Button(onClick = {
            openCalendar { date ->
                endDate = date
            }
        }) {
            if (endDate == "") {
                Text("End Date")
            } else {
                Text(endDate)
            }
        }

        Spacer(Modifier.height(20.dp))

        // 8. Create Lottery button
        Button(
            onClick = {
                val campsite = selectedCampsite

                if (campsite != null) {
                    onCreateLottery(campsite, startDate, endDate)
                }
            },
            enabled = selectedCampsite != null &&
                    startDate.isNotEmpty() &&
                    endDate.isNotEmpty() &&
                    endDate >= startDate
        ) {
            Text("Create Lottery")
        }
    }
}
