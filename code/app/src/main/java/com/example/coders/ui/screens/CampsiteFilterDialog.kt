package com.example.coders.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.time.Instant
import java.time.ZoneOffset


/**
 * Displays dialog that allows users to apply filters for campsite searches
 *
 * filter by location, check-in/check-out dates,
 * and number of guests.
 * also has validation to ensure checkout date is after checkin date
 *
 * @param initialLocation  current location filter string
 * @param initialStartDate current start date filter string
 * @param initialEndDate current end date filter string
 * @param initialGuests current guest count filter
 * @param onDismiss triggered when the user closes the dialog without applying
 * @param onApply triggered with the selected filter values when applied.
 * @param onClear triggered when the user chooses to reset all filters
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampsiteFilterDialog(
    initialLocation: String = "",
    initialStartDate: String = "",
    initialEndDate: String = "",
    initialGuests: Int = 0,
    onDismiss: () -> Unit,
    onApply: (String, String, String, Int) -> Unit,
    onClear: () -> Unit
) {
    var location by remember { mutableStateOf(initialLocation) }
    var startDate by remember { mutableStateOf(initialStartDate) }
    var endDate by remember { mutableStateOf(initialEndDate) }
    var guests by remember { mutableIntStateOf(initialGuests) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val invalidDateRange = startDate.isNotBlank() &&
            endDate.isNotBlank() &&
            endDate <= startDate

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Filter Campsites",
                    style = MaterialTheme.typography.titleLarge
                )

                // LOCATION
                Text(
                    text = "Location:",
                    style = MaterialTheme.typography.titleMedium
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    placeholder = {
                        Text("Enter city or location")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // CHECK-IN DATE
                Text(
                    text = "Check-in Date:",
                    style = MaterialTheme.typography.titleMedium
                )

                OutlinedButton(
                    onClick = { showStartDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (startDate.isBlank()) {
                            "Select start date"
                        } else {
                            startDate
                        }
                    )
                }

                // CHECK-OUT DATE
                Text(
                    text = "Check-out Date:",
                    style = MaterialTheme.typography.titleMedium
                )

                OutlinedButton(
                    onClick = { showEndDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (endDate.isBlank()) {
                            "Select end date"
                        } else {
                            endDate
                        }
                    )
                }

                if (invalidDateRange) {
                    Text(
                        text = "Check-out must be after check-in.",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // GUESTS
                Text(
                    text = "Number of Guests:",
                    style = MaterialTheme.typography.titleMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (guests == 0) {
                            "Any number of guests"
                        } else {
                            "Guests: $guests"
                        }
                    )

                    Row {
                        OutlinedButton(
                            onClick = {
                                if (guests > 0) guests--
                            },
                            contentPadding = PaddingValues(
                                horizontal = 12.dp
                            )
                        ) {
                            Text("−")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedButton(
                            onClick = { guests++ },
                            contentPadding = PaddingValues(
                                horizontal = 12.dp
                            )
                        ) {
                            Text("+")
                        }
                    }
                }

                // APPLY
                Button(
                    onClick = {
                        onApply(
                            location.trim(),
                            startDate,
                            endDate,
                            guests
                        )
                    },
                    enabled = !invalidDateRange,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apply Filter")
                }

                // CLEAR
                TextButton(
                    onClick = onClear,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Clear All Filters")
                }
            }
        }
    }

    // START DATE PICKER
    if (showStartDatePicker) {
        val pickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = {
                showStartDatePicker = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            startDate = Instant
                                .ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                                .toString()

                            if (
                                endDate.isNotBlank() &&
                                endDate <= startDate
                            ) {
                                endDate = ""
                            }
                        }

                        showStartDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showStartDatePicker = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    // END DATE PICKER
    if (showEndDatePicker) {
        val pickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = {
                showEndDatePicker = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            endDate = Instant
                                .ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                                .toString()
                        }

                        showEndDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEndDatePicker = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}
