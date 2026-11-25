package com.example.coders.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.layout.Column

data class Deletion(
    val deletedObjectId: String = "", //id of deleted object
    val collectionName: String = "",
    val reason: String = "",
    val deletedBy: String = "",
    @ServerTimestamp
    val deletedAt: Timestamp? = null
)

class DeletionRepo  {

    private val deletionRef = FirebaseFirestore.getInstance()
        .collection("deletions")

    /**
     * Adds a campsite to the repository.
     *
     * @param onComplete Called with `null` on success or the error on failure.
     */
    fun addDeletion(
        deletion: Deletion,
        onComplete: (Exception?) -> Unit = {}
    ) {
        deletionRef.add(deletion)
            .addOnSuccessListener {
                onComplete(null)
            }
            .addOnFailureListener { error ->
                onComplete(error)
            }
    }

}


@Composable
fun DeleteButton(onDelete: () -> Unit) {
    IconButton(onClick = onDelete) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete",
            tint = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun DeleteConfirmationDialogAdmin(
    itemName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Confirm Deletion")
        },
        text = {
            Column {
                Text("Please enter the reason for the deletion of $itemName")

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },



        confirmButton = {
            TextButton(onClick = {onConfirm(reason)},
                    enabled = (!reason.isBlank())) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}