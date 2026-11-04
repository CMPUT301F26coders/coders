package com.example.coders


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object UserSession {
    var isOwner by mutableStateOf(false)
}
