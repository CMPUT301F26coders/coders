
package com.example.coders.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun OwnerHomeScreen(navController: NavController) {
    Scaffold(
        bottomBar = {
            AppBottomBar(
                navController = navController
            )
        }
    ) { innerPadding ->
        Text(
            text = "Campsite Owner Home",
            modifier = Modifier.padding(innerPadding)
        )
    }
}

