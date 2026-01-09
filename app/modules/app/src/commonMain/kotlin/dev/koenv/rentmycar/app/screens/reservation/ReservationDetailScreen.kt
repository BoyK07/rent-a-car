package dev.koenv.rentmycar.app.screens.reservation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
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
import dev.koenv.rentmycar.app.ui.components.ButtonVariant
import dev.koenv.rentmycar.app.ui.components.Icon
import dev.koenv.rentmycar.app.ui.components.IconButton
import dev.koenv.rentmycar.app.ui.components.IconButtonVariant
import dev.koenv.rentmycar.app.ui.components.Scaffold
import dev.koenv.rentmycar.app.ui.components.Text
import dev.koenv.rentmycar.app.ui.components.card.Card
import dev.koenv.rentmycar.app.ui.components.topbar.TopBar
import dev.koenv.rentmycar.shared.SharedModule
import dev.koenv.rentmycar.shared.domain.enums.ReservationStatus
import dev.koenv.rentmycar.shared.dto.reservation.ReservationDto
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlin.uuid.Uuid

/**
 * Formats a LocalDateTime to a human-readable string.
 * Example: "Jan 12, 2026 at 10:00 AM"
 */
private fun formatDateTime(dateTime: LocalDateTime): String {
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
 * Screen showing details of a single reservation.
 * Allows cancellation of pending/confirmed reservations.
 */
data class ReservationDetailScreen(
    val reservationId: Uuid
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val reservationRepository = remember { SharedModule.reservationRepository }
        
        var reservation by remember { mutableStateOf<ReservationDto?>(null) }
        var isLoading by remember { mutableStateOf(true) }
        var isCancelling by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var showCancelDialog by remember { mutableStateOf(false) }
        
        val scope = rememberCoroutineScope()
        
        // Load reservation
        LaunchedEffect(reservationId) {
            scope.launch {
                reservationRepository.getReservation(reservationId).onSuccess { res ->
                    reservation = res
                    isLoading = false
                }.onFailure { error ->
                    errorMessage = error.message ?: "Failed to load reservation"
                    isLoading = false
                }
            }
        }
        
        // Cancel dialog
        if (showCancelDialog) {
            AlertDialog(
                onDismissRequest = { showCancelDialog = false },
                title = { Text("Cancel Reservation") },
                text = { Text("Are you sure you want to cancel this reservation? This action cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            isCancelling = true
                            showCancelDialog = false
                            scope.launch {
                                reservationRepository.cancelReservation(reservationId).onSuccess {
                                    // Reload reservation to show updated status
                                    reservationRepository.getReservation(reservationId).onSuccess { res ->
                                        reservation = res
                                        isCancelling = false
                                    }.onFailure {
                                        isCancelling = false
                                        navigator.pop()
                                    }
                                }.onFailure { error ->
                                    errorMessage = error.message ?: "Failed to cancel reservation"
                                    isCancelling = false
                                }
                            }
                        },
                        variant = ButtonVariant.Ghost
                    ) {
                        Text("Cancel Reservation")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showCancelDialog = false },
                        variant = ButtonVariant.Ghost
                    ) {
                        Text("Keep Reservation")
                    }
                }
            )
        }
        
        Scaffold(
            topBar = {
                TopBar {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { navigator.pop() },
                            variant = IconButtonVariant.Ghost
                        ) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Text(
                            text = "Reservation Details",
                            style = AppTheme.typography.titleLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    errorMessage != null && reservation == null -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = errorMessage!!, color = AppTheme.colors.error)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { navigator.pop() }) {
                                Text("Go Back")
                            }
                        }
                    }
                    reservation != null -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Status
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Status", style = AppTheme.typography.titleMedium)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val (color, statusText) = when (reservation!!.status) {
                                        ReservationStatus.PENDING -> AppTheme.colors.tertiary to "Pending"
                                        ReservationStatus.CONFIRMED -> AppTheme.colors.primary to "Confirmed"
                                        ReservationStatus.CANCELLED -> AppTheme.colors.error to "Cancelled"
                                        ReservationStatus.COMPLETED -> AppTheme.colors.onSurface.copy(alpha = 0.6f) to "Completed"
                                    }
                                    Text(statusText, color = color, style = AppTheme.typography.titleLarge)
                                }
                            }
                            
                            // Dates
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Rental Period", style = AppTheme.typography.titleMedium)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Start: ${formatDateTime(reservation!!.startTime)}", style = AppTheme.typography.bodyLarge)
                                    Text("End: ${formatDateTime(reservation!!.endTime)}", style = AppTheme.typography.bodyLarge)
                                }
                            }
                            
                            // Payment
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Payment", style = AppTheme.typography.titleMedium)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Total: €${reservation!!.priceTotal.roundSignificand(DecimalMode.US_CURRENCY).toPlainString()}",
                                        style = AppTheme.typography.headlineSmall,
                                        color = AppTheme.colors.primary)
                                    Text("Points Awarded: ${reservation!!.pointsAwarded}", 
                                        style = AppTheme.typography.bodyMedium)
                                }
                            }
                            
                            // Error message
                            if (errorMessage != null) {
                                Text(errorMessage!!, color = AppTheme.colors.error)
                            }
                            
                            // Cancel button (only for pending/confirmed)
                            if (reservation!!.status == ReservationStatus.PENDING || 
                                reservation!!.status == ReservationStatus.CONFIRMED) {
                                Button(
                                    onClick = { showCancelDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !isCancelling,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AppTheme.colors.error
                                    )
                                ) {
                                    if (isCancelling) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = AppTheme.colors.onError
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Text(if (isCancelling) "Cancelling..." else "Cancel Reservation")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
