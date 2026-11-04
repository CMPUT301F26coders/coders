
package com.example.coders.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.coders.UserSession

@Composable
fun AppBottomBar(navController: NavController) {

    val isOwner = UserSession.isOwner

    val homeRoute = if (isOwner) "owner" else "entrant"
    val myStuffRoute = if (isOwner) "ownermystuff" else "entrantmystuff"

    fun navigateTo(route: String) {
        navController.navigate(route) {
            popUpTo(homeRoute) {
                inclusive = false
            }
            launchSingleTop = true
        }
    }

    NavigationBar {

        NavigationBarItem(
            selected = false,
            onClick = { navigateTo(homeRoute) },
            icon = {
                Icon(Icons.Default.Home, contentDescription = "Home")
            }
        )

        NavigationBarItem(
            selected = false,
            onClick = { navigateTo("messages") },
            icon = {
                Icon(Icons.Default.Email, contentDescription = "Messages")
            }
        )

        NavigationBarItem(
            selected = false,
            onClick = { navigateTo(myStuffRoute) },
            icon = {
                Icon(Icons.Default.Inventory2, contentDescription = "My Stuff")
            }
        )

        if (!isOwner) {
            NavigationBarItem(
                selected = false,
                onClick = { navigateTo("entrantsearch") },
                icon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            )
        }

        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = {
                Icon(Icons.Default.Person, contentDescription = "Profile")
            }
        )
    }
}
