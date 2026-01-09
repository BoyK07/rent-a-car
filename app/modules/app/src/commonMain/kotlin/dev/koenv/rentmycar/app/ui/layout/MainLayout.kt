package dev.koenv.rentmycar.app.ui.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.koenv.rentmycar.app.navigation.NavDestination
import dev.koenv.rentmycar.app.ui.components.Icon
import dev.koenv.rentmycar.app.ui.components.NavigationBar
import dev.koenv.rentmycar.app.ui.components.NavigationBarItem
import dev.koenv.rentmycar.app.ui.components.NavigationBarItemDefaults
import dev.koenv.rentmycar.app.ui.components.Text
import dev.koenv.rentmycar.shared.SharedModule

/**
 * Main layout component that provides bottom navigation for authenticated screens.
 * This centralizes the navigation logic and removes duplication across screens.
 * 
 * @param selectedRoute The route of the currently selected screen
 */
@Composable
fun MainLayoutBottomBar(
    selectedRoute: String
) {
    val navigator = LocalNavigator.currentOrThrow
    val authRepository = remember { SharedModule.authRepository }
    val currentUser by authRepository.currentUser.collectAsState()
    val isAdmin = currentUser?.role?.name == "ADMIN"
    
    // Get navigation destinations based on user role
    val navItems = remember(isAdmin) {
        NavDestination.getDestinations(isAdmin)
    }
    
    // Map route string to destination route identifier
    val routeMap = mapOf(
        "home" to NavDestination.Home,
        "reservations" to NavDestination.Reservations,
        "profile" to NavDestination.Profile,
        "admin" to NavDestination.Admin
    )
    
    // Find the selected index based on the current route
    val selectedDestination = routeMap[selectedRoute]
    val selectedIndex = navItems.indexOf(selectedDestination)
    
    NavigationBar {
        navItems.forEachIndexed { index, destination ->
            NavigationBarItem(
                selected = selectedIndex == index,
                onClick = {
                    if (selectedIndex != index) {
                        navigator.replaceAll(destination.screen)
                    }
                },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.title
                    )
                },
                label = { Text(destination.title) },
                colors = NavigationBarItemDefaults.colors()
            )
        }
    }
}
