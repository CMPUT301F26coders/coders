/**
 * Displays and manages the signed-in entrant's profile information and settings.
 *
 * Uses AuthManager and Firebase Firestore to display and
 * update profile information, manage notification preferences, and delete
 * the user's profile.
 *
 * Outstanding issues: Only entrant profiles are supported 
 * (fulfills the user stories tied to profiles but could be improved).
 * Deleting a profile does not automatically remove associated records
 * such as reservations, conversations, or notifications.
 */

package com.example.coders.ui.screens

import android.app.NotificationManager
import android.content.Context
import android.util.Patterns
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.coders.AuthManager
import com.example.coders.Entrant
import com.example.coders.ProfileRepo
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.core.content.edit

/**
 * Displays the entrant's profile and account management options.
 *
 * Loads profile information from Firestore and allows the user to edit
 * personal details, toggle notification preferences, or delete their profile.
 *
 * @param navController Used to navigate between screens.
 */
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = remember { AuthManager(context.applicationContext) }
    val repo = remember { ProfileRepo() }
    val db = remember { FirebaseFirestore.getInstance() }

    var user by remember { mutableStateOf<Entrant?>(null) }
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

    LaunchedEffect(Unit) {
        try {
            val id = auth.getCurrentUserId()
            if (id.isNullOrBlank()) {
                error = "No user is signed in."
                loading = false
            } else {
                repo.getProfileById(id) { profile, exception ->
                    user = profile as? Entrant
                    if (exception != null || user == null) {
                        error = exception?.message ?: "Entrant not found."
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
                    Icon(
                        Icons.Default.AccountCircle, "Avatar",
                        modifier = Modifier.size(110.dp)
                    )
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
                        ProfileItem("Email", p.email) { openEdit("Email") }
                        HorizontalDivider()
                        ProfileItem("Phone Number", p.phone) {
                            openEdit("Phone Number")
                        }
                        HorizontalDivider()
                        Row(
                            Modifier.fillMaxWidth().heightIn(min = 56.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Person, null)
                            Spacer(Modifier.width(12.dp))
                            Text("Account Type", Modifier.weight(1f))
                            Text("● Entrant", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(Color.White),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(Modifier.padding(horizontal = 12.dp)) {
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
                                "name" -> p.copy(name = updated)
                                "email" -> p.copy(email = updated)
                                else -> p.copy(phone = updated)
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
                            scope.launch {
                                try {
                                    auth.clearUserId()
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
