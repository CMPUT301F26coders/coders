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

@Composable
fun OwnerHomeScreen() {
    Scaffold(
        bottomBar = {
            OwnerBottomBar()
        }
    ) { innerPadding ->
        Text(
            text = "Campsite Owner Home",
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun OwnerBottomBar() {
    NavigationBar {
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = {
                Icon(Icons.Default.Home, contentDescription = "Home")
            }
        )

        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = {
                Icon(Icons.Default.Email, contentDescription = "Messages")
            }
        )

        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = {
                Icon(Icons.Default.Inventory2, contentDescription = "My Stuff")
            }
        )

        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
        )

        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = {
                Icon(Icons.Default.Person, contentDescription = "Profile")
            }
        )
    }
}
