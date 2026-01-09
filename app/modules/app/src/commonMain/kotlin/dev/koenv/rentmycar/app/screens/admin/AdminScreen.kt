package dev.koenv.rentmycar.app.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.koenv.rentmycar.app.ui.AppTheme
import dev.koenv.rentmycar.app.ui.components.Icon
import dev.koenv.rentmycar.app.ui.components.IconButton
import dev.koenv.rentmycar.app.ui.components.Scaffold
import dev.koenv.rentmycar.app.ui.components.Text
import dev.koenv.rentmycar.app.ui.components.topbar.TopBar
import dev.koenv.rentmycar.app.ui.components.card.Card
import dev.koenv.rentmycar.app.ui.layout.MainLayoutBottomBar
import dev.koenv.rentmycar.shared.SharedModule

/**
 * Admin screen showing available admin functions.
 * Accessible only to users with ADMIN role.
 */
class AdminScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val authRepository = remember { SharedModule.authRepository }
        
        Scaffold(
            topBar = {
                TopBar {
                    Text(
                        text = "Admin Dashboard",
                        style = AppTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            },
            bottomBar = {
                MainLayoutBottomBar(selectedRoute = "admin")
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
                    style = AppTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "Select an admin function:",
                    style = AppTheme.typography.bodyLarge,
                    color = AppTheme.colors.onSurface.copy(alpha = 0.7f),
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
                            tint = AppTheme.colors.primary
                        )
                        Column {
                            Text(
                                text = "User Management",
                                style = AppTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "View, edit, and manage user accounts",
                                style = AppTheme.typography.bodyMedium,
                                color = AppTheme.colors.onSurface.copy(alpha = 0.7f)
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
                            style = AppTheme.typography.titleMedium,
                            color = AppTheme.colors.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Coming soon...",
                            style = AppTheme.typography.bodySmall,
                            color = AppTheme.colors.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}
