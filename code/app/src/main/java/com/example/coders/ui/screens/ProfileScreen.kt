/**
 * Displays and manages profile information for all user types.
 *
 * Allows users to view and update their personal information
 * and delete their profiles.
 *
 * Entrant view provides notification preferences.
 * Administrators can switch between entrant, campsite owner,
 * and administrator interfaces.
 *
 * Outstanding issues:
 * Deleting a profile does not automatically remove associated
 * reservations, conversations, or notifications.
 */

package com.example.coders.ui.screens

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.coders.AuthManager
import com.example.coders.Image as ImageRecord
import com.example.coders.ProfileRepo
import com.example.coders.ProfileImageRepo
import com.example.coders.dataUrlToImageBitmap
import com.example.coders.encodeProfileImage
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.core.content.edit
import com.example.coders.Profile
import com.example.coders.UserSession
import com.example.coders.ViewMode
import com.example.coders.Admin

/**
 * Displays the user's profile and account management options.
 *
 * Loads profile information from Firestore and allows the user to edit
 * personal details, toggle notification preferences, or delete their profile.
 *
 * @param navController Used to navigate between screens.
 */
// The following composable was modified using OpenAI, Codex, "Implement US 01.08.02: add, update,
// and remove profile images stored as Firestore data URLs", 2026-11-22.
@OptIn(ExperimentalMaterial3Api::class) //needed for dropdown menu
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = remember { AuthManager(context.applicationContext) }
    val repo = remember { ProfileRepo() }
    val profileImageRepo = remember { ProfileImageRepo() }
    val db = remember { FirebaseFirestore.getInstance() }

    var user by remember { mutableStateOf<Profile?>(null) }
    var loading by remember { mutableStateOf(true) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var info by remember { mutableStateOf("") }

    var editField by remember { mutableStateOf<String?>(null) }
    var editValue by remember { mutableStateOf("") }
    var dnd by remember { mutableStateOf(false) }
    var pendingDnd by remember { mutableStateOf<Boolean?>(null) }
    var notificationSuccess by remember { mutableStateOf<Boolean?>(null) }
    var deleteConfirm by remember { mutableStateOf(false) }
    var deleteSuccess by remember { mutableStateOf(false) }
    var profileImage by remember { mutableStateOf<ImageRecord?>(null) }
    var profileImageMenuExpanded by remember { mutableStateOf(false) }

    // The following block was modified using OpenAI, Codex, "Implement US 01.08.02: add, update,
    // and remove profile images stored as Firestore data URLs", 2026-11-22.
    val profileImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        val profile = user
        if (uri != null && profile != null) {
            busy = true
            error = ""
            info = ""
            scope.launch {
                try {
                    val dataUrl = encodeProfileImage(context.applicationContext, uri)
                    profileImageRepo.setProfileImage(profile.id, dataUrl) { exception ->
                        busy = false
                        if (exception == null) {
                            info = "Profile picture updated"
                            error = ""
                        } else {
                            error = exception.message ?: "Failed to update profile picture."
                        }
                    }
                } catch (exception: Exception) {
                    busy = false
                    error = exception.message ?: "Failed to encode profile picture."
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        try {
            val id = auth.getCurrentUserId()
            if (id.isNullOrBlank()) {
                error = "No user is signed in."
                loading = false
            } else {
                repo.getProfileById(id) { profile, exception ->
                    user = profile
                    if (exception != null || user == null) {
                        error = exception?.message ?: "Profile not found."
                        loading = false
                    } else {
                        db.collection("profiles").document(id).get()
                            .addOnSuccessListener {
                                dnd = it.getBoolean("doNotDisturb") ?: false
                                context.getSharedPreferences(
                                    "entrant_notification_settings",
                                    Context.MODE_PRIVATE
                                ).edit {
                                    putBoolean("dnd_$id", dnd)
                                }
                                loading = false
                            }
                            .addOnFailureListener {
                                error = it.message ?: "Failed to load settings."
                                loading = false
                            }
                    }
                }
            }
        } catch (e: Exception) {
            error = e.message ?: "Failed to load profile."
            loading = false
        }
    }

    // The following block was modified using OpenAI, Codex, "Implement US 01.08.02: add, update,
    // and remove profile images stored as Firestore data URLs", 2026-11-22.
    DisposableEffect(user?.id) {
        val profileId = user?.id
        if (profileId == null) {
            onDispose { }
        } else {
            val registration = profileImageRepo.listenForProfileImage(
                profileId = profileId,
                onUpdate = { image -> profileImage = image },
                onError = { exception ->
                    error = exception.message ?: "Failed to load profile picture."
                }
            )
            onDispose { registration.remove() }
        }
    }

    fun openEdit(field: String) {
        val p = user ?: return
        editField = field
        editValue = when (field) {
            "First Name" -> p.name.substringBefore(" ")
            "Last Name" -> p.name.substringAfter(" ", "")
            "Email" -> p.email
            else -> p.phone
        }
        error = ""
        info = ""
    }

    Scaffold(
        containerColor = Color(0xFFF4F4F8),
        bottomBar = { AppBottomBar(navController) }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding)
                .safeDrawingPadding().verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
            }

            if (loading) {
                CircularProgressIndicator()
            } else if (user == null) {
                Text(error.ifBlank { "Profile not found." })
            } else {
                val p = user!!

                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // The following block was modified using OpenAI, Codex, "Implement US 01.08.02: add, update,
                    // and remove profile images stored as Firestore data URLs", 2026-11-22.
                    ProfileAvatar(profileImage?.url)

                    // The following block was modified using OpenAI, Codex, "Implement US 01.08.02: add, update,
                    // and remove profile images stored as Firestore data URLs", 2026-11-22.
                    if (profileImage == null) {
                        TextButton(
                            onClick = {
                                profileImagePicker.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            enabled = !busy,
                            modifier = Modifier.testTag("editProfileImageButton")
                        ) {
                            Icon(Icons.Default.AddAPhoto, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Add photo")
                        }
                    } else {
                        Box {
                            TextButton(
                                onClick = { profileImageMenuExpanded = true },
                                enabled = !busy,
                                modifier = Modifier.testTag("editProfileImageButton")
                            ) {
                                Icon(Icons.Default.Edit, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Edit photo")
                            }
                            DropdownMenu(
                                expanded = profileImageMenuExpanded,
                                onDismissRequest = { profileImageMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Change photo") },
                                    onClick = {
                                        profileImageMenuExpanded = false
                                        profileImagePicker.launch(
                                            PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageOnly
                                            )
                                        )
                                    },
                                    modifier = Modifier.testTag("changeProfileImageOption")
                                )
                                DropdownMenuItem(
                                    text = { Text("Remove photo") },
                                    onClick = {
                                        profileImageMenuExpanded = false
                                        busy = true
                                        error = ""
                                        info = ""
                                        profileImageRepo.removeProfileImage(p.id) { exception ->
                                            busy = false
                                            if (exception == null) {
                                                info = "Profile picture removed"
                                            } else {
                                                error = exception.message
                                                    ?: "Failed to remove profile picture."
                                            }
                                        }
                                    },
                                    modifier = Modifier.testTag("removeProfileImageOption")
                                )
                            }
                        }
                    }
                    Text(
                        p.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(p.email)
                    Text(p.phone)
                    Text(p.id)
                }
                Card(
                    colors = CardDefaults.cardColors(Color.White),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        ProfileItem("First Name", p.name.substringBefore(" ")) {
                            openEdit("First Name")
                        }

                        HorizontalDivider()

                        ProfileItem("Last Name", p.name.substringAfter(" ", "")) {
                            openEdit("Last Name")
                        }

                        HorizontalDivider()

                        ProfileItem("Email", p.email) {
                            openEdit("Email")
                        }

                        HorizontalDivider()

                        ProfileItem("Phone Number", p.phone) {
                            openEdit("Phone Number")
                        }

                        HorizontalDivider()

                        Text("Account Type: ${p.type}")
                    }
                }
                if (p is Admin) {

                    var expanded by remember { mutableStateOf(false) }

                    val viewOptions = listOf(
                        ViewMode.ADMIN,
                        ViewMode.ENTRANT,
                        ViewMode.CAMP_OWNER
                    )

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = UserSession.viewMode.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Switch View") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = expanded
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            viewOptions.forEach { mode ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            when (mode) {
                                                ViewMode.ADMIN -> "Admin"
                                                ViewMode.ENTRANT -> "Entrant"
                                                ViewMode.CAMP_OWNER -> "Campsite Owner"
                                            }
                                        )
                                    },
                                    onClick = {
                                        UserSession.switchView(p, mode)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }


                Card(
                    colors = CardDefaults.cardColors(Color.White),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(Modifier.padding(horizontal = 12.dp)) {
                        if(UserSession.viewMode == ViewMode.ENTRANT) {
                            Row(
                                Modifier.fillMaxWidth().heightIn(min = 64.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Notifications, null)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text("Turn Off Notifications")
                                    Text(
                                        if (dnd) "Notifications muted"
                                        else "Notifications enabled",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                                Switch(
                                    checked = !dnd,
                                    enabled = !busy,
                                    onCheckedChange = { pendingDnd = !it }
                                )
                            }
                            HorizontalDivider()
                        }
                        Row(
                            Modifier.fillMaxWidth().heightIn(min = 56.dp)
                                .clickable { deleteConfirm = true },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Delete, null)
                            Spacer(Modifier.width(12.dp))
                            Text("Delete My Profile", Modifier.weight(1f))
                            Icon(Icons.Default.ChevronRight, null)
                        }
                    }
                }

                if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
                if (error.isNotBlank()) {
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
                if (info.isNotBlank()) {
                    Text(info, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }

    // Edit an individual field
    editField?.let { field ->
        val value = editValue.trim()
        val valid = if (field == "Email") {
            Patterns.EMAIL_ADDRESS.matcher(value).matches()
        } else value.isNotEmpty()

        AlertDialog(
            onDismissRequest = { if (!busy) editField = null },
            title = { Text("Edit $field") },
            text = {
                OutlinedTextField(
                    value = editValue,
                    onValueChange = { editValue = it },
                    label = { Text(field) },
                    singleLine = true,
                    enabled = !busy
                )
            },
            confirmButton = {
                TextButton(enabled = valid && !busy, onClick = {
                    val p = user ?: return@TextButton
                    val key = when (field) {
                        "First Name", "Last Name" -> "name"
                        "Email" -> "email"
                        else -> "phone"
                    }
                    val updated = when (field) {
                        "First Name" ->
                            "$value ${p.name.substringAfter(" ", "")}".trim()
                        "Last Name" ->
                            "${p.name.substringBefore(" ")} $value".trim()
                        else -> value
                    }
                    busy = true
                    db.collection("profiles").document(p.id)
                        .update(key, updated)
                        .addOnSuccessListener {
                            user = when (key) {
                                "name" -> p.withUpdatedDetails(name = updated)
                                "email" -> p.withUpdatedDetails(email = updated)
                                else -> p.withUpdatedDetails(phone = updated)
                            }
                            busy = false
                            editField = null
                            info = "$field updated successfully."
                            error = ""
                        }
                        .addOnFailureListener {
                            busy = false
                            error = it.message ?: "Update failed."
                        }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(
                    enabled = !busy,
                    onClick = { editField = null }
                ) { Text("Cancel") }
            }
        )
    }

    // Notification confirmation
    pendingDnd?.let { requested ->
        AlertDialog(
            onDismissRequest = { if (!busy) pendingDnd = null },
            icon = { Icon(Icons.Default.NotificationsOff, null) },
            title = {
                Text(if (requested) "Turn Off Notifications?"
                else "Turn On Notifications?")
            },
            text = {
                Text(if (requested)
                    "You will no longer receive notification alerts."
                else "You will receive notification alerts again.")
            },
            confirmButton = {
                Button(enabled = !busy, onClick = {
                    val p = user ?: return@Button
                    busy = true
                    db.collection("profiles").document(p.id)
                        .update("doNotDisturb", requested)
                        .addOnSuccessListener {
                            dnd = requested
                            context.getSharedPreferences(
                                "entrant_notification_settings",
                                Context.MODE_PRIVATE
                            ).edit {
                                putBoolean("dnd_${p.id}", requested)
                            }

                            if (requested) {
                                val manager = context.getSystemService(
                                    Context.NOTIFICATION_SERVICE
                                ) as NotificationManager
                                manager.cancelAll()
                            }
                            busy = false
                            pendingDnd = null
                            notificationSuccess = requested
                            error = ""
                        }
                        .addOnFailureListener {
                            busy = false
                            pendingDnd = null
                            error = it.message ?: "Failed to save setting."
                        }
                }) { Text(if (requested) "Turn Off" else "Turn On") }
            },
            dismissButton = {
                TextButton(
                    enabled = !busy,
                    onClick = { pendingDnd = null }
                ) { Text("Cancel") }
            }
        )
    }

    // Notification success
    notificationSuccess?.let { off ->
        AlertDialog(
            onDismissRequest = { notificationSuccess = null },
            icon = {
                Icon(
                    Icons.Default.CheckCircle, null,
                    tint = Color(0xFF23B65D)
                )
            },
            title = {
                Text(if (off) "Notifications Turned Off"
                else "Notifications Turned On")
            },
            text = { Text("Your notification preference has been saved.") },
            confirmButton = {
                Button(onClick = { notificationSuccess = null }) {
                    Text("OK")
                }
            }
        )
    }

    // Delete confirmation
    if (deleteConfirm) {
        AlertDialog(
            onDismissRequest = { if (!busy) deleteConfirm = false },
            title = { Text("Delete My Profile?") },
            text = {
                Text("Are you sure? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(enabled = !busy, onClick = {
                    val p = user ?: return@TextButton
                    busy = true
                    db.collection("profiles").document(p.id).delete()
                        .addOnSuccessListener {
                            // The following block was modified using OpenAI, Codex, "Implement US 01.08.02: add, update,
                            // and remove profile images stored as Firestore data URLs", 2026-11-22.
                            profileImageRepo.removeProfileImage(p.id) { imageError ->
                                if (imageError != null) {
                                    Log.e(
                                        "ProfileScreen",
                                        "Profile deleted but profile-image cleanup failed.",
                                        imageError
                                    )
                                }
                                scope.launch {
                                    try {
                                        auth.clearUserId()
                                        UserSession.clearSession()
                                        context.getSharedPreferences(
                                            "entrant_notification_settings",
                                            Context.MODE_PRIVATE
                                        ).edit {
                                            remove("dnd_${p.id}")
                                        }
                                        busy = false
                                        deleteConfirm = false
                                        deleteSuccess = true
                                    } catch (_: Exception) {
                                        busy = false
                                        deleteConfirm = false
                                        error = "Profile deleted, but session cleanup failed."
                                    }
                                }
                            }
                        }
                        .addOnFailureListener {
                            busy = false
                            deleteConfirm = false
                            error = it.message ?: "Delete failed."
                        }
                }) {
                    Text(
                        "Delete",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !busy,
                    onClick = { deleteConfirm = false }
                ) { Text("Cancel") }
            }
        )
    }

    if (deleteSuccess) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Profile Deleted") },
            text = { Text("Your profile has been deleted successfully.") },
            confirmButton = {
                Button(onClick = {
                    navController.navigate("signup") {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                    deleteSuccess = false
                }) { Text("I Understand") }
            }
        )
    }
}

// The following composable was added using OpenAI, Codex, "Implement US 01.08.02: add, update,
// and remove profile images stored as Firestore data URLs", 2026-11-22.
/**
 * Renders a circular profile image or the shared default avatar when no decodable image exists.
 *
 * @param dataUrl The selected profile image stored as a data URL, if any.
 */
@Composable
private fun ProfileAvatar(dataUrl: String?) {
    val bitmap = remember(dataUrl) { dataUrl?.let(::dataUrlToImageBitmap) }
    val avatarModifier = Modifier
        .size(110.dp)
        .clip(CircleShape)

    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = "Profile picture",
            contentScale = ContentScale.Crop,
            modifier = avatarModifier.testTag("profileAvatarImage")
        )
    } else {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Default profile picture",
            modifier = avatarModifier.testTag("profileAvatarDefault")
        )
    }
}

/**
 * Displays a profile field with its current value and an edit button.
 *
 * @param label The name of the profile field.
 * @param value The current value of the field.
 * @param onEdit Callback invoked when the edit button is pressed.
 */
@Composable
private fun ProfileItem(
    label: String,
    value: String,
    onEdit: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 60.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Person, null, Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(value.ifBlank { "Not provided" })
        }
        IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, "Edit $label")
        }
    }
}
