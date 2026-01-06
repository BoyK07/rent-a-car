package dev.koenv.rentmycar.app.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.koenv.rentmycar.app.screens.admin.UserManagementScreen
import dev.koenv.rentmycar.app.screens.auth.LoginScreen
import dev.koenv.rentmycar.app.screens.home.HomeScreen
import dev.koenv.rentmycar.app.ui.components.AppBottomNavigationBar
import dev.koenv.rentmycar.app.ui.components.BottomNavItem
import dev.koenv.rentmycar.app.ui.components.Button
import dev.koenv.rentmycar.app.ui.components.Text
import dev.koenv.rentmycar.app.ui.components.card.Card
import dev.koenv.rentmycar.shared.SharedModule
import dev.koenv.rentmycar.shared.dto.user.UserDto
import kotlinx.coroutines.launch

/**
 * Profile screen displaying user information and settings.
 * Shows role, email, stored data count, and logout functionality.
 */
class ProfileScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val authRepository = remember { SharedModule.authRepository }
        val userRepository = remember { SharedModule.userRepository }
        val appDataStorage = remember { SharedModule.getAppDataStorage() }
        
        var user by remember { mutableStateOf<UserDto?>(null) }
        var isLoading by remember { mutableStateOf(true) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var showLogoutDialog by remember { mutableStateOf(false) }
        
        val scope = rememberCoroutineScope()
        
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
        val isAdmin = currentUser?.role?.name == "ADMIN"
        
        // Define bottom navigation items
        val navItems = remember(isAdmin) {
            buildList {
                add(BottomNavItem("Home", Icons.Default.Home, "home"))
                add(BottomNavItem("Reservations", Icons.Default.DateRange, "reservations"))
                add(BottomNavItem("Profile", Icons.Default.Person, "profile"))
                if (isAdmin) {
                    add(BottomNavItem("Admin", Icons.Default.Settings, "admin"))
                }
            }
        }
        
        // Logout dialog
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Logout") },
                text = { Text("Are you sure you want to logout?") },
                confirmButton = {
                    TextButton(onClick = {
                        authRepository.logout()
                        navigator.replaceAll(LoginScreen())
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
                TopAppBar(
                    title = { Text("Profile") },
                    actions = {
                        IconButton(onClick = { showLogoutDialog = true }) {
                            Icon(Icons.Filled.Logout, contentDescription = "Logout")
                        }
                    }
                )
            },
            bottomBar = {
                AppBottomNavigationBar(
                    selectedIndex = 2, // Profile is selected
                    onItemSelected = { index ->
                        when (navItems[index].route) {
                            "home" -> navigator.replaceAll(HomeScreen())
                            "reservations" -> {
                                // TODO: Navigate to reservations when implemented
                            }
                            "profile" -> { /* Already on profile */ }
                            "admin" -> navigator.push(UserManagementScreen())
                        }
                    },
                    items = navItems
                )
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
                                color = MaterialTheme.colorScheme.error
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
                            viewedCarsCount = appDataStorage.getViewedCars().size,
                            onLogout = { showLogoutDialog = true },
                            onNavigateToUserManagement = { 
                                navigator.push(UserManagementScreen())
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
    viewedCarsCount: Int,
    onLogout: () -> Unit,
    onNavigateToUserManagement: () -> Unit
) {
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
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
        
        // Role card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Role",
                    style = MaterialTheme.typography.titleLarge
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
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        
        // Local data card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Local Data",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Cars Viewed:")
                    Text(
                        text = viewedCarsCount.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "This data is stored locally on your device",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        
        // Admin section (only visible for ADMIN users)
        if (user.role.name == "ADMIN") {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Admin",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Button(
                        onClick = onNavigateToUserManagement,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Manage Users")
                    }
                }
            }
        }
        
        // Account actions
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Account",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Logout")
                }
            }
        }
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
