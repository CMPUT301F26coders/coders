
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
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
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
import com.example.coders.ui.screens.OwnerSentNotificationsScreen
import com.example.coders.ui.screens.OwnerLotteriesScreen
import com.example.coders.ui.screens.OwnerLotteryDetailsScreen
import com.example.coders.ui.screens.OwnerLotteryWaitlistScreen
import com.example.coders.ui.screens.PoliciesScreen
import com.example.coders.ui.screens.SignUpScreen
import com.example.coders.ui.screens.AdminSearchScreen
import com.example.coders.ui.screens.EntrantSearchScreen
import com.example.coders.ui.screens.CampsiteDetailsScreen
import com.example.coders.ui.theme.CodersTheme
import com.example.coders.LotteryRepo
import com.example.coders.ui.screens.CreateCampsiteScreen
import com.example.coders.ui.screens.EditCampsiteScreen
import com.example.coders.ui.screens.EntrantPastReservationsScreen
import com.example.coders.ui.screens.RateCampsiteScreen
import com.example.coders.ui.screens.CampsiteRatingsScreen
import com.example.coders.ui.screens.CampsiteReviewsScreen
import com.example.coders.ui.screens.ReservationMapScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.coders.ui.screens.ReviewCampsiteScreen
import com.example.coders.ui.screens.CampsiteMemoriesMapScreen

// Lottery screen imports.
import com.example.coders.ui.screens.OwnerLotteriesScreen
import com.example.coders.ui.screens.OwnerLotteryDetailsScreen
import com.example.coders.ui.screens.MultiOwnerLotteryScreen
import com.example.coders.ui.screens.OwnerLotteryWaitlistScreen
import com.example.coders.ui.screens.EditLotteryScreen
import com.example.coders.ui.screens.ViewBookedDatesScreen

//Qr Code imports:
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.navDeepLink

import com.example.coders.ui.screens.AdminNotificationLogs
import com.example.coders.ui.screens.AdminMessageLogs

/** Starts the app by restoring the profile associated with the locally saved ID. */
class MainActivity : ComponentActivity() {

