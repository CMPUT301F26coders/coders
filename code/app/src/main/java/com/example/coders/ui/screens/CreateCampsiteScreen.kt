/**
 * Gates and collects the required details for campsite registration.
 *
 * Outstanding issues: Map selection depends on the remote map-style service being available.
 */

package com.example.coders.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.coders.AuthManager
import com.example.coders.CAMPSITE_REGISTERED_MESSAGE
import com.example.coders.CAMPSITE_REGISTERED_RESULT_KEY
import com.example.coders.CampOwner
import com.example.coders.CampsiteRegistrationAccess
import com.example.coders.CampsiteRepo
import com.example.coders.CampsiteRequiredField
import com.example.coders.CampsiteTagResult
import com.example.coders.MAX_CAMPSITE_TAGS
import com.example.coders.ONLY_REGISTERED_USERS_MESSAGE
import com.example.coders.ProfileRepo
import com.example.coders.addCampsiteTag
import com.example.coders.buildCampsiteForRegistration
import com.example.coders.campsiteRegistrationAccess
import com.example.coders.dataUrlToImageBitmap
import com.example.coders.encodeProfileImage
import com.example.coders.missingCampsiteFields
import com.example.coders.parseCampsiteOccupancy
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

// The following composable was modified using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
/**
 * Checks whether the saved user is a registered campsite owner before rendering registration.
 *
 * @param navController Controls navigation after rejection or successful registration.
 * @param campsiteRepo Repository used to save the resulting campsite.
 */
