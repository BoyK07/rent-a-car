package dev.koenv.rentmycar.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import dev.koenv.rentmycar.app.screens.auth.LoginScreen
import dev.koenv.rentmycar.app.screens.home.HomeScreen
import dev.koenv.rentmycar.app.ui.AppTheme
import dev.koenv.rentmycar.app.ui.components.Surface
import dev.koenv.rentmycar.shared.SharedModule
import dev.koenv.rentmycar.shared.repository.AuthState
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun App() {
    val themePreferences = remember { SharedModule.getThemePreferences() }
    val systemDarkTheme = isSystemInDarkTheme()
    
    // Observe dark mode preference changes
    val darkModePreference by themePreferences.darkModeFlow.collectAsState()
    val isDarkTheme = darkModePreference ?: systemDarkTheme
    
    AppTheme(isDarkTheme = isDarkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = AppTheme.colors.background
        ) {
            val authRepository = remember { SharedModule.authRepository }
            val authState by authRepository.authState.collectAsState()
            
            // Determine initial screen based on auth state
            // HomeScreen now includes bottom navigation
            val initialScreen = when (authState) {
                AuthState.Authenticated -> HomeScreen()
                else -> LoginScreen()
            }
            
            Navigator(initialScreen) { navigator ->
                // No transition animation for cleaner bottom navigation UX
                navigator.lastItem.Content()
            }
        }
    }
}

