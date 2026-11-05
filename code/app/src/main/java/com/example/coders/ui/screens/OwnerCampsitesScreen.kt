package com.example.coders.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.coders.CampOwner
import com.example.coders.Campsite
import com.example.coders.CampsiteRepo

/**
 * Displays a list of campsites
 *
 */
@Composable
fun OwnerCampsitesScreen(
    navController: NavController,
    campOwner: CampOwner? = null,
    campsiteRepo: CampsiteRepo? = null
) {

    val campsites = campsiteRepo?.getAllCampsites() ?: emptyList()

    var campsiteToDelete by remember { mutableStateOf<Campsite?>(null) }
    var isNavigating by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            AppBottomBar(
                navController = navController
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 2.dp)
        ) {
            IconButton(
                onClick = {
                    if(!isNavigating) {
                        navController.popBackStack()
                }
                }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            Text(
                text = "My Campsites:",
                fontSize = 21.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(2.dp))

            campsites.forEach { campsite ->
                OwnerCampsiteCard(
                    campsite = campsite,
                    onDetailsClick = { navController.navigate("campsiteDetails/${campsite.id}") },
                    onDeleteClick = { campsiteToDelete = campsite }
                )
                Spacer(modifier = Modifier.height(25.dp))
            }

            Spacer(modifier = Modifier.height(40.dp))

            /*Button(
                onClick = { navController.navigate("create_campsite") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                shape = RoundedCornerShape(0.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create", modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(18.dp))
                Text("Create Campsite", fontSize = 11.sp, fontWeight = FontWeight.Normal)
            }*/
        }

        campsiteToDelete?.let { target ->
            AlertDialog(
                onDismissRequest = { campsiteToDelete = null },
                title = { Text("Delete ${target.name}?", fontSize = 12.sp) },
                text = { Text("Are you sure? cant undo.", fontSize = 10.sp) },
                confirmButton = {
                    TextButton(onClick = {
                        campsiteRepo?.deleteCampsite(target)
                        campsiteToDelete = null
                    }) { Text("Delete", fontSize = 10.sp) }
                },
                dismissButton = {
                    TextButton(onClick = { campsiteToDelete = null }) { Text("Nevermind", fontSize = 10.sp) }
                }
            )
        }
    }
}

/**
 * Renders a summary card for a campsite.
 */
@Composable
fun OwnerCampsiteCard(
    campsite: Campsite,
    onDetailsClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDetailsClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .clip(RoundedCornerShape(0.dp))
                    .background(MaterialTheme.colorScheme.error),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Landscape,
                    contentDescription = "Campsite Image",
                    modifier = Modifier.size(15.dp),
                    tint = MaterialTheme.colorScheme.onError
                )
            }

            Spacer(modifier = Modifier.width(2.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = campsite.name, fontSize = 22.sp)
                Text(text = campsite.location, fontSize = 10.sp)
                Text(text = "${campsite.capacity} guests", fontSize = 14.sp)
            }

            IconButton(onClick = onDeleteClick, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Default.Remove, contentDescription = "Delete")
            }
        }
    }
}