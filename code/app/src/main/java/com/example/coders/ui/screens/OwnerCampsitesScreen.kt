package com.example.coders.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import com.example.coders.CampOwner
import com.example.coders.CAMPSITE_REGISTERED_RESULT_KEY
import com.example.coders.Campsite
import com.example.coders.CampsiteListingImageRepo
import com.example.coders.CampsiteRepo
import com.example.coders.Image as ImageRecord
import com.example.coders.SignedInProfile
import com.example.coders.dataUrlToImageBitmap
import com.example.coders.encodeProfileImage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
/**
 * Displays a list of campsites
 *
 */
// The following composable was modified using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
@Composable
fun OwnerCampsitesScreen(
    navController: NavController,
    campsiteRepo: CampsiteRepo? = null,
    savedStateHandle: SavedStateHandle? = null
) {

    val repo = remember { campsiteRepo ?: CampsiteRepo() }
    val snackbarHostState = remember { SnackbarHostState() }


    val profile by SignedInProfile().rememberSignedInProfile()
    var campsiteToDelete by remember { mutableStateOf<Campsite?>(null) }
    var isNavigating by remember { mutableStateOf(false) }

    var campsites by remember(profile?.id) {
        mutableStateOf<List<Campsite>>(emptyList())
    }
    DisposableEffect(profile?.id, repo) {
        val ownerId = (profile as? CampOwner)?.id

        val listener = if (!ownerId.isNullOrBlank()) {
            repo.listenForOwnerCampsites(
                ownerId = ownerId,
                onUpdate = { updatedCampsites ->
                    campsites = updatedCampsites
                }
            )
        } else {
            null
        }

        onDispose {
            listener?.remove()
        }
    }

    DisposableEffect(repo) {
        onDispose {
            if (campsiteRepo == null) {
                repo.close()
            }
        }
    }

    // The following block was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
    LaunchedEffect(savedStateHandle) {
        savedStateHandle
            ?.getStateFlow<String?>(CAMPSITE_REGISTERED_RESULT_KEY, null)
            ?.collect { message ->
                if (message != null) {
                    savedStateHandle[CAMPSITE_REGISTERED_RESULT_KEY] = null
                    snackbarHostState.showSnackbar(message)
                }
            }
    }

    Scaffold(
        bottomBar = {
            AppBottomBar(
                navController = navController
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 2.dp)
        ) {
            IconButton(
                onClick = {
                    if(!isNavigating) {
                        navController.popBackStack()
                }
                }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            Text(
                text = "My Campsites:",
                fontSize = 21.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(2.dp))

            campsites.forEach { campsite ->
                OwnerCampsiteCard(
                    campsite = campsite,
                    onDetailsClick = { navController.navigate("campsiteDetails/${campsite.id}") },
                    onDeleteClick = { campsiteToDelete = campsite }
                )
                Spacer(modifier = Modifier.height(25.dp))
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { navController.navigate("create_campsite") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .testTag("registerCampsiteButton"),
                shape = RoundedCornerShape(0.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Register", modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(18.dp))
                Text("Register a Campsite", fontSize = 11.sp, fontWeight = FontWeight.Normal)
            }
        }

        campsiteToDelete?.let { target ->
            AlertDialog(
                onDismissRequest = { campsiteToDelete = null },
                title = { Text("Delete ${target.name}?", fontSize = 12.sp) },
                text = { Text("Are you sure? cant undo.", fontSize = 10.sp) },
                confirmButton = {
                    TextButton(onClick = {
                        repo.deleteCampsite(target)
                        campsiteToDelete = null
                    }) { Text("Delete", fontSize = 10.sp) }
                },
                dismissButton = {
                    TextButton(onClick = { campsiteToDelete = null }) { Text("Nevermind", fontSize = 10.sp) }
                }
            )
        }
    }
}

// The following composable was modified using OpenAI, Codex, "Add campsite image uploads to owner listings", 2026-11-26.
/** Renders a summary card for a campsite, including its primary listing image. */
@Composable
fun OwnerCampsiteCard(
    campsite: Campsite,
    onDetailsClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDetailsClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.Top
        ) {
            CampsiteListingImage(campsite)

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = campsite.name, fontSize = 22.sp)
                Text(text = campsite.location, fontSize = 10.sp)
                Text(text = "${campsite.capacity} guests", fontSize = 14.sp)
            }

            CampsiteQrCodeImage(
                campsite = campsite,
                size = 56.dp
            )

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(onClick = onDeleteClick, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Default.Remove, contentDescription = "Delete")
            }
        }
    }
}

// The following composable was added using OpenAI, Codex, "Add campsite image uploads to owner listings", 2026-11-26.
/**
 * Shows a campsite's primary listing image and lets its owner replace it.
 *
 * Image documents use the same deterministic campsite ID as the registration flow, so a photo
 * selected while registering is shown here as soon as Firestore delivers it.
 */
@Composable
private fun CampsiteListingImage(campsite: Campsite) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val imageRepo = remember { CampsiteListingImageRepo() }
    var image by remember(campsite.id) { mutableStateOf<ImageRecord?>(null) }
    var isUploading by remember(campsite.id) { mutableStateOf(false) }
    var error by remember(campsite.id) { mutableStateOf<String?>(null) }
    val uploaderId = campsite.campOwnerId.ifBlank { campsite.campOwner.id }

    DisposableEffect(campsite.id) {
        val listener = imageRepo.listenForCampsiteImage(
            campsiteId = campsite.id,
            onUpdate = { image = it },
            onError = { exception ->
                error = exception.message ?: "Unable to load campsite image."
            }
        )
        onDispose { listener.remove() }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            isUploading = true
            error = null
            scope.launch {
                try {
                    val dataUrl = encodeProfileImage(context.applicationContext, uri)
                    imageRepo.setCampsiteImage(campsite.id, uploaderId, dataUrl) { exception ->
                        isUploading = false
                        if (exception != null) {
                            error = exception.message ?: "Unable to save campsite image."
                        }
                    }
                } catch (exception: Exception) {
                    if (exception is CancellationException) throw exception
                    isUploading = false
                    error = exception.message ?: "Unable to use that image."
                }
            }
        }
    }

    val bitmap = remember(image?.url) { image?.url?.let(::dataUrlToImageBitmap) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.error),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap == null) {
                Icon(
                    imageVector = Icons.Default.Landscape,
                    contentDescription = "Campsite image for ${campsite.name}",
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onError
                )
            } else {
                Image(
                    bitmap = bitmap,
                    contentDescription = "Campsite image for ${campsite.name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
            }

            if (isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 3.dp,
                    color = MaterialTheme.colorScheme.onError
                )
            } else {
                IconButton(
                    onClick = {
                        imagePicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(50)
                        )
                        .size(32.dp)
                        .testTag("uploadCampsiteImage_${campsite.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = if (image == null) {
                            "Add image for ${campsite.name}"
                        } else {
                            "Change image for ${campsite.name}"
                        },
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
        error?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                fontSize = 10.sp
            )
        }
    }
}
