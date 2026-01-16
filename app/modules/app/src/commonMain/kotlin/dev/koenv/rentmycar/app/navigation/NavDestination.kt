package dev.koenv.rentmycar.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import cafe.adriel.voyager.core.screen.Screen
import dev.koenv.rentmycar.app.screens.admin.AdminScreen
import dev.koenv.rentmycar.app.screens.home.HomeScreen
import dev.koenv.rentmycar.app.screens.profile.ProfileScreen
import dev.koenv.rentmycar.app.screens.reservation.ReservationListScreen

/**
 * Navigation destination definitions for the application's bottom navigation bar.
 * 
 * Provides type-safe navigation targets with associated icons and titles.
 * Admin destination is conditionally available based on user role.
 */
sealed class NavDestination(
    val title: String,
    val icon: ImageVector,
    val screen: Screen
) {
    data object Home : NavDestination(
        title = "Home",
        icon = Icons.Default.Home,
        screen = HomeScreen()
    )
    
    data object Reservations : NavDestination(
        title = "Reservations",
        icon = Icons.Default.DateRange,
        screen = ReservationListScreen()
    )
    
    data object Profile : NavDestination(
        title = "Profile",
        icon = Icons.Default.Person,
        screen = ProfileScreen()
    )
    
    data object Admin : NavDestination(
        title = "Admin",
        icon = Icons.Default.Settings,
        screen = AdminScreen()
    )
    
    companion object {
        /**
         * Returns all available navigation destinations.
         * 
         * @param isAdmin Whether the user has admin role
         * @return List of navigation destinations, including Admin if user is admin
         */
        fun getDestinations(isAdmin: Boolean = false): List<NavDestination> {
            return buildList {
                add(Home)
                add(Reservations)
                add(Profile)
                if (isAdmin) {
                    add(Admin)
                }
            }
        }
    }
}
