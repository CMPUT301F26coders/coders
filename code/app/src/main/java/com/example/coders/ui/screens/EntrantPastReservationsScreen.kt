package com.example.coders.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.coders.Campsite
import com.example.coders.CampsiteRepo
import com.example.coders.Entrant
import com.example.coders.Reservation
import com.example.coders.ReservationStatus
import com.example.coders.SignedInProfile


//The code for filtering past reservations is from Google, Gemini, "How can I make sure this code only displays past reservations and not all reservations"
@Composable
fun EntrantPastReservationsScreen(
    navController: NavController,
    testEntrant: Entrant? = null
) {
    val profile by SignedInProfile().rememberSignedInProfile()
    val entrant = testEntrant ?: (profile as? Entrant)
    val pastReservations = entrant?.reservations.orEmpty().filter {
        it.status == ReservationStatus.Past
    }
    val campsiteRepo = remember { CampsiteRepo() }
    var campsites by remember { mutableStateOf<Map<String, Campsite>>(emptyMap()) }

    DisposableEffect(pastReservations, campsiteRepo) {
        var disposed = false
        campsites = emptyMap()

        pastReservations.map { it.campsiteId }
            .filter { it.isNotBlank() }
            .distinct()
            .forEach { campsiteId ->
                campsiteRepo.getCampsiteById(campsiteId) { campsite, _ ->
                    if (!disposed && campsite != null) {
                        campsites = campsites + (campsiteId to campsite)
                    }
                }
            }

        onDispose { disposed = true }
    }

    DisposableEffect(campsiteRepo) {
        onDispose { campsiteRepo.close() }
    }

    val context = LocalContext.current
    var reservationToExport by remember { mutableStateOf<Reservation?>(null) }
    val csvExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        val reservation = reservationToExport
        reservationToExport = null
        if (uri != null && reservation != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use {
                    it.write(reservation.asCsv(campsites[reservation.campsiteId]))
                } ?: error("Unable to open the selected file.")
            }.onSuccess {
                Toast.makeText(context, "Reservation CSV exported", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(context, "Unable to export reservation CSV", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        bottomBar = { AppBottomBar(navController = navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "My Bookings:",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Past Reservations:",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))


            if (pastReservations.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(top = 40.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text("No past reservations found.", color = Color.Gray)
                }
            } else {
                pastReservations.forEach { reservation ->
                    PastReservationCard(
                        reservation = reservation,
                        campsite = campsites[reservation.campsiteId],
                        onRate = {
                            navController.navigate("rate_campsite/${Uri.encode(reservation.id)}")
                        },
                        onExportCsv = {
                            reservationToExport = reservation
                            csvExportLauncher.launch("reservation-${reservation.id}.csv")
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Current Reservations", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PastReservationCard(
    reservation: Reservation,
    campsite: Campsite?,
    onRate: () -> Unit,
    onExportCsv: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Landscape, contentDescription = "Image", tint = Color.White)
            }

            Spacer(modifier = Modifier.width(12.dp))


            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = campsite?.name ?: "Loading campsite…",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = campsite?.location ?: "",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
                Text(text = "${reservation.guestNumber} guests", fontSize = 12.sp, color = Color.DarkGray)
            }

            Column(horizontalAlignment = Alignment.End) {
                Button(
                    onClick = onRate,
                    modifier = Modifier.height(28.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text("Rate", fontSize = 10.sp, color = Color.White)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onExportCsv,
                    modifier = Modifier.height(28.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = "CSV", modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("CSV", fontSize = 10.sp)
                }
            }
        }
    }
}

private fun Reservation.asCsv(campsite: Campsite?): String = buildString {
    appendLine("reservation_id,campsite_id,campsite_name,location,dates,guests,status,rating")
    appendCsvValue(id)
    append(',')
    appendCsvValue(campsiteId)
    append(',')
    appendCsvValue(campsite?.name.orEmpty())
    append(',')
    appendCsvValue(campsite?.location.orEmpty())
    append(',')
    appendCsvValue(dates)
    append(',')
    append(guestNumber)
    append(',')
    appendCsvValue(status.name)
    append(',')
    appendCsvValue(rating?.toString().orEmpty())
    appendLine()
}

private fun StringBuilder.appendCsvValue(value: String) {
    append('"')
    append(value.replace("\"", "\"\""))
    append('"')
}