    //So that a qr code navigates to the right thing even if the app is already open
    private var appNavController: NavHostController? = null

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        appNavController?.handleDeepLink(intent)
    }
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
                appNavController = navController
                val authManager = remember {
                    AuthManager(applicationContext)
                }
                val campsiteRepo = remember { CampsiteRepo() }
                val profileRepo = remember { ProfileRepo() }
                val lotteryRepo = remember { LotteryRepo() }
                val mainScope = rememberCoroutineScope()

                NavHost(
                    navController = navController,
                    startDestination = "startup"
                ) {
                    composable("profile") {
                        ProfileScreen(navController)
                    }
                    composable("campsite_memories") {
                        CampsiteMemoriesMapScreen(navController)
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

                    // OWNER LOTTERY SCREENS

                    /**
                     * Displays all lotteries belonging to the
                     * signed-in campsite owner.
                     */
                    composable("ownerlotteries") {
                        OwnerLotteriesScreen(navController)
                    }

                    composable("ownerLotteries") {
                        OwnerLotteriesScreen(navController)
                    }

                    /**
                     * Displays the details of a selected lottery.
                     */
                    composable(
                        route = "ownerLotteryDetails/{lotteryId}",
                        arguments = listOf(
                            navArgument("lotteryId") {
                                type = NavType.StringType
                            }
                        )
                    ) { backStackEntry ->

                        val lotteryId = backStackEntry.arguments
                            ?.getString("lotteryId")
                            .orEmpty()

                        OwnerLotteryDetailsScreen(
                            navController = navController,
                            lotteryId = lotteryId
                        )
                    }

                    /**
                     * Displays the lottery creation screen.
                     *
                     * Loads campsites belonging to the signed-in owner
                     * and saves the lottery information to Firebase.
                     */
                    composable("multiOwnerLottery") {

                        var ownerId by remember {
                            mutableStateOf("")
                        }

                        var campsites by remember {
                            mutableStateOf<List<Campsite>>(emptyList())
                        }

                        var loading by remember {
                            mutableStateOf(true)
                        }

                        var errorMessage by remember {
                            mutableStateOf("")
                        }

                        LaunchedEffect(Unit) {

                            val id = authManager.getCurrentUserId()

                            if (id.isNullOrBlank()) {

                                errorMessage =
                                    "Please sign in."

                                loading = false

                            } else {

                                profileRepo.getProfileById(id) { profile, _ ->

                                    if (profile != null) {

                                        ownerId = profile.id

                                    } else {

                                        errorMessage =
                                            "Please sign in."

                                        loading = false
                                    }
                                }
                            }
                        }

                        DisposableEffect(ownerId) {

                            val listener = if (ownerId.isNotEmpty()) {

                                campsiteRepo.listenForCampsites(
                                    onUpdate = { updatedCampsites ->

                                        campsites = updatedCampsites.filter {
                                            it.campOwnerId == ownerId
                                        }

                                        loading = false
                                    }
                                )

                            } else {
                                null
                            }

                            onDispose {
                                if (listener != null) {
                                    listener.remove()
                                }
                            }
                        }

                        if (loading) {

                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }

                        } else if (errorMessage.isNotEmpty()) {

                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(errorMessage)
                            }

                        } else {

                            MultiOwnerLotteryScreen(
                                navController = navController,
                                campsites = campsites,
                                currentOwnerId = ownerId,
                                onCreateLottery = {
                                        campsiteIds,
                                        invitedOwnerIds,
                                        availabilityStartDate,
                                        availabilityEndDate,
                                        registrationStartDate,
                                        registrationEndDate,
                                        bookingStartDate,
                                        bookingEndDate,
                                        winnerCount,
                                        capacity ->

                                    val lottery = Lottery(
                                        campOwnerId = ownerId,
                                        campsiteIds = campsiteIds,
                                        availabilityStartDate = availabilityStartDate,
                                        availabilityEndDate = availabilityEndDate,
                                        registrationStartDate = registrationStartDate,
                                        registrationEndDate = registrationEndDate,
                                        bookingStartDate = bookingStartDate,
                                        bookingEndDate = bookingEndDate,
                                        winnerCount = winnerCount,
                                        waitlistCapacity = capacity,
                                        invitedOwnerIds = invitedOwnerIds,
                                        participatingOwnerIds = listOf(ownerId),
                                        allowOtherOwners = invitedOwnerIds.isNotEmpty(),
                                    )

                                    lotteryRepo.addLottery(lottery) { error ->

                                        mainScope.launch(Dispatchers.Main.immediate) {

                                            if (error == null) {

                                                Toast.makeText(
                                                    applicationContext,
                                                    "Lottery created successfully.",
                                                    Toast.LENGTH_SHORT
                                                ).show()

                                                navController.popBackStack()

                                            } else {

                                                Toast.makeText(
                                                    applicationContext,
                                                    error.message
                                                        ?: "Unable to create lottery.",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }

                    /**
                     * Displays the entrants registered for a lottery
                     * and connects winner selection to the repository.
                     */
                    composable(
                        route = "ownerLotteryWaitlist/{lotteryId}",
                        arguments = listOf(
                            navArgument("lotteryId") {
                                type = NavType.StringType
                            }
                        )
                    ) { backStackEntry ->

                        val lotteryId = backStackEntry.arguments
                            ?.getString("lotteryId")
                            .orEmpty()

                        OwnerLotteryWaitlistScreen(
                            navController = navController,
                            lotteryId = lotteryId,
                            onDrawWinners = { selectedLotteryId, onComplete ->

                                mainScope.launch(Dispatchers.Main.immediate) {

                                    val ownerId = authManager.getCurrentUserId()

                                    if (ownerId.isNullOrBlank()) {

                                        onComplete(
                                            "Please sign in."
                                        )

                                    } else {

                                        lotteryRepo.drawWinners(
                                            lotteryId = selectedLotteryId,
                                            ownerId = ownerId
                                        ) { error ->

                                            mainScope.launch(Dispatchers.Main.immediate) {

                                                onComplete(
                                                    if (error == null) {
                                                        null
                                                    } else {
                                                        error.message
                                                            ?: "Unable to draw winners."
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }

                    /**
                     * Allows the campsite owner to edit
                     * an existing lottery.
                     */
                    composable(
                        route = "editLottery/{lotteryId}",
                        arguments = listOf(
                            navArgument("lotteryId") {
                                type = NavType.StringType
                            }
                        )
                    ) { backStackEntry ->

                        val lotteryId = backStackEntry.arguments
                            ?.getString("lotteryId")
                            .orEmpty()

                        EditLotteryScreen(
                            navController = navController,
                            lotteryId = lotteryId
                        )
                    }

                    /**
                     * Displays the booked and available dates
                     * for the selected campsite.
                     */
                    composable(
                        route = "booked_dates/{campsiteId}",
                        arguments = listOf(
                            navArgument("campsiteId") {
                                type = NavType.StringType
                            }
                        )
                    ) { backStackEntry ->

                        val campsiteId = backStackEntry.arguments
                            ?.getString("campsiteId")
                            .orEmpty()

                        ViewBookedDatesScreen(
                            navController = navController,
                            campsiteId = campsiteId
                        )
                    }

                    // END OF OWNER LOTTERY SCREENS

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

                    composable(
                        route = "reservationmap/{campsiteId}",
                        arguments = listOf(
                            navArgument("campsiteId") {
                                type = NavType.StringType
                            }
                        )
                    ) { backStackEntry ->

                        val campsiteId = backStackEntry.arguments
                            ?.getString("campsiteId")
                            .orEmpty()

                        ReservationMapScreen(
                            navController = navController,
                            campsiteId = campsiteId
                        )
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
                    composable("ownerSentNotifications") {
                        OwnerSentNotificationsScreen(navController)
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


                    // Campsite Details page (supports QR deep link: coders://campsite/{campsiteId})
                    composable(
                        route = "campsiteDetails/{campsiteId}",
                        arguments = listOf(
                            navArgument("campsiteId") {
                                type = NavType.StringType
                            }
                        ),
                        deepLinks = listOf(
                            navDeepLink {
                                uriPattern = "coders://campsite/{campsiteId}"
                            }
                        )
                    ) { backStackEntry ->
                        val campsiteId = backStackEntry.arguments
                            ?.getString("campsiteId")
                            .orEmpty()

                        val cachedCampsite = campsiteRepo
                            .getAllCampsites()
                            .firstOrNull { it.id == campsiteId }

                        var loadedCampsite by remember(campsiteId) { mutableStateOf(cachedCampsite) }
                        var isLoading by remember(campsiteId) { mutableStateOf(cachedCampsite == null) }

                        LaunchedEffect(campsiteId) {
                            if (loadedCampsite == null && campsiteId.isNotBlank()) {
                                campsiteRepo.getCampsiteById(campsiteId) { fetched, _ ->
                                    loadedCampsite = fetched
                                    isLoading = false
                                }
                            } else {
                                isLoading = false
                            }
                        }

                        when {
                            isLoading -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                            loadedCampsite != null -> {
                                CampsiteDetailsScreen(
                                    navController = navController,
                                    campsite = loadedCampsite!!
                                )
                            }
                            else -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Campsite not found")
                                }
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
                    // The following block was modified using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
                    composable("messagelogs")  {
                        AdminMessageLogs(
                            navController = navController
                        )
                    }
                    composable("notificationlogs") {
                        AdminNotificationLogs(
                            navController = navController
                        )
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
