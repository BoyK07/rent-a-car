package dev.koenv.rentmycar.app.screens.reservation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import dev.koenv.rentmycar.app.screens.admin.AdminScreen
import dev.koenv.rentmycar.app.screens.home.HomeScreen
import dev.koenv.rentmycar.app.screens.profile.ProfileScreen
import dev.koenv.rentmycar.app.ui.components.AppBottomNavigationBar
import dev.koenv.rentmycar.app.ui.components.BottomNavItem
import dev.koenv.rentmycar.app.ui.components.Button
import dev.koenv.rentmycar.app.ui.components.Text
import dev.koenv.rentmycar.app.ui.components.card.Card
import dev.koenv.rentmycar.shared.SharedModule
import dev.koenv.rentmycar.shared.domain.enums.ReservationStatus
import dev.koenv.rentmycar.shared.dto.reservation.ReservationDto
import kotlinx.coroutines.launch

/**
 * Screen displaying user's reservations.
 * Shows active, past, and cancelled reservations with filtering.
 */
class ReservationListScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val reservationRepository = remember { SharedModule.reservationRepository }
        val authRepository = remember { SharedModule.authRepository }
        
        var reservations by remember { mutableStateOf<List<ReservationDto>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var isRefreshing by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var selectedFilter by remember { mutableStateOf(0) } // 0=All, 1=Active, 2=Past, 3=Cancelled
        
        val scope = rememberCoroutineScope()
        val currentUser by authRepository.currentUser.collectAsState()
        val isAdmin = currentUser?.role?.name == "ADMIN"
        
        // Fetch reservations
        LaunchedEffect(selectedFilter) {
            scope.launch {
                isLoading = true
                errorMessage = null
                
                val result = when (selectedFilter) {
                    1 -> reservationRepository.getActiveReservations()
                    else -> {
                        val userId = currentUser?.id
                        if (userId != null) {
                            reservationRepository.getReservations(renterId = userId)
                        } else {
                            Result.failure(Exception("User not logged in"))
                        }
                    }
                }
                
                result.onSuccess { reservationList ->
                    // Apply client-side filtering
                    reservations = when (selectedFilter) {
                        2 -> reservationList.filter { it.status == ReservationStatus.COMPLETED }
                        3 -> reservationList.filter { it.status == ReservationStatus.CANCELLED }
                        else -> reservationList
                    }
                    isLoading = false
                }.onFailure { error ->
                    errorMessage = error.message ?: "Failed to load reservations"
                    isLoading = false
                }
            }
        }
        
        val refreshReservations: () -> Unit = {
            scope.launch {
                isRefreshing = true
                val userId = currentUser?.id
                if (userId != null) {
                    reservationRepository.getReservations(renterId = userId, forceRefresh = true)
                        .onSuccess { reservationList ->
                            reservations = when (selectedFilter) {
                                1 -> reservationList.filter { 
                                    it.status == ReservationStatus.PENDING || it.status == ReservationStatus.CONFIRMED
                                }
                                2 -> reservationList.filter { it.status == ReservationStatus.COMPLETED }
                                3 -> reservationList.filter { it.status == ReservationStatus.CANCELLED }
                                else -> reservationList
                            }
                            isRefreshing = false
                        }.onFailure {
                            isRefreshing = false
                        }
                }
            }
        }
        
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
                    title = { Text("My Reservations") },
                    actions = {
                        IconButton(
                            onClick = refreshReservations,
                            enabled = !isRefreshing
                        ) {
                            if (isRefreshing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Refresh, "Refresh")
                            }
                        }
                    }
                )
            },
            bottomBar = {
                AppBottomNavigationBar(
                    selectedIndex = 1, // Reservations is selected
                    onItemSelected = { index ->
                        when (navItems[index].route) {
                            "home" -> navigator.replaceAll(HomeScreen())
                            "reservations" -> { /* Already on reservations */ }
                            "profile" -> navigator.replaceAll(ProfileScreen())
                            "admin" -> navigator.replaceAll(AdminScreen())
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
            ) {
                // Filter tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedFilter,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedFilter == 0,
                        onClick = { selectedFilter = 0 },
                        text = { Text("All") }
                    )
                    Tab(
                        selected = selectedFilter == 1,
                        onClick = { selectedFilter = 1 },
                        text = { Text("Active") }
                    )
                    Tab(
                        selected = selectedFilter == 2,
                        onClick = { selectedFilter = 2 },
                        text = { Text("Past") }
                    )
                    Tab(
                        selected = selectedFilter == 3,
                        onClick = { selectedFilter = 3 },
                        text = { Text("Cancelled") }
                    )
                }
                
                // Content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
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
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = errorMessage ?: "Error",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = {
                                    isLoading = true
                                    errorMessage = null
                                    scope.launch {
                                        val userId = currentUser?.id
                                        if (userId != null) {
                                            reservationRepository.getReservations(renterId = userId)
                                                .onSuccess { reservationList ->
                                                    reservations = reservationList
                                                    isLoading = false
                                                }.onFailure { error ->
                                                    errorMessage = error.message ?: "Failed to load reservations"
                                                    isLoading = false
                                                }
                                        }
                                    }
                                }) {
                                    Text("Retry")
                                }
                            }
                        }
                        reservations.isEmpty() -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = when (selectedFilter) {
                                        1 -> "No active reservations"
                                        2 -> "No past reservations"
                                        3 -> "No cancelled reservations"
                                        else -> "No reservations yet"
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Browse cars and make a reservation!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                        }
                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(reservations) { reservation ->
                                    ReservationCard(
                                        reservation = reservation,
                                        onClick = {
                                            navigator.push(ReservationDetailScreen(reservation.id))
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReservationCard(
    reservation: ReservationDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reservation #${reservation.id.toString().take(8)}",
                    style = MaterialTheme.typography.titleMedium
                )
                StatusChip(status = reservation.status)
            }
            
            Divider()
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Column {
                    Text(
                        text = "Start: ${reservation.startTime}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "End: ${reservation.endTime}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total: €${reservation.priceTotal.roundSignificand(DecimalMode.US_CURRENCY).toPlainString()}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${reservation.pointsAwarded} points",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun StatusChip(status: ReservationStatus) {
    val (color, text) = when (status) {
        ReservationStatus.PENDING -> MaterialTheme.colorScheme.tertiary to "Pending"
        ReservationStatus.CONFIRMED -> MaterialTheme.colorScheme.primary to "Confirmed"
        ReservationStatus.CANCELLED -> MaterialTheme.colorScheme.error to "Cancelled"
        ReservationStatus.COMPLETED -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) to "Completed"
    }
    
    AssistChip(
        onClick = { },
        label = { Text(text) },
        colors = AssistChipDefaults.assistChipColors(
            labelColor = color
        )
    )
}
