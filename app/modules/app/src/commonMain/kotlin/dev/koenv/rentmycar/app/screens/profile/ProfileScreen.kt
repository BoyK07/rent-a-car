package dev.koenv.rentmycar.app.screens.profile

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.koenv.rentmycar.app.screens.admin.AdminScreen
import dev.koenv.rentmycar.app.screens.auth.LoginScreen
import dev.koenv.rentmycar.app.screens.home.HomeScreen
import dev.koenv.rentmycar.app.ui.AppTheme
import dev.koenv.rentmycar.app.ui.components.Button
import dev.koenv.rentmycar.app.ui.components.Icon
import dev.koenv.rentmycar.app.ui.components.IconButton
import dev.koenv.rentmycar.app.ui.components.IconButtonVariant
import dev.koenv.rentmycar.app.ui.components.Scaffold
import dev.koenv.rentmycar.app.ui.components.Text
import dev.koenv.rentmycar.app.ui.components.card.Card
import dev.koenv.rentmycar.app.ui.components.topbar.TopBar
import dev.koenv.rentmycar.app.ui.layout.MainLayoutBottomBar
import dev.koenv.rentmycar.shared.SharedModule
import dev.koenv.rentmycar.shared.dto.user.UserDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Profile screen displaying user information and settings.
 * Shows role, email, stored data count, and logout functionality.
 */
class ProfileScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val authRepository = remember { SharedModule.authRepository }
        val userRepository = remember { SharedModule.userRepository }
        val appDataStorage = remember { SharedModule.getAppDataStorage() }
        
        var user by remember { mutableStateOf<UserDto?>(null) }
        var isLoading by remember { mutableStateOf(true) }
        var isRefreshing by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var showLogoutDialog by remember { mutableStateOf(false) }
        
        val scope = rememberCoroutineScope()
        
        // Refresh function
        val refreshProfile = {
            scope.launch {
                isRefreshing = true
                authRepository.restoreUserSession().onSuccess { restoredUser ->
                    user = restoredUser
                    isRefreshing = false
                }.onFailure { error ->
                    isRefreshing = false
                    errorMessage = error.message
                }
            }
        }
        
        // Try to get user from auth state first
        LaunchedEffect(Unit) {
            val currentUser = authRepository.currentUser.value
            if (currentUser != null) {
                user = currentUser
                isLoading = false
            } else {
                // Try to restore user from token
                scope.launch {
                    authRepository.restoreUserSession().onSuccess { restoredUser ->
                        user = restoredUser
                        isLoading = false
                    }.onFailure {
                        errorMessage = "Failed to load user profile. Please log in again."
                        isLoading = false
                    }
                }
            }
        }
        
        val currentUser by authRepository.currentUser.collectAsState()
        
        val themePreferences = remember { SharedModule.getThemePreferences() }
        val darkModePreference by themePreferences.darkModeFlow.collectAsState()
        val systemDarkTheme = isSystemInDarkTheme()
        val isDarkMode = darkModePreference ?: systemDarkTheme
        
        // Logout dialog
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Logout") },
                text = { Text("Are you sure you want to logout?") },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            authRepository.logout()
                            navigator.replaceAll(LoginScreen())
                        }
                    }) {
                        Text("Logout")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        Scaffold(
            topBar = {
                TopBar {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Profile",
                            style = AppTheme.typography.titleLarge,
                            modifier = Modifier.weight(1f).padding(start = 8.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = { refreshProfile() },
                                enabled = !isRefreshing,
                                variant = IconButtonVariant.Ghost
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Refresh profile"
                                )
                            }
                            IconButton(
                                onClick = { showLogoutDialog = true },
                                variant = IconButtonVariant.Ghost
                            ) {
                                Icon(Icons.Filled.Logout, contentDescription = "Logout")
                            }
                        }
                    }
                }
            },
            bottomBar = {
                MainLayoutBottomBar(selectedRoute = "profile")
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    errorMessage != null -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                color = AppTheme.colors.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { navigator.pop() }) {
                                Text("Go Back")
                            }
                        }
                    }
                    user != null -> {
                        ProfileContent(
                            user = user!!,
                            carsCount = SharedModule.provideCarDao().getAllCars().size,
                            usersCount = SharedModule.provideUserDao().getAllUsers().size,
                            reservationsCount = SharedModule.provideReservationDao().getAllReservations().size,
                            viewedCarsCount = appDataStorage.getViewedCars().size,
                            isDarkMode = isDarkMode,
                            onDarkModeToggle = { enabled ->
                                themePreferences.setDarkMode(enabled)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileContent(
    user: UserDto,
    carsCount: Int,
    usersCount: Int,
    reservationsCount: Int,
    viewedCarsCount: Int,
    isDarkMode: Boolean,
    onDarkModeToggle: (Boolean) -> Unit
) {
    val navigator = LocalNavigator.currentOrThrow
    val scope = rememberCoroutineScope()
    var showClearDataDialog by remember { mutableStateOf(false) }
    
    // Clear data confirmation dialog
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear Local Data") },
            text = { Text("This will clear all locally cached cars, users, reservations, and viewed history. Your login will be preserved. Continue?") },
            confirmButton = {
                TextButton(onClick = {
                    showClearDataDialog = false
                    scope.launch {
                        // Clear database caches
                        SharedModule.provideCarDao().deleteAll()
                        SharedModule.provideUserDao().deleteAll()
                        SharedModule.provideReservationDao().deleteAll()
                        // Clear app data storage
                        SharedModule.getAppDataStorage().clearAll()
                    }
                }) {
                    Text("Clear Data")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User info card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.name,
                            style = AppTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = user.email,
                            style = AppTheme.typography.bodyLarge,
                            color = AppTheme.colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    IconButton(onClick = { navigator.push(EditProfileScreen()) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                    }
                }
            }
        }
        
        // Role card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Role",
                    style = AppTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                AssistChip(
                    onClick = { },
                    label = { Text(user.role.name) },
                    leadingIcon = {
                        when (user.role.name) {
                            "ADMIN" -> Text("👑")
                            "DRIVER" -> Text("🚗")
                            "MEMBER" -> Text("👤")
                            else -> Text("📋")
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = getRoleDescription(user.role.name),
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        
        // Theme settings card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Appearance",
                    style = AppTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Dark Mode",
                            style = AppTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isDarkMode) "Using dark theme" else "Using light theme",
                            style = AppTheme.typography.bodySmall,
                            color = AppTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = onDarkModeToggle
                    )
                }
            }
        }
        
        // Local data card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Local Data",
                        style = AppTheme.typography.titleLarge
                    )
                    IconButton(
                        onClick = { showClearDataDialog = true },
                        variant = IconButtonVariant.Ghost
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear Cache")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Cars Cached:")
                    Text(
                        text = carsCount.toString(),
                        style = AppTheme.typography.titleMedium,
                        color = AppTheme.colors.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Users Cached:")
                    Text(
                        text = usersCount.toString(),
                        style = AppTheme.typography.titleMedium,
                        color = AppTheme.colors.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Reservations Cached:")
                    Text(
                        text = reservationsCount.toString(),
                        style = AppTheme.typography.titleMedium,
                        color = AppTheme.colors.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Cars Viewed:")
                    Text(
                        text = viewedCarsCount.toString(),
                        style = AppTheme.typography.titleMedium,
                        color = AppTheme.colors.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total Cached:",
                        style = AppTheme.typography.titleMedium
                    )
                    Text(
                        text = "${carsCount + usersCount + reservationsCount} items",
                        style = AppTheme.typography.titleMedium,
                        color = AppTheme.colors.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "This data is stored locally on your device for offline access",
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        
        // Account actions card removed - logout is now only in TopAppBar
    }
}

private fun getRoleDescription(role: String): String {
    return when (role) {
        "ADMIN" -> "Full access to all system features and user management"
        "DRIVER" -> "Can rent cars and offer own cars for rent"
        "MEMBER" -> "Can browse and rent available cars"
        else -> "Standard user privileges"
    }
}