@Composable
fun CreateCampsiteScreen(
    navController: NavController,
    campsiteRepo: CampsiteRepo?
) {
    val context = LocalContext.current
    var access by remember {
        mutableStateOf<CampsiteRegistrationAccess>(CampsiteRegistrationAccess.Loading)
    }
    var profileLoadFailed by remember { mutableStateOf(false) }

    // The following block was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
    LaunchedEffect(Unit) {
        try {
            val savedProfileId = AuthManager(context.applicationContext).getCurrentUserId()
            if (savedProfileId.isNullOrBlank()) {
                access = campsiteRegistrationAccess(null, null)
            } else {
                val profileRepo = ProfileRepo()
                try {
                    profileRepo.getProfileById(savedProfileId) { profile, error ->
                        profileRepo.close()
                        if (error != null) {
                            profileLoadFailed = true
                        } else {
                            access = campsiteRegistrationAccess(savedProfileId, profile)
                        }
                    }
                } catch (_: Exception) {
                    profileRepo.close()
                    profileLoadFailed = true
                }
            }
        } catch (_: Exception) {
            profileLoadFailed = true
        }
    }

    Scaffold(bottomBar = { AppBottomBar(navController = navController) }) { innerPadding ->
        when (val currentAccess = access) {
            CampsiteRegistrationAccess.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    if (profileLoadFailed) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Could not load your profile. Please check your connection and try again.")
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { navController.popBackStack() }) {
                                Text("Back")
                            }
                        }
                    } else {
                        CircularProgressIndicator(
                            modifier = Modifier.testTag("campsiteRegistrationLoading")
                        )
                    }
                }
            }

            is CampsiteRegistrationAccess.Rejected -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
                AlertDialog(
                    modifier = Modifier.testTag("registrationRejectedDialog"),
                    onDismissRequest = {
                        if (currentAccess.hasSavedAccount) {
                            navController.popBackStack()
                        } else {
                            navController.navigate("signup") {
                                popUpTo(navController.graph.id) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    },
                    title = { Text("Registration Not Allowed") },
                    text = { Text(ONLY_REGISTERED_USERS_MESSAGE) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (currentAccess.hasSavedAccount) {
                                    navController.popBackStack()
                                } else {
                                    navController.navigate("signup") {
                                        popUpTo(navController.graph.id) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            }
                        ) {
                            Text("Ok")
                        }
                    }
                )
            }

            is CampsiteRegistrationAccess.Allowed -> {
                CampsiteRegistrationForm(
                    navController = navController,
                    campsiteRepo = campsiteRepo,
                    owner = currentAccess.owner,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

// The following composable was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
/** Renders the validated campsite-owner registration form. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CampsiteRegistrationForm(
    navController: NavController,
    campsiteRepo: CampsiteRepo?,
    owner: CampOwner,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var occupancy by remember { mutableStateOf("") }
    var rules by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf(listOf<String>()) }
    var selectedLatitude by remember { mutableStateOf<Double?>(null) }
    var selectedLongitude by remember { mutableStateOf<Double?>(null) }
    var isSearchingAddress by remember { mutableStateOf(false) }
    var addressSearchMessage by remember { mutableStateOf<String?>(null) }
    var missingFields by remember { mutableStateOf<List<CampsiteRequiredField>?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    // The following block was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
    var imageDataUrl by remember { mutableStateOf<String?>(null) }
    var isEncodingImage by remember { mutableStateOf(false) }
    var imageError by remember { mutableStateOf<String?>(null) }

    // The following block was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            isEncodingImage = true
            imageError = null
            coroutineScope.launch {
                try {
                    imageDataUrl = encodeProfileImage(context.applicationContext, uri)
                } catch (exception: Exception) {
                    if (exception is CancellationException) throw exception
                    imageError = exception.message
                        ?: "Could not use that image. Please choose another one."
                } finally {
                    isEncodingImage = false
                }
            }
        }
    }

    missingFields?.let { fields ->
        AlertDialog(
            modifier = Modifier.testTag("missingInfoDialog"),
            onDismissRequest = { missingFields = null },
            title = { Text("Missing Information") },
            text = {
                Column {
                    Text("You did not provide all required information:")
                    fields.forEach { field -> Text("• ${field.label}") }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { missingFields = null },
                    modifier = Modifier.testTag("missingInfoRetryButton")
                ) { Text("Retry") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        missingFields = null
                        navController.popBackStack()
                    },
                    modifier = Modifier.testTag("missingInfoCancelButton")
                ) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Register a Campsite",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(16.dp))
        CampsiteInputField(
            label = "Enter Campsite Name",
            placeholder = "Name",
            value = name,
            onValueChange = { name = it },
            testTag = "input_Name"
        )

        Spacer(Modifier.height(12.dp))
        CampsiteInputField(
            label = "Enter Campsite Location",
            placeholder = "e.g. Pine Lake, Alberta, Canada",
            value = location,
            onValueChange = {
                location = it
                selectedLatitude = null
                selectedLongitude = null
                addressSearchMessage = null
            },
            testTag = "input_Location"
        )

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                isSearchingAddress = true
                addressSearchMessage = null
                coroutineScope.launch {
                    try {
                        val coordinates = AddressGeocoder.findCoordinates(context, location)
                        if (coordinates == null) {
                            selectedLatitude = null
                            selectedLongitude = null
                            addressSearchMessage = "Address not found. Try adding the city and province."
                        } else {
                            selectedLatitude = coordinates.first
                            selectedLongitude = coordinates.second
                            addressSearchMessage =
                                "Address found! You can adjust the location on the map."
                        }
                    } catch (exception: Exception) {
                        if (exception is CancellationException) throw exception
                        selectedLatitude = null
                        selectedLongitude = null
                        addressSearchMessage = "Unable to find this address. Please try again."
                    } finally {
                        isSearchingAddress = false
                    }
                }
            },
            enabled = location.isNotBlank() && !isSearchingAddress
        ) {
            if (isSearchingAddress) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(Modifier.size(8.dp))
                Text("Searching...")
            } else {
                Text("Find on Map")
            }
        }
        addressSearchMessage?.let { message ->
            Spacer(Modifier.height(4.dp))
            Text(message, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(Modifier.height(12.dp))
        CampsiteInputField(
            label = "Enter Campsite Description (amenities)",
            placeholder = "Description",
            value = description,
            onValueChange = { description = it },
            testTag = "input_Description"
        )

        Spacer(Modifier.height(12.dp))
        CampsiteInputField(
            label = "Enter Maximum Occupancy",
            placeholder = "Value",
            value = occupancy,
            onValueChange = { occupancy = it },
            testTag = "input_Occupancy",
            keyboardOptions = KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
            )
        )

        Spacer(Modifier.height(20.dp))
        Text("Select Campsite Location", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Search for an address first, then tap the map to adjust the exact campsite location.",
            fontSize = 13.sp
        )
        Spacer(Modifier.height(8.dp))
        CampsiteMapPicker(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .testTag("campsiteMapPicker"),
            initialLatitude = selectedLatitude,
            initialLongitude = selectedLongitude,
            onLocationSelected = { latitude, longitude ->
                selectedLatitude = latitude
                selectedLongitude = longitude
            }
        )
        Spacer(Modifier.height(6.dp))
        Text("Map data © OpenStreetMap contributors", fontSize = 11.sp)
        Spacer(Modifier.height(8.dp))
        if (selectedLatitude != null && selectedLongitude != null) {
            Text(
                text = "Selected Location: %.6f, %.6f".format(
                    selectedLatitude,
                    selectedLongitude
                ),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Text(
                "No location selected yet.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(20.dp))
        CampsiteInputField(
            label = "Enter Campsite Rules & Policies",
            placeholder = "Rules & Policies",
            value = rules,
            onValueChange = { rules = it }
        )

        Spacer(Modifier.height(20.dp))
        Text("Optional: Campsite Image and Tags", fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        CampsiteImageSection(
            imageDataUrl = imageDataUrl,
            isEncodingImage = isEncodingImage,
            imageError = imageError,
            onChooseImage = {
                imagePicker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onRemoveImage = {
                imageDataUrl = null
                imageError = null
            }
        )

        Spacer(Modifier.height(16.dp))
        CampsiteTagsSection(tags = tags, onTagsChanged = { tags = it })

        Spacer(Modifier.height(32.dp))
        Button(
            onClick = {
                val fields = missingCampsiteFields(
                    name,
                    location,
                    description,
                    occupancy,
                    selectedLatitude,
                    selectedLongitude
                )
                if (fields.isNotEmpty()) {
                    missingFields = fields
                } else if (campsiteRepo == null) {
                    Toast.makeText(
                        context,
                        "Campsite repository unavailable",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val capacity = parseCampsiteOccupancy(occupancy) ?: return@Button
                    val latitude = selectedLatitude ?: return@Button
                    val longitude = selectedLongitude ?: return@Button
                    isSaving = true
                    campsiteRepo.registerCampsite(
                        buildCampsiteForRegistration(
                            owner = owner,
                            name = name,
                            location = location,
                            description = description,
                            rules = rules,
                            capacity = capacity,
                            latitude = latitude,
                            longitude = longitude,
                            tags = tags
                        ),
                        imageDataUrl
                    ) { error ->
                        if (error == null) {
                            if (navController.currentBackStackEntry?.destination?.route ==
                                "create_campsite"
                            ) {
                                navController.previousBackStackEntry?.savedStateHandle?.set(
                                    CAMPSITE_REGISTERED_RESULT_KEY,
                                    CAMPSITE_REGISTERED_MESSAGE
                                )
                                navController.popBackStack()
                            }
                        } else {
                            isSaving = false
                            Toast.makeText(
                                context,
                                "Failed to save campsite. Please try again.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            },
            enabled = !isSaving && !isEncodingImage,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("confirmCampsiteButton"),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onSurface,
                contentColor = MaterialTheme.colorScheme.surface
            )
        ) {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(Modifier.size(8.dp))
                Text("Saving...")
            } else {
                Text("Confirm Campsite", fontSize = 16.sp)
            }
        }
    }
}

// The following composable was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
/** Shows the campsite's optional, locally encoded image selection controls. */
@Composable
private fun CampsiteImageSection(
    imageDataUrl: String?,
    isEncodingImage: Boolean,
    imageError: String?,
    onChooseImage: () -> Unit,
    onRemoveImage: () -> Unit
) {
    val bitmap = remember(imageDataUrl) { imageDataUrl?.let(::dataUrlToImageBitmap) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Optional image", fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        if (imageDataUrl == null) {
            Button(
                onClick = onChooseImage,
                enabled = !isEncodingImage,
                modifier = Modifier.testTag("addCampsiteImageButton")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.size(6.dp))
                Text("Add Image")
            }
        } else {
            if (bitmap == null) {
                Text("Image selected")
            } else {
                Image(
                    bitmap = bitmap,
                    contentDescription = "Selected campsite image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .testTag("campsiteImagePreview")
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onChooseImage,
                    enabled = !isEncodingImage,
                    modifier = Modifier.testTag("changeCampsiteImageButton")
                ) { Text("Change Image") }
                TextButton(
                    onClick = onRemoveImage,
                    enabled = !isEncodingImage,
                    modifier = Modifier.testTag("removeCampsiteImageButton")
                ) { Text("Remove Image") }
            }
        }
        if (isEncodingImage) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(Modifier.size(8.dp))
                Text("Processing image...")
            }
        }
        imageError?.let { error ->
            Spacer(Modifier.height(4.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }
    }
}

// The following composable was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
/** Collects, validates, and displays the optional campsite tags. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun CampsiteTagsSection(
    tags: List<String>,
    onTagsChanged: (List<String>) -> Unit
) {
    var tagInput by remember { mutableStateOf("") }
    var tagMessage by remember { mutableStateOf<String?>(null) }
    val addTag = {
        when (val result = addCampsiteTag(tags, tagInput)) {
            is CampsiteTagResult.Added -> {
                onTagsChanged(result.tags)
                tagInput = ""
                tagMessage = null
            }

            CampsiteTagResult.TooLong -> tagMessage = "Tags can be up to 30 characters."
            CampsiteTagResult.Duplicate -> {
                tagInput = ""
                tagMessage = "That tag is already added."
            }

            CampsiteTagResult.LimitReached -> tagMessage = "You can add up to 10 tags."
            CampsiteTagResult.Blank -> Unit
        }
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Optional tags", fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = tagInput,
                onValueChange = { tagInput = it },
                placeholder = { Text("e.g. lakefront") },
                singleLine = true,
                enabled = tags.size < MAX_CAMPSITE_TAGS,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onDone = { addTag() }
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_Tag")
            )
            Spacer(Modifier.size(8.dp))
            FilledTonalIconButton(
                onClick = addTag,
                enabled = tags.size < MAX_CAMPSITE_TAGS && tagInput.isNotBlank(),
                modifier = Modifier.testTag("addTagButton")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Tag")
            }
        }
        if (tags.size >= MAX_CAMPSITE_TAGS) {
            Text("You can add up to 10 tags.", color = MaterialTheme.colorScheme.primary)
        }
        tagMessage?.let { message ->
            Text(message, color = MaterialTheme.colorScheme.error)
        }
        if (tags.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                tags.forEach { tag ->
                    InputChip(
                        selected = false,
                        onClick = { onTagsChanged(tags - tag) },
                        label = { Text(tag) },
                        trailingIcon = {
                            IconButton(
                                onClick = { onTagsChanged(tags - tag) },
                                modifier = Modifier
                                    .size(24.dp)
                                    .semantics { contentDescription = "Remove tag $tag" }
                                    .testTag("removeTag_$tag")
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

// The following composable was modified using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
/** Displays a labelled single-line registration field. */
@Composable
fun CampsiteInputField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    testTag: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 14.sp)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    placeholder,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            keyboardOptions = keyboardOptions
        )
    }
}

// The following composable was modified using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
/** Previews the registration form with a non-persistent repository. */
@Preview(showBackground = true)
@Composable
fun PreviewCreateCampsiteScreen() {
    CampsiteRegistrationForm(
        navController = rememberNavController(),
        campsiteRepo = null,
        owner = CampOwner(name = "Preview Owner")
    )
}
