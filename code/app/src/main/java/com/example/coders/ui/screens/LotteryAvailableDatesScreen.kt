/**
 * Defines the calendar a lottery winner uses to see which dates of the won campsite are open.
 *
 * Opened from a selected lottery on the Waitlists screen or from a pending lottery offer in
 * My Bookings. Each day is shown as available, booked, unavailable, or the entrant's own
 * reservation, and updates live as reservations change.
 *
 * Outstanding issues: None known.
 */

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.coders.AuthManager
import com.example.coders.CampsiteDateStatus
import com.example.coders.CampsiteRepo
import com.example.coders.Lottery
import com.example.coders.LotteryRepo
import com.example.coders.Reservation
import com.example.coders.ReservationRepo
import com.example.coders.availabilityRange
import com.example.coders.availableDates
import com.example.coders.availableDatesError
import com.example.coders.campsiteDateStatus
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

// The following screen was added using Anthropic, Claude Code, "Implement US 01.04.01: show
// lottery winners the available dates for the campsite they won", 2026-11-28.
/**
 * Displays the available, booked, and unavailable dates of a lottery's campsite to its winner.
 *
 * Entrants who did not win see an explanation instead of the calendar. Reservation details of
 * other entrants are never shown, only whether a date is booked.
 *
 * @param navController Used to go back.
 * @param lotteryId The lottery the entrant won.
 */
