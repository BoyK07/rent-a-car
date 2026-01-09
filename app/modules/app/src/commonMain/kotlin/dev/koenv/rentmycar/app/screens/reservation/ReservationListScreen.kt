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
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.runtime.*
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
import dev.koenv.rentmycar.shared.SharedModule
import dev.koenv.rentmycar.shared.domain.enums.ReservationStatus
import dev.koenv.rentmycar.shared.dto.reservation.ReservationDto
import kotlinx.coroutines.launch

/**
 * Formats a LocalDateTime to a human-readable string.
 * Example: "Jan 12, 2026 at 10:00 AM"
 */
private fun formatDateTime(dateTime: kotlinx.datetime.LocalDateTime): String {
    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val month = months[dateTime.monthNumber - 1]
    val day = dateTime.dayOfMonth
    val year = dateTime.year
    
    val hour = if (dateTime.hour == 0) 12 else if (dateTime.hour > 12) dateTime.hour - 12 else dateTime.hour
    val minute = dateTime.minute.toString().padStart(2, '0')
    val amPm = if (dateTime.hour < 12) "AM" else "PM"
    
    return "$month $day, $year at $hour:$minute $amPm"
}

/**
 * Screen displaying user's reservations.
 * Shows active, past, and cancelled reservations with filtering.
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
        var selectedFilter by remember { mutableStateOf(0) } // 0=All, 1=Active, 2=Past, 3=Cancelled
        
        val scope = rememberCoroutineScope()
        val currentUser by authRepository.currentUser.collectAsState()
        
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
                            text = "My Reservations",
                            style = AppTheme.typography.titleLarge,
                            modifier = Modifier.weight(1f).padding(start = 8.dp)
                        )
                        IconButton(
                            onClick = refreshReservations,
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
                        text = { Text("Active", color = if (selectedFilter == 1) AppTheme.colors.primary else AppTheme.colors.onBackground) }
                    )
                    Tab(
                        selected = selectedFilter == 2,
                        onClick = { selectedFilter = 2 },
                        text = { Text("Past", color = if (selectedFilter == 2) AppTheme.colors.primary else AppTheme.colors.onBackground) }
                    )
                    Tab(
                        selected = selectedFilter == 3,
                        onClick = { selectedFilter = 3 },
                        text = { Text("Cancelled", color = if (selectedFilter == 3) AppTheme.colors.primary else AppTheme.colors.onBackground) }
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
                                    tint = AppTheme.colors.onSurface.copy(alpha = 0.3f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = when (selectedFilter) {
                                        1 -> "No active reservations"
                                        2 -> "No past reservations"
                                        3 -> "No cancelled reservations"
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
