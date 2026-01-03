package dev.koenv.rentmycar.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import dev.koenv.rentmycar.app.screens.auth.LoginScreen
import dev.koenv.rentmycar.app.screens.home.HomeScreen
import dev.koenv.rentmycar.app.ui.AppTheme
import dev.koenv.rentmycar.shared.SharedModule
import dev.koenv.rentmycar.shared.repository.AuthState
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun App() {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val authRepository = remember { SharedModule.authRepository }
            val authState by authRepository.authState.collectAsState()
            
            // Determine initial screen based on auth state
            val initialScreen = when (authState) {
                AuthState.Authenticated -> HomeScreen()
                else -> LoginScreen()
            }
            
            Navigator(initialScreen) { navigator ->
                SlideTransition(navigator)
            }
        }
    }
}

