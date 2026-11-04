package com.example.coders

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.coders.ui.screens.AdminHomeScreen
import com.example.coders.ui.screens.ChatScreen
import com.example.coders.ui.screens.EntrantReservationsScreen
import com.example.coders.ui.screens.HomeScreen
import com.example.coders.ui.screens.MessagesScreen
import com.example.coders.ui.screens.MiniMapScreen
import com.example.coders.ui.screens.MyStuffScreen
import com.example.coders.ui.screens.OwnerCampsitesScreen
import com.example.coders.ui.screens.PoliciesScreen
import com.example.coders.ui.screens.SignUpScreen
import com.example.coders.ui.theme.CodersTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.coders.ui.screens.OwnerReservationsScreen

/** Starts the app by restoring the profile associated with the locally saved ID. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CodersTheme {
                val navController = rememberNavController()
                val authManager = remember { AuthManager(applicationContext) }
                val profileRepo = remember { ProfileRepo() }
                val mainScope = rememberCoroutineScope()

                NavHost(
                    navController = navController,
                    startDestination = "startup"
                ) {

                    composable("startup") {
                        LaunchedEffect(Unit) {
                            val savedProfileId = authManager.getCurrentUserId()
                            if (savedProfileId.isNullOrBlank()) {
                                mainScope.launch(Dispatchers.Main.immediate) {
                                    navController.replaceStartupWith("signup")
                                }
                            } else {
                                profileRepo.getProfileById(savedProfileId) { profile, _ ->
                                    mainScope.launch(Dispatchers.Main.immediate) {
                                        val destination = when (profile) {
                                            is Admin -> {
                                                UserSession.isOwner = false
                                                "admin"
                                            }
                                            is CampOwner -> {
                                                UserSession.isOwner = true
                                                "home"
                                            }
                                            is Entrant -> {
                                                UserSession.isOwner = false
                                                "home"
                                            }
                                            else -> "signup"
                                        }
                                        navController.replaceStartupWith(destination)
                                    }
                                }
                            }
                        }

                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
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
                    composable("reservations") {
                        EntrantReservationsScreen(navController)
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
                }
            }
        }
    }
}

/**
 * Replaces the temporary startup destination with the resolved route.
 *
 * @param route The route to navigate to.
 * */
private fun NavHostController.replaceStartupWith(route: String) {
    navigate(route) {
        popUpTo("startup") { inclusive = true }
        launchSingleTop = true
    }
}
