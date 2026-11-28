package com.example.coders.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import com.example.coders.ProfileRepo
import java.util.Calendar
import java.util.Locale
import com.example.coders.Profile
import com.example.coders.UserRole

/**
 * Displays the multi-owner lottery creation screen.
 *
 * Allows a campsite owner to select multiple campsites,
 * set availability, registration and booking periods,
 * set the number of winners, and choose other campsite
 * owners to invite to the lottery.
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
    currentOwnerId: String,
    onCreateLottery: (
        List<String>,
        List<String>,
        String,
        String,
        String,
        String,
        String,
        String,
        Int,
        Int?,
        String,
        String,
        String,
        String,
        String,
        String
    ) -> Unit
) {
    val context = LocalContext.current

    val selectedCampsiteIds = remember {
        mutableStateListOf<String>()
    }

    // Load campsite owners from Firebase.
    val profileRepo = remember { ProfileRepo() }

    var owners by remember {
        mutableStateOf<List<Profile>>(emptyList())
    }

    var ownersLoading by remember {
        mutableStateOf(true)
    }

    var ownersError by remember {
        mutableStateOf(false)
    }

    val selectedOwnerIds = remember {
        mutableStateListOf<String>()
    }

    // NEW: Owner search
    // Stores the name entered in the search field.
    var ownerSearch by remember {
        mutableStateOf("")
    }

    // Filters campsite owners based on the entered name.
    val filteredOwners = owners.filter { owner ->
        owner.name.contains(
            ownerSearch.trim(),
            ignoreCase = true
        )
    }

    LaunchedEffect(currentOwnerId) {
        ownersLoading = true

        profileRepo.getAllProfiles { profiles, error ->
            ownersLoading = false
            ownersError = error != null

            if (error == null) {
                owners = profiles.orEmpty()
                    .filter {
                        it.role == UserRole.NORMAL &&
                                it.id != currentOwnerId
                    }
            }
        }
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

    // NEW: Stores the availability start and end times.
    var availabilityStartTime by remember {
        mutableStateOf("")
    }

    var availabilityEndTime by remember {
        mutableStateOf("")
    }

    // NEW: Stores the registration start and end times.
    var registrationStartTime by remember {
        mutableStateOf("")
    }

    var registrationEndTime by remember {
        mutableStateOf("")
    }

    // NEW: Stores the booking start and end times.
    var bookingStartTime by remember {
        mutableStateOf("")
    }

    var bookingEndTime by remember {
        mutableStateOf("")
    }

    // Stores the number of winners entered by the owner.
    var numberOfWinners by remember {
        mutableStateOf("")
    }

    // NEW: Stores the optional maximum waitlist capacity.
    var maximumCapacity by remember {
        mutableStateOf("")
    }

    // NEW: Empty means no limit. Otherwise, capacity must be positive.
    val capacity = maximumCapacity.toIntOrNull()

    val validCapacity = maximumCapacity.isBlank() ||
            (capacity != null && capacity > 0)

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

    // NEW: Opens the clock to select a time.
    fun openTimePicker(saveTime: (String) -> Unit) {
        val calendar = Calendar.getInstance()

        TimePickerDialog(
            context,
            { _, hour, minute ->
                val time = String.format(
                    Locale.US,
                    "%02d:%02d",
                    hour,
                    minute
                )

                saveTime(time)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
    }

    // NEW: Checks whether an end date and time
    // occur after a start date and time.
    fun validDateTime(
        startDate: String,
        startTime: String,
        endDate: String,
        endTime: String
    ): Boolean {
        return startDate.isNotEmpty() &&
                startTime.isNotEmpty() &&
                endDate.isNotEmpty() &&
                endTime.isNotEmpty() &&
                "$endDate $endTime" > "$startDate $startTime"
    }

    // Checks whether the selected dates and times are valid.
    val validAvailabilityDates = validDateTime(
        availabilityStartDate,
        availabilityStartTime,
        availabilityEndDate,
        availabilityEndTime
    )

    val validRegistrationDates = validDateTime(
        registrationStartDate,
        registrationStartTime,
        registrationEndDate,
        registrationEndTime
    )

    val validBookingDates = validDateTime(
        bookingStartDate,
        bookingStartTime,
        bookingEndDate,
        bookingEndTime
    )

    // Booking starts after registration closes.
    val validPeriodOrder =
        validRegistrationDates &&
                validBookingDates &&
                "$bookingStartDate $bookingStartTime" >
                "$registrationEndDate $registrationEndTime"

    // Converts the number of winners into an integer.
    val winnerCount = numberOfWinners.toIntOrNull()

    val validWinnerCount =
        winnerCount != null && winnerCount > 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
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
            text = "Create Lottery",
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
                 * vertically beside the checkbox.
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

        // NEW: Availability start time.
        Text("Availability Start Time:")

        Button(
            onClick = {
                openTimePicker { time ->
                    availabilityStartTime = time
                }
            }
        ) {
            if (availabilityStartTime.isEmpty()) {
                Text("Start Time")
            } else {
                Text(availabilityStartTime)
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

        // NEW: Availability end time.
        Text("Availability End Time:")

        Button(
            onClick = {
                openTimePicker { time ->
                    availabilityEndTime = time
                }
            }
        ) {
            if (availabilityEndTime.isEmpty()) {
                Text("End Time")
            } else {
                Text(availabilityEndTime)
            }
        }

        if (availabilityStartDate.isNotEmpty() &&
            availabilityEndDate.isNotEmpty() &&
            availabilityStartTime.isNotEmpty() &&
            availabilityEndTime.isNotEmpty()
        ) {
            if (!validAvailabilityDates) {
                Text("Invalid availability dates or times")
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

        // NEW: Registration start time.
        Text("Registration Start Time:")

        Button(
            onClick = {
                openTimePicker { time ->
                    registrationStartTime = time
                }
            }
        ) {
            if (registrationStartTime.isEmpty()) {
                Text("Start Time")
            } else {
                Text(registrationStartTime)
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

        // NEW: Registration end time.
        Text("Registration End Time:")

        Button(
            onClick = {
                openTimePicker { time ->
                    registrationEndTime = time
                }
            }
        ) {
            if (registrationEndTime.isEmpty()) {
                Text("End Time")
            } else {
                Text(registrationEndTime)
            }
        }

        if (registrationStartDate.isNotEmpty() &&
            registrationEndDate.isNotEmpty() &&
            registrationStartTime.isNotEmpty() &&
            registrationEndTime.isNotEmpty()
        ) {
            if (!validRegistrationDates) {
                Text("Invalid registration dates or times")
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

        // NEW: Booking start time.
        Text("Booking Start Time:")

        Button(
            onClick = {
                openTimePicker { time ->
                    bookingStartTime = time
                }
            }
        ) {
            if (bookingStartTime.isEmpty()) {
                Text("Start Time")
            } else {
                Text(bookingStartTime)
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

        // NEW: Booking end time.
        Text("Booking End Time:")

        Button(
            onClick = {
                openTimePicker { time ->
                    bookingEndTime = time
                }
            }
        ) {
            if (bookingEndTime.isEmpty()) {
                Text("End Time")
            } else {
                Text(bookingEndTime)
            }
        }

        if (bookingStartDate.isNotEmpty() &&
            bookingEndDate.isNotEmpty() &&
            bookingStartTime.isNotEmpty() &&
            bookingEndTime.isNotEmpty()
        ) {
            if (!validBookingDates) {
                Text("Invalid dates or times")
            }
        }

        if (registrationEndDate.isNotEmpty() &&
            registrationEndTime.isNotEmpty() &&
            bookingStartDate.isNotEmpty() &&
            bookingStartTime.isNotEmpty()
        ) {
            if ("$bookingStartDate $bookingStartTime" <=
                "$registrationEndDate $registrationEndTime"
            ) {
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

        // NEW: OPTIONAL MAXIMUM WAITLIST CAPACITY

        Text(
            text = "Set Maximum Capacity",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Leave empty for no maximum capacity."
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = maximumCapacity,
            onValueChange = {
                maximumCapacity = it
            },
            label = {
                Text("Set Maximum Capacity")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (!validCapacity) {
            Text("Enter a positive whole number.")
        }

        Spacer(modifier = Modifier.height(20.dp))

        HorizontalDivider()

        Spacer(modifier = Modifier.height(20.dp))

        // 6. CHOOSE CAMPSITE OWNERS

        Text(
            text = "Choose Campsite Owners",
            style = MaterialTheme.typography.titleMedium
        )

        Text("Select the campsite owners you want to invite to this lottery.")

        Spacer(modifier = Modifier.height(12.dp))

        // NEW: Owner search
        // Allows the user to search for campsite owners by name.
        OutlinedTextField(
            value = ownerSearch,
            onValueChange = {
                ownerSearch = it
            },
            label = {
                Text("Search Campsite Owner")
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        when {
            ownersLoading -> Text("Loading campsite owners...")
            ownersError -> Text("Could not load campsite owners.")
            owners.isEmpty() -> Text("No campsite owners found.")

            // NEW: Owner search
            filteredOwners.isEmpty() ->
                Text("No matching campsite owners found.")

            // Displays only the owners matching the search.
            else -> filteredOwners.forEach { owner ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Checkbox(
                        checked = owner.id in selectedOwnerIds,
                        onCheckedChange = { checked ->
                            if (checked) {
                                if (owner.id !in selectedOwnerIds) {
                                    selectedOwnerIds.add(owner.id)
                                }
                            } else {
                                selectedOwnerIds.remove(owner.id)
                            }
                        }
                    )

                    Text(owner.name)
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // 7. CREATE LOTTERY

        Button(
            onClick = {
                winnerCount?.let { count ->
                    onCreateLottery(
                        selectedCampsiteIds.toList(),
                        selectedOwnerIds.toList(),
                        availabilityStartDate,
                        availabilityEndDate,
                        registrationStartDate,
                        registrationEndDate,
                        bookingStartDate,
                        bookingEndDate,
                        count,
                        capacity,
                        availabilityStartTime,
                        availabilityEndTime,
                        registrationStartTime,
                        registrationEndTime,
                        bookingStartTime,
                        bookingEndTime
                    )
                }
            },
            enabled = selectedCampsiteIds.isNotEmpty() &&
                    validAvailabilityDates &&
                    validRegistrationDates &&
                    validBookingDates &&
                    validPeriodOrder &&
                    validWinnerCount &&
                    validCapacity,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Lottery")
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
