package com.example.coders

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.coders.ui.theme.CodersTheme
import com.example.coders.ui.screens.SignUpScreen
import com.example.coders.ui.screens.AdminHomeScreen
import com.example.coders.ui.screens.HomeScreen
import com.example.coders.ui.screens.PoliciesScreen
import com.example.coders.ui.screens.MessagesScreen
import com.example.coders.ui.screens.ChatScreen
import com.example.coders.ui.screens.EntrantReservationsScreen
import com.example.coders.ui.screens.MyStuffScreen
import com.example.coders.ui.screens.MiniMapScreen
import com.example.coders.ui.screens.OwnerCampsitesScreen
import com.example.coders.ui.screens.OwnerReservationsScreen
import com.example.coders.ui.screens.EntrantSearchScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CodersTheme {

                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "signup"
                ) {
                    composable("signup") {
                        SignUpScreen(navController)
                    }

                    composable("admin") {
                        AdminHomeScreen()
                    }

                    composable("home") {
                        HomeScreen(navController)
                    }

                    composable("mystuff") {
                        MyStuffScreen(navController)
                    }

                    composable("ownercampsites") {
                        OwnerCampsitesScreen(navController)
                    }

                    composable("policies") {
                        PoliciesScreen(navController)
                    }

                    composable("messages") {
                        MessagesScreen(navController)
                    }

                    composable("chat") {
                        ChatScreen(navController)
                    }

                    composable("entrantreservations") {
                        EntrantReservationsScreen(navController)
                    }

                    composable("ownerreservations") {
                        OwnerReservationsScreen(navController)
                    }

                    composable("minimap") {
                        MiniMapScreen(navController)
                    }

                    // Campsite Search
                    composable("search") {
                        EntrantSearchScreen(navController)
                    }
                }
            }
        }
    }
}
