
package com.example.coders.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.coders.Campsite
import java.util.Calendar

/**
 * Displays the multi-owner lottery creation screen.
 *
 * Allows a campsite owner to select multiple campsites,
 * set availability, registration and booking periods,
 * set the number of winners, and decide whether other
 * owners may contribute campsites.
 *
 * @param navController Handles navigation.
 * @param campsites Campsites belonging to the signed-in owner.
 * @param onCreateLottery Sends the selected information
 * to the code responsible for creating the lottery.
 */
@Composable
fun MultiOwnerLotteryScreen(
    navController: NavController,
    campsites: List<Campsite>,
    onCreateLottery: (
        List<String>,
        Boolean,
        String,
        String,
        String,
        String,
        String,
        String,
        Int
    ) -> Unit
) {
    val context = LocalContext.current

    val selectedCampsiteIds = remember {
        mutableStateListOf<String>()
    }

    var allowOtherOwners by remember {
        mutableStateOf(false)
    }

    // Stores the availability start and end dates.
    var availabilityStartDate by remember {
        mutableStateOf("")
    }

    var availabilityEndDate by remember {
        mutableStateOf("")
    }

    // Stores the registration start and end dates.
    var registrationStartDate by remember {
        mutableStateOf("")
    }

    var registrationEndDate by remember {
        mutableStateOf("")
    }

    // Stores the booking start and end dates.
    var bookingStartDate by remember {
        mutableStateOf("")
    }

    var bookingEndDate by remember {
        mutableStateOf("")
    }

    // Stores the number of winners entered by the owner.
    var numberOfWinners by remember {
        mutableStateOf("")
    }

    val availableCampsiteIds = campsites.map { it.id }.toSet()

    LaunchedEffect(availableCampsiteIds) {
        selectedCampsiteIds.removeAll {
            it !in availableCampsiteIds
        }
    }

    // Opens the calendar to select a date.
    fun openCalendar(saveDate: (String) -> Unit) {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            context,
            { _, year, month, day ->
                val date = "%04d-%02d-%02d".format(
                    year, month + 1, day
                )

                saveDate(date)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // Checks whether the selected dates are valid.
    val validAvailabilityDates =
        availabilityStartDate.isNotEmpty() &&
                availabilityEndDate.isNotEmpty() &&
                availabilityEndDate > availabilityStartDate

    val validRegistrationDates =
        registrationStartDate.isNotEmpty() &&
                registrationEndDate.isNotEmpty() &&
                registrationEndDate > registrationStartDate

    val validBookingDates =
        bookingStartDate.isNotEmpty() &&
                bookingEndDate.isNotEmpty() &&
                bookingEndDate > bookingStartDate

    // Booking starts after registration closes.
    val validPeriodOrder =
        validRegistrationDates &&
                validBookingDates &&
                bookingStartDate > registrationEndDate

    // Converts the number of winners into an integer.
    val winnerCount = numberOfWinners.toIntOrNull()

    val validWinnerCount =
        winnerCount != null && winnerCount > 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        TextButton(
            onClick = {
                navController.popBackStack()
            }
        ) {
            Text("Back")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Create Multi-Owner Lottery",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 1. SELECT CAMPSITES

        Text(
            text = "Select Your Campsites",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "Choose the campsites you want in this lottery."
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (campsites.isEmpty()) {
            Text("You have no campsites available.")
        }

        /**
         * Displays all campsites belonging to the owner.
         * Each campsite has a checkbox so the owner can select
         * which campsites they want to include in the lottery.
         */
        campsites.forEach { campsite ->

            /**
             * Displays the checkbox, campsite name, and location
             * together in a horizontal row.
             */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {

                /**
                 * Checks whether this campsite is selected.
                 * If checked, its ID is added to selectedCampsiteIds.
                 * If unchecked, its ID is removed from the list.
                 */
                Checkbox(
                    checked = campsite.id in selectedCampsiteIds,
                    onCheckedChange = { checked ->
                        if (checked) {
                            if (campsite.id !in selectedCampsiteIds) {
                                selectedCampsiteIds.add(campsite.id)
                            }
                        } else {
                            selectedCampsiteIds.remove(campsite.id)
                        }
                    }
                )

                /**
                 * Displays the campsite name and location
                 * vertically beside its checkbox.
                 */
                Column {
                    Text(campsite.name)

                    Text(
                        text = campsite.location,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        HorizontalDivider()

        Spacer(modifier = Modifier.height(20.dp))

        // 2. CAMPSITE AVAILABILITY PERIOD

        Text(
            text = "Set Campsite Availability Period",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Choose when the campsites are available for winners to stay."
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text("Availability Start Date:")

        Button(
            onClick = {
                openCalendar { date ->
                    availabilityStartDate = date
                }
            }
        ) {
            if (availabilityStartDate.isEmpty()) {
                Text("Start Date")
            } else {
                Text(availabilityStartDate)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text("Availability End Date:")

        Button(
            onClick = {
                openCalendar { date ->
                    availabilityEndDate = date
                }
            }
        ) {
            if (availabilityEndDate.isEmpty()) {
                Text("End Date")
            } else {
                Text(availabilityEndDate)
            }
        }

        if (availabilityStartDate.isNotEmpty() &&
            availabilityEndDate.isNotEmpty()
        ) {
            if (availabilityEndDate <= availabilityStartDate) {
                Text("Invalid availability dates")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        HorizontalDivider()

        Spacer(modifier = Modifier.height(20.dp))

        // 3. REGISTRATION PERIOD

        Text(
            text = "Set Registration Period",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Choose when entrants can register for your campsite lottery."
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text("Registration Start Date:")

        Button(
            onClick = {
                openCalendar { date ->
                    registrationStartDate = date
                }
            }
        ) {
            if (registrationStartDate.isEmpty()) {
                Text("Start Date")
            } else {
                Text(registrationStartDate)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text("Registration End Date:")

        Button(
            onClick = {
                openCalendar { date ->
                    registrationEndDate = date
                }
            }
        ) {
            if (registrationEndDate.isEmpty()) {
                Text("End Date")
            } else {
                Text(registrationEndDate)
            }
        }

        if (registrationStartDate.isNotEmpty() &&
            registrationEndDate.isNotEmpty()
        ) {
            if (registrationEndDate <= registrationStartDate) {
                Text("Invalid registration dates")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        HorizontalDivider()

        Spacer(modifier = Modifier.height(20.dp))

        // 4. BOOKING PERIOD

        Text(
            text = "Set Booking Period",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            "Choose when lottery winners can claim their spot and book your campsite."
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text("Booking Start Date:")

        Button(
            onClick = {
                val calendar = Calendar.getInstance()

                DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        bookingStartDate = "%04d-%02d-%02d".format(
                            year, month + 1, day
                        )
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        ) {
            if (bookingStartDate.isEmpty()) {
                Text("Start Date")
            } else {
                Text(bookingStartDate)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text("Booking End Date:")

        Button(
            onClick = {
                val calendar = Calendar.getInstance()

                DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        bookingEndDate = "%04d-%02d-%02d".format(
                            year, month + 1, day
                        )
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        ) {
            if (bookingEndDate.isEmpty()) {
                Text("End Date")
            } else {
                Text(bookingEndDate)
            }
        }

        if (bookingStartDate.isNotEmpty() &&
            bookingEndDate.isNotEmpty()
        ) {
            if (bookingEndDate <= bookingStartDate) {
                Text("Invalid dates")
            }
        }

        if (registrationEndDate.isNotEmpty() &&
            bookingStartDate.isNotEmpty()
        ) {
            if (bookingStartDate <= registrationEndDate) {
                Text("Booking must start after registration ends.")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        HorizontalDivider()

        Spacer(modifier = Modifier.height(20.dp))

        // 5. NUMBER OF WINNERS

        Text(
            text = "Set Number of Winners",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Choose how many entrants can win this lottery."
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = numberOfWinners,
            onValueChange = {
                numberOfWinners = it
            },
            label = {
                Text("Number of Winners")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (numberOfWinners.isNotEmpty() && !validWinnerCount) {
            Text("Enter a positive whole number.")
        }

        Spacer(modifier = Modifier.height(20.dp))

        HorizontalDivider()

        Spacer(modifier = Modifier.height(20.dp))

        // 6. OTHER CAMPSITE OWNERS

        Text(
            text = "Other Campsite Owners",
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Checkbox(
                checked = allowOtherOwners,
                onCheckedChange = {
                    allowOtherOwners = it
                }
            )

            Text(
                text = "Allow other campsite owners to add their campsites"
            )
        }

        Text(
            text = if (allowOtherOwners) {
                "Other owners can contribute campsites to this lottery."
            } else {
                "Only your selected campsites will be included."
            },
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 7. CREATE LOTTERY

        Button(
            onClick = {
                winnerCount?.let { count ->
                    onCreateLottery(
                        selectedCampsiteIds.toList(),
                        allowOtherOwners,
                        availabilityStartDate,
                        availabilityEndDate,
                        registrationStartDate,
                        registrationEndDate,
                        bookingStartDate,
                        bookingEndDate,
                        count
                    )
                }
            },
            enabled = selectedCampsiteIds.isNotEmpty() &&
                    validAvailabilityDates &&
                    validRegistrationDates &&
                    validBookingDates &&
                    validPeriodOrder &&
                    validWinnerCount,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Lottery")
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
