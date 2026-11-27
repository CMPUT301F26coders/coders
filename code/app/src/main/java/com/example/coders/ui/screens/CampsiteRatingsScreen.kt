package com.example.coders.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import com.example.coders.ProfileRepo
import com.example.coders.CampsiteRepo
import com.example.coders.ReservationRating
import com.example.coders.ReservationRatingRepo
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import com.example.coders.UserRole
import com.example.coders.SignedInProfile
import androidx.compose.runtime.DisposableEffect
import android.util.Log

@Composable
fun RatingRow(rating: ReservationRating, name: String) {
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

        Text(text = "${rating.score}/5")
    }
}

@Composable
fun CampsiteRatingsScreen(
    navController: NavController,
    campsiteId: String
) {
    val profile by SignedInProfile().rememberSignedInProfile()

    var ratings by remember(campsiteId) {
        mutableStateOf<List<ReservationRating>?>(emptyList())
    }

    var ratingNames by remember(campsiteId) {
        mutableStateOf<Map<String, String>>(emptyMap())
    }

    var campsiteName by remember(campsiteId) { mutableStateOf("") }
    val ratingRepo = remember { ReservationRatingRepo() }
    val profileRepo = remember { ProfileRepo() }
    val campsiteRepo = remember { CampsiteRepo() }
    val deletionRepo = remember { DeletionRepo() }

    var isLoadingRating by remember(campsiteId) {
        mutableStateOf(true)
    }
    var backClicked by remember(){ mutableStateOf(false)}

    var ratingError by remember(campsiteId) {
        mutableStateOf<Exception?>(null)
    }
    var deleteError by remember(campsiteId) {
        mutableStateOf<String?>(null)
    }


    var ratingToDelete by remember {
        mutableStateOf<ReservationRating?>(null)
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

        ratingRepo.getRatingsByCampsite(campsiteId) { result, error ->
            ratings = result
            ratingError = error
            isLoadingRating = false

            result?.map { it.entrantId }?.distinct()?.forEach { entrantId ->
                profileRepo.getProfileById(entrantId) { profile, error ->
                    if (profile != null) {
                        ratingNames = ratingNames +
                                (entrantId to profile.name)
                    }
                }
            }
        }
    }
    Column (
        modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ){
        TextButton(
            onClick = {
                if (!backClicked) {
                    backClicked = true
                    navController.popBackStack()
                }
            }
        ) {
            Text("Back")
        }

        Text(
            text = campsiteName,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )

        deleteError?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )
        }
        when {
            isLoadingRating -> {
                CircularProgressIndicator()
            }

            ratingError != null -> {
                Text("Failed to load ratings")
            }

            ratings.isNullOrEmpty() -> {
                Text("No ratings yet")
            }

            else -> {
                LazyColumn {
                    items(
                        items = ratings.orEmpty(),
                        key = { it.reservationId }
                    ) { rating ->

                        RatingRow(
                            rating = rating,
                            name = ratingNames[rating.entrantId] ?: "Unknown"
                        )

                        if (profile?.role == UserRole.ADMIN) {
                            DeleteButton(
                                onDelete = {
                                    ratingToDelete = rating
                                }
                            )
                        }

                        HorizontalDivider()
                    }
                }
            }
        }
        ratingToDelete?.let { rating ->
            DeleteConfirmationDialogAdmin(
                itemName = "this rating",

                onConfirm = { reason ->
                    ratingToDelete = null

                    val currentProfile = profile

                    if (currentProfile?.role == UserRole.ADMIN) { //stupid check because apparently it cant be sure its still an admin???
                    ratingRepo.deleteRating(rating.reservationId) { error ->
                        if (error == null) {
                            ratings = ratings.orEmpty().filterNot {
                                it.reservationId == rating.reservationId
                            }


                            val deletion = Deletion(
                                deletedObjectId = rating.reservationId,
                                collectionName = "reservationRatings",
                                reason = reason,
                                objectDeletedBy = currentProfile.id,
                                objectCreatedBy = rating.entrantId
                            )

                            deletionRepo.addDeletion(deletion)
                        } else {
                            deleteError = error.message ?: "Failed to delete rating."
                        }
                    }
                    }

                },

                onDismiss = {
                    ratingToDelete = null
                }
            )
        }

    }


}