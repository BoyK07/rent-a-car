package dev.koenv.rentmycar.app.screens.reservation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import dev.koenv.rentmycar.app.ui.AppTheme
import dev.koenv.rentmycar.app.ui.components.Button
import dev.koenv.rentmycar.app.ui.components.Icon
import dev.koenv.rentmycar.app.ui.components.IconButton
import dev.koenv.rentmycar.app.ui.components.IconButtonVariant
import dev.koenv.rentmycar.app.ui.components.Scaffold
import dev.koenv.rentmycar.app.ui.components.Text
import dev.koenv.rentmycar.app.ui.components.topbar.TopBar
import dev.koenv.rentmycar.app.ui.components.card.Card
import dev.koenv.rentmycar.app.ui.layout.MainLayoutBottomBar
import dev.koenv.rentmycar.app.util.formatDateTime
import dev.koenv.rentmycar.shared.SharedModule
import dev.koenv.rentmycar.shared.domain.enums.ReservationStatus
import dev.koenv.rentmycar.shared.dto.reservation.ReservationDto
import kotlinx.coroutines.launch

/**
 * Screen displaying user's reservations as a renter with status-based filtering.
 * 
 * Features:
 * - Tab-based filtering (All, Active, Past, Cancelled)
 * - Reservation list with car details, dates, status, and pricing
 * - Clickable reservation items for detail view
 * - Loading and empty states
 * - Refresh capability
 * - Color-coded status indicators
 * - Sorted by start time (newest first)
 */
class ReservationListScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val reservationRepository = remember { SharedModule.reservationRepository }
        val authRepository = remember { SharedModule.authRepository }
        
        var reservations by remember { mutableStateOf<List<ReservationDto>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var isRefreshing by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var selectedFilter by remember { mutableStateOf(0) } // 0=All, 1=Pending, 2=Active, 3=Past, 4=Cancelled
        
        val scope = rememberCoroutineScope()
        val currentUser by authRepository.currentUser.collectAsState()
        
        // Fetch reservations where user is the renter
        val loadReservations: (Boolean) -> Unit = { forceRefresh ->
            scope.launch {
                if (forceRefresh) {
                    isRefreshing = true
                } else {
                    isLoading = true
                }
                errorMessage = null
                
                val userId = currentUser?.id
                if (userId == null) {
                    errorMessage = "User not logged in"
                    isLoading = false
                    isRefreshing = false
                    return@launch
                }
                
                // Always use forceRefresh=true to bypass shared cache
                reservationRepository.getReservations(renterId = userId, forceRefresh = true)
                    .onSuccess { allReservations ->
                        // Apply filtering
                        reservations = when (selectedFilter) {
                            1 -> allReservations.filter { it.status == ReservationStatus.PENDING }
                            2 -> allReservations.filter { 
                                it.status == ReservationStatus.PENDING || it.status == ReservationStatus.CONFIRMED
                            }
                            3 -> allReservations.filter { it.status == ReservationStatus.COMPLETED }
                            4 -> allReservations.filter { it.status == ReservationStatus.CANCELLED }
                            else -> allReservations
                        }
                    }.onFailure { error ->
                        errorMessage = error.message ?: "Failed to load reservations"
                    }
                
                isLoading = false
                isRefreshing = false
            }
        }
        
        // Load on mount and when filter changes
        LaunchedEffect(selectedFilter) {
            loadReservations(false)
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
                            text = "My Bookings",
                            style = AppTheme.typography.titleLarge,
                            modifier = Modifier.weight(1f).padding(start = 8.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = { navigator.push(MyCarReservationsScreen()) },
                                variant = IconButtonVariant.Ghost
                            ) {
                                Icon(Icons.Default.DirectionsCar, contentDescription = "My Car Reservations")
                            }
                            IconButton(
                                onClick = { loadReservations(true) },
                                enabled = !isRefreshing,
                                variant = IconButtonVariant.Ghost
                            ) {
                                if (isRefreshing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                                }
                            }
                        }
                    }
                }
            },
            bottomBar = {
                MainLayoutBottomBar(selectedRoute = "reservations")
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Filter tabs
                PrimaryScrollableTabRow(
                    selectedTabIndex = selectedFilter,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = AppTheme.colors.background,
                    contentColor = AppTheme.colors.onBackground,
                    indicator = {
                        TabRowDefaults.PrimaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(selectedFilter),
                            color = AppTheme.colors.primary
                        )
                    }
                ) {
                    Tab(
                        selected = selectedFilter == 0,
                        onClick = { selectedFilter = 0 },
                        text = { Text("All", color = if (selectedFilter == 0) AppTheme.colors.primary else AppTheme.colors.onBackground) }
                    )
                    Tab(
                        selected = selectedFilter == 1,
                        onClick = { selectedFilter = 1 },
                        text = { Text("Pending", color = if (selectedFilter == 1) AppTheme.colors.primary else AppTheme.colors.onBackground) }
                    )
                    Tab(
                        selected = selectedFilter == 2,
                        onClick = { selectedFilter = 2 },
                        text = { Text("Active", color = if (selectedFilter == 2) AppTheme.colors.primary else AppTheme.colors.onBackground) }
                    )
                    Tab(
                        selected = selectedFilter == 3,
                        onClick = { selectedFilter = 3 },
                        text = { Text("Past", color = if (selectedFilter == 3) AppTheme.colors.primary else AppTheme.colors.onBackground) }
                    )
                    Tab(
                        selected = selectedFilter == 4,
                        onClick = { selectedFilter = 4 },
                        text = { Text("Cancelled", color = if (selectedFilter == 4) AppTheme.colors.primary else AppTheme.colors.onBackground) }
                    )
                }
                
                // Divider below tabs
                Divider(
                    modifier = Modifier.fillMaxWidth(),
                    color = AppTheme.colors.outline
                )
                
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
                                    style = AppTheme.typography.bodyLarge,
                                    color = AppTheme.colors.error
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { loadReservations(false) }) {
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
                                    tint = AppTheme.colors.onSurface.copy(alpha = 0.3f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = when (selectedFilter) {
                                        1 -> "No pending reservations"
                                        2 -> "No active reservations"
                                        3 -> "No past reservations"
                                        4 -> "No cancelled reservations"
                                        else -> "No reservations yet"
                                    },
                                    style = AppTheme.typography.bodyLarge,
                                    color = AppTheme.colors.onSurface.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Browse cars and make a reservation!",
                                    style = AppTheme.typography.bodySmall,
                                    color = AppTheme.colors.onSurface.copy(alpha = 0.4f)
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
                    style = AppTheme.typography.titleMedium
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
                    tint = AppTheme.colors.onSurface.copy(alpha = 0.6f)
                )
                Column {
                    Text(
                        text = "Start: ${formatDateTime(reservation.startTime)}",
                        style = AppTheme.typography.bodyMedium
                    )
                    Text(
                        text = "End: ${formatDateTime(reservation.endTime)}",
                        style = AppTheme.typography.bodyMedium
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
                    style = AppTheme.typography.titleMedium,
                    color = AppTheme.colors.primary
                )
                Text(
                    text = "${reservation.pointsAwarded} points",
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun StatusChip(status: ReservationStatus) {
    val (color, text) = when (status) {
        ReservationStatus.PENDING -> AppTheme.colors.tertiary to "Pending"
        ReservationStatus.CONFIRMED -> AppTheme.colors.primary to "Confirmed"
        ReservationStatus.CANCELLED -> AppTheme.colors.error to "Cancelled"
        ReservationStatus.COMPLETED -> AppTheme.colors.onSurface.copy(alpha = 0.6f) to "Completed"
    }
    
    AssistChip(
        onClick = { },
        label = { Text(text) },
        colors = AssistChipDefaults.assistChipColors(
            labelColor = color
        )
    )
}
