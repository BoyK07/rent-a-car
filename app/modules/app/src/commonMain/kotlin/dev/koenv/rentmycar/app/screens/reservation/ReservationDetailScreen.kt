package dev.koenv.rentmycar.app.screens.reservation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.SubcomposeAsyncImage
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import dev.koenv.rentmycar.app.ui.AppTheme
import dev.koenv.rentmycar.app.ui.components.*
import dev.koenv.rentmycar.app.ui.components.card.Card
import dev.koenv.rentmycar.app.ui.components.topbar.TopBar
import dev.koenv.rentmycar.app.util.formatDateTime
import dev.koenv.rentmycar.shared.SharedModule
import dev.koenv.rentmycar.shared.domain.enums.ReservationStatus
import dev.koenv.rentmycar.shared.domain.enums.Role
import dev.koenv.rentmycar.shared.dto.car.CarDto
import dev.koenv.rentmycar.shared.dto.car.CarPhotoDto
import dev.koenv.rentmycar.shared.dto.reservation.ReservationDto
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

/**
 * Detailed view of a single reservation with cancellation capability.
 * 
 * Features:
 * - Complete reservation information (status, dates, car, pricing)
 * - Car details with photo display
 * - Status-specific actions (cancel for pending/confirmed)
 * - Cancellation confirmation dialog
 * - Role-based visibility (renter can cancel, owner can view)
 * - Loading states for reservation and car data
 * - Error handling with retry option
 * - Color-coded status badges
 */
