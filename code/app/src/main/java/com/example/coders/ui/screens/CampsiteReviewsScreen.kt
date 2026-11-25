package com.example.coders.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.coders.ReservationReview
import com.example.coders.ReservationReviewRepo
import com.example.coders.ProfileRepo
import com.example.coders.CampsiteRepo
import com.example.coders.Admin
import com.example.coders.SignedInProfile

@Composable
fun ReviewRow(review: ReservationReview, name: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = name,
            fontWeight = FontWeight.Bold
        )

        Text(text = review.content)
    }
}

@Composable
fun CampsiteReviewsScreen(
    navController: NavController,
    campsiteId: String
) {
    val profile by SignedInProfile().rememberSignedInProfile()

    var reviews by remember(campsiteId) {
        mutableStateOf<List<ReservationReview>?>(emptyList())
    }

    var reviewerNames by remember(campsiteId) {
        mutableStateOf<Map<String, String>>(emptyMap())
    }

    var campsiteName by remember(campsiteId) {
        mutableStateOf("")
    }

    val reviewRepo = remember { ReservationReviewRepo() }
    val profileRepo = remember { ProfileRepo() }
    val campsiteRepo = remember { CampsiteRepo() }

    var isLoadingReview by remember(campsiteId) {
        mutableStateOf(true)
    }

    var backClicked by remember {
        mutableStateOf(false)
    }

    var reviewError by remember(campsiteId) {
        mutableStateOf<Exception?>(null)
    }

    var reviewToDelete by remember(campsiteId) {
        mutableStateOf<ReservationReview?>(null)
    }

    DisposableEffect(profileRepo, campsiteRepo) {
        onDispose {
            profileRepo.close()
            campsiteRepo.close()
        }
    }

    LaunchedEffect(campsiteId) {
        campsiteRepo.getCampsiteById(campsiteId) { campsite, error ->
            if (campsite != null) {
                campsiteName = campsite.name
            }
        }

        reviewRepo.getReviewsByCampsite(campsiteId) { result, error ->
            reviews = result
            reviewError = error
            isLoadingReview = false

            result?.map { it.entrantId }?.distinct()?.forEach { entrantId ->
                profileRepo.getProfileById(entrantId) { profile, error ->
                    if (profile != null) {
                        reviewerNames = reviewerNames +
                                (entrantId to profile.name)
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextButton(
            onClick = {
                if (!backClicked) {
                    backClicked = true
                    if (!navController.popBackStack()) {
                        backClicked = false
                    }
                }
            },
            enabled = !backClicked
        ) {
            Text("Back")
        }

        Text(
            text = campsiteName,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )

        when {
            isLoadingReview -> {
                CircularProgressIndicator()
            }

            reviewError != null -> {
                Text("Failed to load reviews")
            }

            reviews.isNullOrEmpty() -> {
                Text("No reviews yet")
            }

            else -> {
                LazyColumn {
                    items(
                        items = reviews.orEmpty(),
                        key = { it.reservationId }
                    ) { review ->

                        ReviewRow(
                            review = review,
                            name = reviewerNames[review.entrantId]
                                ?: "Unknown"
                        )

                        if (profile is Admin) {
                            DeleteButton(
                                onDelete = {
                                    reviewToDelete = review
                                }
                            )
                        }

                        HorizontalDivider()
                    }
                }
            }
        }

        reviewToDelete?.let { review ->
            DeleteConfirmationDialog(
                itemName = "this review",

                onConfirm = {
                    reviewToDelete = null

                    if (profile is Admin) {
                        reviewRepo.deleteReview(review.reservationId) { error ->
                            if (error == null) {
                                reviews = reviews.orEmpty().filterNot {
                                    it.reservationId == review.reservationId
                                }
                            } else {
                                reviewError = error
                            }
                        }
                    }
                },

                onDismiss = {
                    reviewToDelete = null
                }
            )
        }
    }
}