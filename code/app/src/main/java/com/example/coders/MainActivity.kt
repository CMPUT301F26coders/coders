package com.example.coders

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
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
import com.example.coders.ui.screens.OwnerReservationsScreen
import com.example.coders.ui.screens.PoliciesScreen
import com.example.coders.ui.screens.SignUpScreen
import com.example.coders.ui.screens.EntrantSearchScreen
import com.example.coders.ui.screens.CampsiteDetailsScreen
import com.example.coders.ui.theme.CodersTheme

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Starts the app by restoring the profile associated with the locally saved ID. */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CodersTheme {

                val navController = rememberNavController()
                val authManager = remember {
                    AuthManager(applicationContext)
                }
                val campsiteRepo = remember { CampsiteRepo() }
                val profileRepo = remember { ProfileRepo() }
                val mainScope = rememberCoroutineScope()

                NavHost(
                    navController = navController,
                    startDestination = "startup"
                ) {

                    composable("startup") {
                        LaunchedEffect(Unit) {
                            val savedProfileId =
                                authManager.getCurrentUserId()

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
                        OwnerCampsitesScreen(
                            navController = navController,
                            campsiteRepo = campsiteRepo
                        )
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

                    // Search page
                    composable("search") {
                        EntrantSearchScreen(navController)
                    }

                    // Campsite Details page
                    composable(
                        route = "campsiteDetails/{campsiteId}",
                        arguments = listOf(
                            navArgument("campsiteId") {
                                type = NavType.StringType
                            }
                        )
                    ) { backStackEntry ->

                        val campsiteId = backStackEntry.arguments
                            ?.getString("campsiteId")
                            .orEmpty()


                        val campsite = campsiteRepo
                            .getAllCampsites()
                            .firstOrNull { it.id == campsiteId }

                        if (campsite != null) {
                            CampsiteDetailsScreen(
                                navController = navController,
                                campsite = campsite
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Campsite not found")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Replaces the temporary startup destination with the resolved route.
 */
private fun NavHostController.replaceStartupWith(route: String) {
    navigate(route) {
        popUpTo("startup") {
            inclusive = true
        }
        launchSingleTop = true
    }
}