data class ReservationDetailScreen(
    val reservationId: Uuid
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val reservationRepository = remember { SharedModule.reservationRepository }
        val carsRepository = remember { SharedModule.carsRepository }
        val authRepository = remember { SharedModule.authRepository }

        var reservation by remember { mutableStateOf<ReservationDto?>(null) }
        var car by remember { mutableStateOf<CarDto?>(null) }
        var carPhotos by remember { mutableStateOf<List<CarPhotoDto>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var isCancelling by remember { mutableStateOf(false) }
        var isConfirming by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var showCancelDialog by remember { mutableStateOf(false) }
        var showConfirmDialog by remember { mutableStateOf(false) }

        val currentUser by authRepository.currentUser.collectAsState()

        // Check if current user is the car owner
        var isCarOwner by remember { mutableStateOf(false) }

        val scope = rememberCoroutineScope()

        // Load reservation
        LaunchedEffect(reservationId) {
            scope.launch {
                reservationRepository.getReservation(reservationId).onSuccess { res ->
                    reservation = res

                    // Load car details
                    carsRepository.getCar(res.carId).onSuccess { carDto ->
                        car = carDto
                        isCarOwner = currentUser?.id == carDto.ownerId || currentUser?.role == Role.ADMIN

                        // Load car photos
                        SharedModule.carPhotoApi.getCarPhotosByCarId(carDto.id).onSuccess { photos ->
                            carPhotos = photos
                        }.onFailure {
                            // Photos are optional, continue without them
                        }
                    }.onFailure {
                        // Car details failed, but continue showing reservation
                    }

                    isLoading = false
                }.onFailure { error ->
                    errorMessage = error.message ?: "Failed to load reservation"
                    isLoading = false
                }
            }
        }

        // Confirm dialog
        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = { Text("Confirm Reservation") },
                text = { Text("Are you sure you want to confirm this reservation? The renter will be notified.") },
                confirmButton = {
                    Button(
                        onClick = {
                            isConfirming = true
                            showConfirmDialog = false
                            scope.launch {
                                reservationRepository.confirmReservation(reservationId).onSuccess { confirmed ->
                                    reservation = confirmed
                                    isConfirming = false
                                }.onFailure { error ->
                                    errorMessage = error.message ?: "Failed to confirm reservation"
                                    isConfirming = false
                                }
                            }
                        }
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showConfirmDialog = false },
                        variant = ButtonVariant.Ghost
                    ) {
                        Text("Cancel")
                    }
                }
            )
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
                            // Car Information
                            if (car != null) {
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Car Details", style = AppTheme.typography.titleMedium)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "${car!!.brand} ${car!!.model}",
                                            style = AppTheme.typography.headlineSmall,
                                            color = AppTheme.colors.primary
                                        )
                                        Text(
                                            text = car!!.category.name,
                                            style = AppTheme.typography.bodyMedium,
                                            color = AppTheme.colors.onSurface.copy(alpha = 0.7f)
                                        )
                                        if (car!!.fuelType != null) {
                                            Text(
                                                text = "Fuel: ${car!!.fuelType!!.name}",
                                                style = AppTheme.typography.bodyMedium,
                                                color = AppTheme.colors.onSurface.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }

                            // Car Photos
                            if (carPhotos.isNotEmpty()) {
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Car Photos", style = AppTheme.typography.titleMedium)
                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Display primary photo or first photo using Coil3
                                        val primaryPhoto = carPhotos.firstOrNull { it.isPrimary } ?: carPhotos.first()

                                        key(primaryPhoto.url) {
                                            SubcomposeAsyncImage(
                                                model = primaryPhoto.url,
                                                contentDescription = "Car photo",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(200.dp),
                                                contentScale = ContentScale.Crop,
                                                loading = {
                                                    Box(
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        CircularProgressIndicator()
                                                    }
                                                },
                                                error = {
                                                    Box(
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Column(
                                                            horizontalAlignment = Alignment.CenterHorizontally,
                                                            verticalArrangement = Arrangement.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Filled.Image,
                                                                contentDescription = "Car photo",
                                                                modifier = Modifier.size(64.dp),
                                                                tint = AppTheme.colors.onSurface.copy(alpha = 0.3f)
                                                            )
                                                            Spacer(modifier = Modifier.height(8.dp))
                                                            Text(
                                                                text = "Image not available",
                                                                style = AppTheme.typography.bodyMedium,
                                                                color = AppTheme.colors.onSurface.copy(alpha = 0.6f)
                                                            )
                                                        }
                                                    }
                                                }
                                            )
                                        }

                                        if (carPhotos.size > 1) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "+${carPhotos.size - 1} more photo${if (carPhotos.size > 2) "s" else ""}",
                                                style = AppTheme.typography.bodySmall,
                                                color = AppTheme.colors.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }
                            }

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

                                    // Show message if pending and user is NOT the car owner
                                    if (reservation!!.status == ReservationStatus.PENDING && !isCarOwner) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Waiting for car owner to confirm your reservation",
                                            style = AppTheme.typography.bodyMedium,
                                            color = AppTheme.colors.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }

                            // Dates
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Rental Period", style = AppTheme.typography.titleMedium)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Start: ${formatDateTime(reservation!!.startTime)}",
                                        style = AppTheme.typography.bodyLarge
                                    )
                                    Text(
                                        "End: ${formatDateTime(reservation!!.endTime)}",
                                        style = AppTheme.typography.bodyLarge
                                    )
                                }
                            }

                            // Payment
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Payment", style = AppTheme.typography.titleMedium)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Total: €${
                                            reservation!!.priceTotal.roundSignificand(DecimalMode.US_CURRENCY)
                                                .toPlainString()
                                        }",
                                        style = AppTheme.typography.headlineSmall,
                                        color = AppTheme.colors.primary
                                    )
                                    Text(
                                        "Points Awarded: ${reservation!!.pointsAwarded}",
                                        style = AppTheme.typography.bodyMedium
                                    )
                                }
                            }

                            // Error message
                            if (errorMessage != null) {
                                Text(errorMessage!!, color = AppTheme.colors.error)
                            }

                            // Confirm button (only for car owner and pending reservations)
                            if (isCarOwner && reservation!!.status == ReservationStatus.PENDING) {
                                Button(
                                    onClick = { showConfirmDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !isConfirming,
                                    variant = ButtonVariant.Primary,
                                    loading = isConfirming
                                ) {
                                    Text(if (isConfirming) "Confirming..." else "Confirm Reservation")
                                }
                            }

                            // Cancel button (only for pending/confirmed)
                            if (reservation!!.status == ReservationStatus.PENDING ||
                                reservation!!.status == ReservationStatus.CONFIRMED
                            ) {
                                Button(
                                    onClick = { showCancelDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !isCancelling,
                                    variant = ButtonVariant.Destructive,
                                    loading = isCancelling
                                ) {
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
