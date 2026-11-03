
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
import com.example.coders.ui.screens.EntrantHomeScreen
import com.example.coders.ui.screens.OwnerHomeScreen
import com.example.coders.ui.screens.PoliciesScreen
import com.example.coders.ui.screens.EntrantMessagesScreen
import com.example.coders.ui.screens.EntrantChatScreen
import com.example.coders.ui.screens.EntrantReservationsScreen
import com.example.coders.ui.screens.EntrantMyStuffScreen
import com.example.coders.ui.screens.MiniMapScreen


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

                    composable("entrant") {
                        EntrantHomeScreen(navController)
                    }

                    composable("owner") {
                        OwnerHomeScreen()
                    }

                    composable("policies") {
                        PoliciesScreen(navController)
                    }

                    composable("messages") {
                        EntrantMessagesScreen(navController)
                    }

                    composable("messages") {
                        EntrantMessagesScreen(navController)
                    }

                    composable("chat") {
                        EntrantChatScreen(navController)
                    }

                    composable("reservations") {
                        EntrantReservationsScreen(navController)
                    }

                    composable("mystuff") {
                        EntrantMyStuffScreen(navController)
                    }

                    composable("minimap") {
                        MiniMapScreen(navController)
                    }
                }
            }
        }
    }
}
