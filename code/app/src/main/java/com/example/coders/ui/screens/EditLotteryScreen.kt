
package com.example.coders.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

import com.example.coders.AuthManager
import com.example.coders.CampsiteRepo
import com.example.coders.Lottery
import com.example.coders.LotteryRepo
import com.example.coders.ProfileRepo

import java.util.Calendar
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Displays the edit lottery screen for a campsite owner.
 *
 * Loads an existing lottery, lets its owner change campsite, registration,
 * and booking dates and winner count, and saves valid changes to Firebase.
 * Editing is blocked once the original registration period has ended.
 *
 * US 02.04.06 Update Lottery Listing.
 *
 * @param navController Controls navigation between screens.
 * @param lotteryId ID of the lottery to load and edit.
 */
@Composable
fun EditLotteryScreen(
    navController: NavController,
    lotteryId: String
) {

    val context = LocalContext.current

    val authManager = remember {
        AuthManager(context.applicationContext)
    }

    val profileRepo = remember { ProfileRepo() }
    val campsiteRepo = remember { CampsiteRepo() }
    val lotteryRepo = remember { LotteryRepo() }

    var ownerId by remember { mutableStateOf("") }

    var lottery by remember {
        mutableStateOf<Lottery?>(null)
    }

    var campsiteName by remember { mutableStateOf("") }
    var campsiteLocation by remember { mutableStateOf("") }

    // Campsite Period.
    var availabilityStartDate by remember { mutableStateOf("") }
    var availabilityEndDate by remember { mutableStateOf("") }

    // Registration Period.
    var registrationStartDate by remember { mutableStateOf("") }
    var registrationEndDate by remember { mutableStateOf("") }

    // Booking Period.
    var bookingStartDate by remember { mutableStateOf("") }
    var bookingEndDate by remember { mutableStateOf("") }

    // Number of Winners.
    var winnerText by remember { mutableStateOf("") }

    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var backClicked by remember {mutableStateOf(false)}

    /**
     * Loads the selected lottery and campsite details from Firebase.
     * Checks that the signed-in user is the lottery's campsite owner.
     * Fills the editable fields with the saved lottery values.
     */
    LaunchedEffect(lotteryId) {

        val userId = authManager.getCurrentUserId()

        if (userId == null) {

            errorMessage = "Please sign in as a campsite owner."
            loading = false

        } else {

            profileRepo.getProfileById(userId) { profile, _ ->

                if (profile == null) {

                    errorMessage = "Please sign in."
                    loading = false

                } else {

                    ownerId = profile.id

                    lotteryRepo.getLotteryById(lotteryId) { result, error ->

                        if (error != null || result == null) {

                            errorMessage = "Unable to load lottery."

                        } else if (result.campOwnerId != ownerId) {

                            errorMessage = "You do not own this lottery."

                        } else {

                            lottery = result

                            // Load Campsite Period.
                            availabilityStartDate = result.availabilityStartDate
                            availabilityEndDate = result.availabilityEndDate

                            // Load Registration Period.
                            registrationStartDate = result.registrationStartDate
                            registrationEndDate = result.registrationEndDate

                            // Load Booking Period.
                            bookingStartDate = result.bookingStartDate
                            bookingEndDate = result.bookingEndDate

                            // Load winners.
                            winnerText = result.winnerCount.toString()

                            // Load campsite names and locations.
                            campsiteRepo.getAllCampsites { allCampsites, _ ->

                                if (allCampsites != null) {

                                    val selectedCampsites = allCampsites.filter {
                                        it.id in result.campsiteIds
                                    }

                                    campsiteName = selectedCampsites.joinToString(", ") {
                                        it.name
                                    }

                                    campsiteLocation = selectedCampsites.joinToString(", ") {
                                        it.location
                                    }
                                }
                            }
                        }

                        loading = false
                    }
                }
            }
        }
    }

    /**
     * Opens a date picker using the current date as the initial selection.
     *
     * @param date Current date in yyyy-MM-dd format.
     * @param onPick Called with the newly selected date in yyyy-MM-dd format.
     */
    fun openCalendar(
        date: String,
        onPick: (String) -> Unit
    ) {


        val calendar = Calendar.getInstance()

        if (date.isNotEmpty()) {

            try {

                val parts = date.split("-")

                calendar.set(
                    parts[0].toInt(),
                    parts[1].toInt() - 1,
                    parts[2].toInt()
                )

            } catch (e: Exception) {
                // Use today's date.
            }
        }

        DatePickerDialog(
            context,
            { _, year, month, day ->

                val newDate = String.format(
                    Locale.US,
                    "%04d-%02d-%02d",
                    year,
                    month + 1,
                    day
                )

                onPick(newDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    val currentLottery = lottery

    val today = SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.US
    ).format(Date())

    // Check the ORIGINAL registration end date.
    val registrationEnded = if (currentLottery == null) {

        false



    } else {

        currentLottery.registrationEndDate.isEmpty() ||
                today >= currentLottery.registrationEndDate
    }

    /**
     * Displays the screen layout, editable fields, and Confirm button.
     * Confirm checks the entered values, updates a copy of the lottery,
     * and asks the repository to save it to Firebase.
     */
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        // Cancel button.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {

            Button(
                onClick = {
                    if (!backClicked) {
                        backClicked = true
                        navController.popBackStack()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6750A4)
                )
            ) {
                Text("Cancel")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Lottery Info",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (loading) {

            CircularProgressIndicator()

        } else if (errorMessage.isNotEmpty() && currentLottery == null) {

            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error
            )

        } else if (currentLottery != null) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    // Lottery name.
                    Text(
                        text = if (campsiteName.isEmpty()) {
                            "Lottery"
                        } else {
                            "$campsiteName Lottery"
                        },
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(campsiteLocation)

                    Spacer(modifier = Modifier.height(25.dp))

                    if (registrationEnded) {

                        Text(
                            text = "Registration has already ended. " +
                                    "This lottery cannot be edited.",
                            color = MaterialTheme.colorScheme.error
                        )

                    } else {

                        // CAMPSITE PERIOD.
                        Text(
                            text = "Campsite Period",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        EditLotteryDateBoxes(
                            date = availabilityStartDate,
                            onClick = {
                                openCalendar(availabilityStartDate) {
                                    availabilityStartDate = it
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(15.dp))

                        Text("To", fontSize = 18.sp)

                        Spacer(modifier = Modifier.height(10.dp))

                        EditLotteryDateBoxes(
                            date = availabilityEndDate,
                            onClick = {
                                openCalendar(availabilityEndDate) {
                                    availabilityEndDate = it
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(25.dp))

                        // REGISTRATION PERIOD.
                        Text(
                            text = "Registration Period",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        EditLotteryDateBoxes(
                            date = registrationStartDate,
                            onClick = {
                                openCalendar(registrationStartDate) {
                                    registrationStartDate = it
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(15.dp))

                        Text("To", fontSize = 18.sp)

                        Spacer(modifier = Modifier.height(10.dp))

                        EditLotteryDateBoxes(
                            date = registrationEndDate,
                            onClick = {
                                openCalendar(registrationEndDate) {
                                    registrationEndDate = it
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(25.dp))

                        // BOOKING PERIOD.
                        Text(
                            text = "Booking Period",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        EditLotteryDateBoxes(
                            date = bookingStartDate,
                            onClick = {
                                openCalendar(bookingStartDate) {
                                    bookingStartDate = it
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(15.dp))

                        Text("To", fontSize = 18.sp)

                        Spacer(modifier = Modifier.height(10.dp))

                        EditLotteryDateBoxes(
                            date = bookingEndDate,
                            onClick = {
                                openCalendar(bookingEndDate) {
                                    bookingEndDate = it
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(25.dp))

                        // NUMBER OF WINNERS.
                        Text(
                            text = "Number of Winners",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            OutlinedTextField(
                                value = winnerText,
                                onValueChange = { value ->

                                    if (value.all { it.isDigit() }) {
                                        winnerText = value
                                    }
                                },
                                modifier = Modifier.width(110.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                )
                            )

                            // CONFIRM BUTTON.
                            Button(
                                enabled = !saving,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF6750A4)
                                ),
                                onClick = {

                                    val winners = winnerText.toIntOrNull()

                                    // Validate the entered information.
                                    if (
                                        availabilityStartDate.isEmpty() ||
                                        availabilityEndDate.isEmpty()
                                    ) {

                                        errorMessage = "Please select campsite dates."

                                    } else if (availabilityStartDate >= availabilityEndDate) {

                                        errorMessage =
                                            "Campsite end must be after start."

                                    } else if (registrationStartDate >= registrationEndDate) {

                                        errorMessage =
                                            "Registration end must be after start."

                                    } else if (bookingStartDate >= bookingEndDate) {

                                        errorMessage =
                                            "Booking end must be after start."

                                    } else if (bookingStartDate <= registrationEndDate) {

                                        errorMessage =
                                            "Booking must start after registration ends."

                                    } else if (winners == null || winners < 1) {

                                        errorMessage =
                                            "Enter at least one winner."

                                    } else {

                                        errorMessage = ""
                                        saving = true

                                        // Copy the existing lottery.
                                        // Change only the edited fields.
                                        val updatedLottery = currentLottery.copy(
                                            availabilityStartDate = availabilityStartDate,
                                            availabilityEndDate = availabilityEndDate,
                                            registrationStartDate = registrationStartDate,
                                            registrationEndDate = registrationEndDate,
                                            bookingStartDate = bookingStartDate,
                                            bookingEndDate = bookingEndDate,
                                            winnerCount = winners
                                        )

                                        // Save changes to Firebase.
                                        lotteryRepo.updateLottery(
                                            updatedLottery
                                        ) { error ->

                                            saving = false

                                            if (error == null) {

                                                // Open a fresh details screen
                                                // so the new values are loaded.
                                                navController.navigate(
                                                    "ownerLotteryDetails/$lotteryId"
                                                ) {
                                                    popUpTo("ownerlotteries")
                                                }

                                            } else {

                                                errorMessage = if (error.message != null) {
                                                    error.message!!
                                                } else {
                                                    "Unable to update lottery."
                                                }
                                            }
                                        }
                                    }
                                }
                            ) {
                                Text(
                                    if (saving) "Saving..." else "Confirm"
                                )
                            }
                        }

                        // Display any error.
                        if (errorMessage.isNotEmpty()) {

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Displays a date as three clickable boxes in day, month, year order.
 * All three boxes open the same date picker when clicked.
 *
 * @param date Date to display in yyyy-MM-dd format.
 * @param onClick Called when the user taps any date box.
 */
@Composable
private fun EditLotteryDateBoxes(
    date: String,
    onClick: () -> Unit
) {

    val parts = date.split("-")

    val day = if (parts.size == 3) {
        parts[2]
    } else {
        "DD"
    }

    val month = if (parts.size == 3) {
        parts[1]
    } else {
        "MM"
    }

    val year = if (parts.size == 3) {
        parts[0]
    } else {
        "YYYY"
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        listOf(day, month, year).forEach { value ->

            OutlinedButton(
                onClick = onClick,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(4.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White
                )
            ) {
                Text(
                    text = value,
                    color = Color.Black
                )
            }
        }
    }
}
