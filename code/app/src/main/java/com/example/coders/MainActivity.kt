/**
 * Main entry point for the application.
 *
 * Initializes repositories
 * used throughout the app. Restores the locally saved user session and directs
 * users to the appropriate screen based on their profile role.
 *
 * Outstanding issues: None known.
 */

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
import com.example.coders.ui.screens.ProfileScreen
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
import com.example.coders.ui.screens.AdminSearchScreen
import com.example.coders.ui.screens.EntrantSearchScreen
import com.example.coders.ui.screens.CampsiteDetailsScreen
import com.example.coders.ui.theme.CodersTheme
import com.example.coders.CampOwner
import com.example.coders.ui.screens.CreateCampsiteScreen
import com.example.coders.ui.screens.EditCampsiteScreen
import com.example.coders.ui.screens.EntrantPastReservationsScreen
import com.example.coders.ui.screens.RateCampsiteScreen
import com.example.coders.ui.screens.CampsiteRatingsScreen
import com.example.coders.ui.screens.CampsiteReviewsScreen

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.coders.ui.screens.ReviewCampsiteScreen

/** Starts the app by restoring the profile associated with the locally saved ID. */
class MainActivity : ComponentActivity() {

    /**
     * Initializes the application's UI and navigation system.
     *
     * Sets up the app theme, navigation controller, and repositories.
     * Restores the user's saved session and navigates to the appropriate
     * screen based on their profile role.
     *
     * @param savedInstanceState Previously saved activity state if needed
     */
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
                    composable("profile") {
                        ProfileScreen(navController)
                    }

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
                                        val destination = if(profile != null) {
                                            UserSession.initializeView(profile)
                                            "home"
                                        }
                                        else{
                                            "signup"
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

                    composable("home") {
                        HomeScreen(navController)
                    }

                    composable("mystuff") {
                        MyStuffScreen(navController)
                    }

                    // The following block was modified using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
                    composable("ownercampsites") { backStackEntry ->
                        OwnerCampsitesScreen(
                            navController = navController,
                            campsiteRepo = campsiteRepo,
                            savedStateHandle = backStackEntry.savedStateHandle
                        )
                    }
                    composable("create_campsite") {
                        CreateCampsiteScreen(
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

                    //routed the rate campsite screen
                    composable(
                        route = "rate_campsite/{reservationId}",
                        arguments = listOf(
                            navArgument("reservationId") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        RateCampsiteScreen(
                            navController = navController,
                            reservationId = backStackEntry.arguments
                                ?.getString("reservationId")
                                .orEmpty()
                        )
                    }

                    //routed the rate campsite screen
                    composable(
                        route = "review_campsite/{reservationId}",
                        arguments = listOf(
                            navArgument("reservationId") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        ReviewCampsiteScreen(
                            navController = navController,
                            reservationId = backStackEntry.arguments
                                ?.getString("reservationId")
                                .orEmpty()
                        )
                    }

                    //routed the campsite ratings screen
                    composable(
                        route = "campsite_ratings/{campsiteId}",
                        arguments = listOf(
                            navArgument("campsiteId") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        CampsiteRatingsScreen(
                            navController = navController,
                            campsiteId = backStackEntry.arguments
                                ?.getString("campsiteId")
                                .orEmpty()
                        )
                    }

                    //routed the campsite reviews screen
                    composable(
                        route = "campsite_reviews/{campsiteId}",
                        arguments = listOf(
                            navArgument("campsiteId") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        CampsiteReviewsScreen(
                            navController = navController,
                            campsiteId = backStackEntry.arguments
                                ?.getString("campsiteId")
                                .orEmpty()
                        )
                    }

                    composable("entrantpastreservations") {
                        EntrantPastReservationsScreen(navController)
                    }

                    composable("ownerreservations") {
                        OwnerReservationsScreen(navController)
                    }

                    composable("minimap") {
                        MiniMapScreen(navController)
                    }

                    // Search page
                    composable("entrantsearch") {
                        EntrantSearchScreen(navController)
                    }

                    // Search page
                    composable("adminsearch") {
                        AdminSearchScreen(navController)
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
                    composable(
                        route = "edit_campsite/{campsiteId}",
                        arguments = listOf(
                            navArgument("campsiteId") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val campsiteId = backStackEntry.arguments?.getString("campsiteId").orEmpty()

                        val campsite = campsiteRepo
                            .getAllCampsites()
                            .firstOrNull { it.id == campsiteId }

                        if (campsite != null) {
                            EditCampsiteScreen(
                                navController = navController,
                                campsite = campsite,
                                campsiteRepo = campsiteRepo
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
