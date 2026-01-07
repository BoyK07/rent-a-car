package dev.koenv.rentmycar.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import cafe.adriel.voyager.core.screen.Screen
import dev.koenv.rentmycar.app.screens.admin.AdminScreen
import dev.koenv.rentmycar.app.screens.home.HomeScreen
import dev.koenv.rentmycar.app.screens.profile.ProfileScreen

/**
 * Navigation destinations for bottom navigation bar.
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
        screen = HomeScreen() // TODO: Replace with ReservationListScreen when implemented
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
         * Get all navigation destinations.
         * Admin destination is conditionally included based on user role.
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
