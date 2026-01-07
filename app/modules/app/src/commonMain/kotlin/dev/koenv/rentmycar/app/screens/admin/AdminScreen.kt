package dev.koenv.rentmycar.app.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.koenv.rentmycar.app.screens.home.HomeScreen
import dev.koenv.rentmycar.app.screens.profile.ProfileScreen
import dev.koenv.rentmycar.app.ui.components.AppBottomNavigationBar
import dev.koenv.rentmycar.app.ui.components.BottomNavItem
import dev.koenv.rentmycar.app.ui.components.Text
import dev.koenv.rentmycar.app.ui.components.card.Card
import dev.koenv.rentmycar.shared.SharedModule

/**
 * Admin screen showing available admin functions.
 * Accessible only to users with ADMIN role.
 */
class AdminScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val authRepository = remember { SharedModule.authRepository }
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
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Admin Dashboard") }
                )
            },
            bottomBar = {
                AppBottomNavigationBar(
                    selectedIndex = 3, // Admin is selected
                    onItemSelected = { index ->
                        when (navItems[index].route) {
                            "home" -> navigator.replaceAll(HomeScreen())
                            "reservations" -> {
                                // TODO: Navigate to reservations when implemented
                            }
                            "profile" -> navigator.replaceAll(ProfileScreen())
                            "admin" -> { /* Already on admin */ }
                        }
                    },
                    items = navItems
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Admin Functions",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "Select an admin function:",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // User Management Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        navigator.push(UserManagementScreen())
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "User Management",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "View, edit, and manage user accounts",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                
                // Placeholder for future admin features
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "More Admin Features",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Coming soon...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}
