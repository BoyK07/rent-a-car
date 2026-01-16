package dev.koenv.rentmycar.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import dev.koenv.rentmycar.app.screens.auth.LoginScreen
import dev.koenv.rentmycar.app.screens.home.HomeScreen
import dev.koenv.rentmycar.app.ui.AppTheme
import dev.koenv.rentmycar.app.ui.components.Surface
import dev.koenv.rentmycar.app.util.createImageLoader
import dev.koenv.rentmycar.shared.SharedModule
import dev.koenv.rentmycar.shared.repository.AuthState
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Main application composable that sets up the root UI structure.
 * 
 * Responsibilities:
 * - Configures Coil3 image loading singleton
 * - Manages theme preferences (light/dark mode)
 * - Sets up navigation based on authentication state
 * - Provides root Surface with theme colors
 */
@Preview
@Composable
fun App() {
    setSingletonImageLoaderFactory { context ->
        createImageLoader(context)
    }
    
    val themePreferences = remember { SharedModule.getThemePreferences() }
    val systemDarkTheme = isSystemInDarkTheme()
    
    val darkModePreference by themePreferences.darkModeFlow.collectAsState()
    val isDarkTheme = darkModePreference ?: systemDarkTheme
    
    AppTheme(isDarkTheme = isDarkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = AppTheme.colors.background
        ) {
            val authRepository = remember { SharedModule.authRepository }
            val authState by authRepository.authState.collectAsState()
            
            val initialScreen = when (authState) {
                AuthState.Authenticated -> HomeScreen()
                else -> LoginScreen()
            }
            
            Navigator(initialScreen) { navigator ->
                navigator.lastItem.Content()
            }
        }
    }
}


