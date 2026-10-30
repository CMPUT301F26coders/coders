
package com.example.coders.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.coders.R

@Composable
fun MiniMapScreen(navController: NavController) {

    Scaffold(
        bottomBar = {
            EntrantBottomBar(navController)
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {

            Button(
                onClick = {
                    navController.popBackStack()
                }
            ) {
                Text("Back")
            }

            Text(
                text = "Pine Lake Campground Map",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Image(
                painter = painterResource(R.drawable.pine_lake_map),
                contentDescription = "Pine Lake Campground Map",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentScale = ContentScale.Fit
            )
        }
    }
}