@Composable
fun LotteryAvailableDatesScreen(navController: NavController, lotteryId: String) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context.applicationContext) }
    val lotteryRepo = remember { LotteryRepo() }
    val campsiteRepo = remember { CampsiteRepo() }
    val reservationRepo = remember { ReservationRepo() }

    // null while the entrant ID or the lottery are still loading
    var entrantId by remember { mutableStateOf<String?>(null) }
    var lottery by remember { mutableStateOf<Lottery?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var campsiteNames by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var selectedCampsiteId by remember { mutableStateOf<String?>(null) }
    var reservations by remember { mutableStateOf<List<Reservation>>(emptyList()) }
    var shownMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    LaunchedEffect(Unit) {
        entrantId = authManager.getCurrentUserId().orEmpty()
    }

    DisposableEffect(lotteryId) {
        var disposed = false
        lotteryRepo.getLotteryById(lotteryId) { loaded, error ->
            if (disposed) return@getLotteryById
            when {
                error != null -> loadError = error.message ?: "Unable to load this lottery."
                loaded == null -> loadError = "Lottery not found."
                else -> {
                    lottery = loaded
                    selectedCampsiteId = loaded.campsiteIds.firstOrNull()
                    loaded.availabilityRange()?.let { shownMonth = YearMonth.from(it.start) }
                }
            }
        }
        onDispose { disposed = true }
    }

    DisposableEffect(campsiteRepo) {
        onDispose { campsiteRepo.close() }
    }

    val campsiteIds = lottery?.campsiteIds.orEmpty()
    LaunchedEffect(campsiteIds) {
        campsiteIds.filterNot { it in campsiteNames }.forEach { campsiteId ->
            campsiteRepo.getCampsiteById(campsiteId) { campsite, _ ->
                if (campsite != null) {
                    campsiteNames = campsiteNames + (campsiteId to campsite.name)
                }
            }
        }
    }


    DisposableEffect(selectedCampsiteId) {
        reservations = emptyList()
        val listener = selectedCampsiteId?.let { campsiteId ->
            reservationRepo.listenForCampsiteReservations(
                campsiteId = campsiteId,
                onUpdate = { updated -> reservations = updated },
                onError = { error ->
                    loadError = error.message ?: "Unable to load reservations."
                }
            )
        }
        onDispose { listener?.remove() }
    }

    val id = entrantId.orEmpty()
    val currentLottery = lottery
    val campsiteId = selectedCampsiteId

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        TextButton(onClick = { navController.popBackStack() }) {
            Text("Back")
        }

        Text(
            text = "Available Dates",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Dates you can reserve at the campsite you won.")

        Spacer(modifier = Modifier.height(20.dp))

        val accessError = currentLottery?.availableDatesError(id)
        when {
            loadError != null -> {
                Text(loadError.orEmpty(), color = MaterialTheme.colorScheme.error)
            }

            entrantId == null || currentLottery == null -> {
                CircularProgressIndicator()
            }

            accessError != null -> {
                Text(accessError, color = MaterialTheme.colorScheme.error)
            }

            campsiteId == null -> {
                Text("This lottery has no campsites.", color = MaterialTheme.colorScheme.error)
            }

            else -> {
                //  lotteries w more than one owner can award more than one campsite.
                if (currentLottery.campsiteIds.size > 1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        currentLottery.campsiteIds.forEach { optionId ->
                            val selected = optionId == campsiteId
                            Button(
                                onClick = {
                                    selectedCampsiteId = optionId
                                    selectedDate = null
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.secondaryContainer
                                    },
                                    contentColor = if (selected) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    }
                                )
                            ) {
                                Text(campsiteNames[optionId] ?: "Campsite")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    text = campsiteNames[campsiteId] ?: "Loading...",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                val range = currentLottery.availabilityRange()
                val openDates = currentLottery.availableDates(campsiteId, reservations, id)
                val dayNoun = if (openDates.size == 1) "date" else "dates"
                Text(
                    text = "Available: ${openDates.size} $dayNoun between " +
                        "${range?.start} and ${range?.endInclusive}",
                    modifier = Modifier.testTag("availableDatesSummary")
                )

                Spacer(modifier = Modifier.height(16.dp))

                AvailabilityCalendar(
                    month = shownMonth,
                    onPreviousMonth = {
                        shownMonth = shownMonth.minusMonths(1)
                        selectedDate = null
                    },
                    onNextMonth = {
                        shownMonth = shownMonth.plusMonths(1)
                        selectedDate = null
                    },
                    statusFor = { date ->
                        currentLottery.campsiteDateStatus(date, campsiteId, reservations, id)
                    },
                    onDateClick = { selectedDate = it }
                )

                Text("Green = Available")
                Text("Red = Booked")
                Text("Grey = Unavailable")
                Text("Blue = Your reservation")

                selectedDate?.let { date ->
                    val status = currentLottery.campsiteDateStatus(date, campsiteId, reservations, id)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Selected Date: $date", fontWeight = FontWeight.Bold)
                    Text("Status: ${status.label}", color = statusTextColor(status))
                }
            }
        }
    }
}

/**
 * Displays one month with each day coloured and described by its [CampsiteDateStatus].
 *
 * @param month The month being shown.
 * @param onPreviousMonth Moves the calendar back one month.
 * @param onNextMonth Moves the calendar forward one month.
 * @param statusFor Returns how a day should be shown.
 * @param onDateClick Called when a day is tapped.
 */
@Composable
private fun AvailabilityCalendar(
    month: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    statusFor: (LocalDate) -> CampsiteDateStatus,
    onDateClick: (LocalDate) -> Unit
) {
    val monthName = "${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.year}"
    // matching the owners booked dates calendar.
    val emptyDays = month.atDay(1).dayOfWeek.value % 7

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(onClick = onPreviousMonth) { Text("<") }
        Text(text = monthName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Button(onClick = onNextMonth) { Text(">") }
    }

    Spacer(modifier = Modifier.height(16.dp))

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.height(330.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
            item { Text(day) }
        }

        items(emptyDays) {
            Spacer(modifier = Modifier.size(40.dp))
        }

        items(month.lengthOfMonth()) { index ->
            val date = month.atDay(index + 1)
            val status = statusFor(date)
            val isoDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE)

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(dayBackgroundColor(status), RoundedCornerShape(8.dp))
                    .clickable { onDateClick(date) }
                    .testTag("day_$isoDate")
                    .semantics { contentDescription = "$isoDate ${status.label}" },
                contentAlignment = Alignment.Center
            ) {
                Text(text = (index + 1).toString(), color = Color.Black)
            }
        }
    }
}

/** Background colour of a calendar day for [status]. */
private fun dayBackgroundColor(status: CampsiteDateStatus): Color = when (status) {
    CampsiteDateStatus.Available -> Color(0xFFC8E6C9)
    CampsiteDateStatus.Booked -> Color(0xFFFFCDD2)
    CampsiteDateStatus.Unavailable -> Color(0xFFE0E0E0)
    CampsiteDateStatus.Yours -> Color(0xFFBBDEFB)
}

/** Text colour used when describing a selected day's [status]. */
private fun statusTextColor(status: CampsiteDateStatus): Color = when (status) {
    CampsiteDateStatus.Available -> Color(0xFF2E7D32)
    CampsiteDateStatus.Booked -> Color.Red
    CampsiteDateStatus.Unavailable -> Color.DarkGray
    CampsiteDateStatus.Yours -> Color(0xFF1565C0)
}
