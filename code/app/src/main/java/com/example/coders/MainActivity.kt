package com.example.coders

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.coders.ui.theme.CodersTheme


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CodersTheme {
                var currentScreen by remember {
                    mutableStateOf("welcome")
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        when (currentScreen) {
                            "welcome" -> WelcomeScreen(
                                onNewUser = {
                                    currentScreen = "signup"
                                },
                                onExistingUser = {
                                    currentScreen = "login"
                                },
                                onAdminLogin = {
                                    currentScreen = "admin_login"
                                }
                            )

                            "signup" -> SignUpScreen(
                                onBack = {
                                    currentScreen = "welcome"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
