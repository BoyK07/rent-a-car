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
import dev.koenv.rentmycar.app.screens.admin.AdminScreen
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
                Column {
                    TopAppBar(
                        title = { Text("Profile") },
                        actions = {
                            IconButton(
                                onClick = { refreshProfile() },
                                enabled = !isRefreshing
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Refresh profile"
                                )
                            }
                            IconButton(onClick = { showLogoutDialog = true }) {
                                Icon(Icons.Filled.Logout, contentDescription = "Logout")
                            }
                        }
                    )
                }
            },
            bottomBar = {
                AppBottomNavigationBar(
                    selectedIndex = 2, // Profile is selected
                    onItemSelected = { index ->
                        when (navItems[index].route) {
                            "home" -> navigator.replaceAll(HomeScreen())
                            "reservations" -> navigator.replaceAll(dev.koenv.rentmycar.app.screens.reservation.ReservationListScreen())
                            "profile" -> { /* Already on profile */ }
                            "admin" -> navigator.replaceAll(AdminScreen())
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
                            carsCount = SharedModule.provideCarDao().getAllCars().size,
                            usersCount = SharedModule.provideUserDao().getAllUsers().size,
                            reservationsCount = SharedModule.provideReservationDao().getAllReservations().size,
                            viewedCarsCount = appDataStorage.getViewedCars().size
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
    viewedCarsCount: Int
) {
    val navigator = LocalNavigator.currentOrThrow
    
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
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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
                    Text(text = "Cars Cached:")
                    Text(
                        text = carsCount.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
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
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
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
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
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
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
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
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${carsCount + usersCount + reservationsCount} items",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "This data is stored locally on your device for offline access",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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
