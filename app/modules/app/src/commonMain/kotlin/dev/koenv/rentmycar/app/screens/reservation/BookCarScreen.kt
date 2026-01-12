package dev.koenv.rentmycar.app.screens.reservation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
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
import dev.koenv.rentmycar.app.ui.components.HorizontalDivider
import dev.koenv.rentmycar.app.ui.components.Icon
import dev.koenv.rentmycar.app.ui.components.IconButton
import dev.koenv.rentmycar.app.ui.components.IconButtonVariant
import dev.koenv.rentmycar.app.ui.components.Scaffold
import dev.koenv.rentmycar.app.ui.components.Text
import dev.koenv.rentmycar.app.ui.components.card.Card
import dev.koenv.rentmycar.app.ui.components.topbar.TopBar
import dev.koenv.rentmycar.app.util.formatDateTime
import dev.koenv.rentmycar.shared.SharedModule
import dev.koenv.rentmycar.shared.dto.car.CarDto
import dev.koenv.rentmycar.shared.dto.reservation.CreateReservationRequestDto
import dev.koenv.rentmycar.shared.dto.reservation.ReservationQuoteRequestDto
import dev.koenv.rentmycar.shared.dto.reservation.ReservationQuoteResponseDto
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlin.uuid.Uuid

/**
 * Screen for booking a car with availability check and quote preview.
 * Shows pricing, duration, and allows user to create a reservation.
 */
data class BookCarScreen(
    val carId: Uuid
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val carsRepository = remember { SharedModule.carsRepository }
        val reservationRepository = remember { SharedModule.reservationRepository }
        
        var car by remember { mutableStateOf<CarDto?>(null) }
        var quote by remember { mutableStateOf<ReservationQuoteResponseDto?>(null) }
        var isLoadingCar by remember { mutableStateOf(true) }
        var isLoadingQuote by remember { mutableStateOf(false) }
        var isCreatingReservation by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        
        // Default to booking for 2 hours starting from next hour
        val now = Clock.System.now()
        val nextHour = now.plus(1, DateTimeUnit.HOUR, TimeZone.UTC)
        val defaultStart = nextHour.toLocalDateTime(TimeZone.UTC).let {
            LocalDateTime(it.year, it.monthNumber, it.dayOfMonth, it.hour, 0, 0, 0)
        }
        val defaultEnd = defaultStart.toInstant(TimeZone.UTC).plus(2, DateTimeUnit.HOUR, TimeZone.UTC).toLocalDateTime(TimeZone.UTC)
        
        var startDateTime by remember { mutableStateOf(defaultStart) }
        var endDateTime by remember { mutableStateOf(defaultEnd) }
        
        val scope = rememberCoroutineScope()
        
        // Load car details
        LaunchedEffect(carId) {
            scope.launch {
                carsRepository.getCar(carId).onSuccess { carDto ->
                    car = carDto
                    isLoadingCar = false
                    
                    // Automatically get quote with default times
                    isLoadingQuote = true
                    val quoteRequest = ReservationQuoteRequestDto(
                        carId = carId,
                        startTime = startDateTime,
                        endTime = endDateTime
                    )
                    reservationRepository.getQuote(quoteRequest).onSuccess { quoteResponse ->
                        quote = quoteResponse
                        isLoadingQuote = false
                    }.onFailure { error ->
                        errorMessage = error.message ?: "Failed to get price quote"
                        isLoadingQuote = false
                    }
                }.onFailure { error ->
                    errorMessage = error.message ?: "Failed to load car details"
                    isLoadingCar = false
                }
            }
        }
        
        val updateQuote = {
            if (startDateTime < endDateTime) {
                isLoadingQuote = true
                errorMessage = null
                scope.launch {
                    val quoteRequest = ReservationQuoteRequestDto(
                        carId = carId,
                        startTime = startDateTime,
                        endTime = endDateTime
                    )
                    reservationRepository.getQuote(quoteRequest).onSuccess { quoteResponse ->
                        quote = quoteResponse
                        isLoadingQuote = false
                    }.onFailure { error ->
                        errorMessage = error.message ?: "Failed to get price quote"
                        quote = null
                        isLoadingQuote = false
                    }
                }
            }
        }
        
        val createReservation: () -> Unit = {
            isCreatingReservation = true
            errorMessage = null
            scope.launch {
                val request = CreateReservationRequestDto(
                    carId = carId,
                    startTime = startDateTime,
                    endTime = endDateTime
                )
                reservationRepository.createReservation(request).onSuccess { reservation ->
                    isCreatingReservation = false
                    // Navigate to reservation detail with reservation list in the back stack
                    navigator.replaceAll(ReservationListScreen())
                    navigator.push(ReservationDetailScreen(reservation.id))
                }.onFailure { error ->
                    errorMessage = error.message ?: "Failed to create reservation"
                    isCreatingReservation = false
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { navigator.pop() },
                            variant = IconButtonVariant.Ghost
                        ) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Text(
                            text = "Book Car",
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
                    isLoadingCar -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    errorMessage != null && car == null -> {
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
                    car != null -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Car info
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "${car!!.brand} ${car!!.model}",
                                        style = AppTheme.typography.headlineMedium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = car!!.category.name,
                                        style = AppTheme.typography.titleMedium,
                                        color = AppTheme.colors.onSurface.copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "€${car!!.ratePerHour.roundSignificand(DecimalMode.US_CURRENCY).toPlainString()}/hour",
                                        style = AppTheme.typography.titleLarge,
                                        color = AppTheme.colors.primary
                                    )
                                }
                            }
                            
                            // Time selection
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Rental Period",
                                        style = AppTheme.typography.titleLarge
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    // Start Time with adjustment buttons
                                    Text("Start Time", style = AppTheme.typography.bodyMedium)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = formatDateTime(startDateTime),
                                            style = AppTheme.typography.bodyLarge,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            // Decrease start time by 1 hour
                                            Button(
                                                onClick = {
                                                    val duration = endDateTime.toInstant(TimeZone.UTC) - startDateTime.toInstant(TimeZone.UTC)
                                                    startDateTime = startDateTime.toInstant(TimeZone.UTC)
                                                        .minus(1, DateTimeUnit.HOUR, TimeZone.UTC)
                                                        .toLocalDateTime(TimeZone.UTC)
                                                    endDateTime = startDateTime.toInstant(TimeZone.UTC).plus(duration).toLocalDateTime(TimeZone.UTC)
                                                    updateQuote()
                                                },
                                                variant = ButtonVariant.Secondary
                                            ) {
                                                Text("-1h")
                                            }
                                            // Increase start time by 1 hour
                                            Button(
                                                onClick = {
                                                    val duration = endDateTime.toInstant(TimeZone.UTC) - startDateTime.toInstant(TimeZone.UTC)
                                                    startDateTime = startDateTime.toInstant(TimeZone.UTC)
                                                        .plus(1, DateTimeUnit.HOUR, TimeZone.UTC)
                                                        .toLocalDateTime(TimeZone.UTC)
                                                    endDateTime = startDateTime.toInstant(TimeZone.UTC).plus(duration).toLocalDateTime(TimeZone.UTC)
                                                    updateQuote()
                                                },
                                                variant = ButtonVariant.Secondary
                                            ) {
                                                Text("+1h")
                                            }
                                            // Increase start time by 1 day
                                            Button(
                                                onClick = {
                                                    val duration = endDateTime.toInstant(TimeZone.UTC) - startDateTime.toInstant(TimeZone.UTC)
                                                    startDateTime = startDateTime.toInstant(TimeZone.UTC)
                                                        .plus(1, DateTimeUnit.DAY, TimeZone.UTC)
                                                        .toLocalDateTime(TimeZone.UTC)
                                                    endDateTime = startDateTime.toInstant(TimeZone.UTC).plus(duration).toLocalDateTime(TimeZone.UTC)
                                                    updateQuote()
                                                },
                                                variant = ButtonVariant.Secondary
                                            ) {
                                                Text("+1d")
                                            }
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text("End Time", style = AppTheme.typography.bodyMedium)
                                    Text(
                                        text = formatDateTime(endDateTime),
                                        style = AppTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    // Quick duration buttons using FilterChip
                                    // Calculate current duration in hours
                                    val currentDurationHours = remember(startDateTime, endDateTime) {
                                        val start = startDateTime.toInstant(TimeZone.UTC)
                                        val end = endDateTime.toInstant(TimeZone.UTC)
                                        val duration = end - start
                                        duration.inWholeHours
                                    }
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        FilterChip(
                                            selected = currentDurationHours == 2L,
                                            onClick = {
                                                endDateTime = startDateTime.toInstant(TimeZone.UTC)
                                                    .plus(2, DateTimeUnit.HOUR, TimeZone.UTC)
                                                    .toLocalDateTime(TimeZone.UTC)
                                                updateQuote()
                                            },
                                            label = { Text("2 hours") },
                                            modifier = Modifier.weight(1f)
                                        )
                                        FilterChip(
                                            selected = currentDurationHours == 4L,
                                            onClick = {
                                                endDateTime = startDateTime.toInstant(TimeZone.UTC)
                                                    .plus(4, DateTimeUnit.HOUR, TimeZone.UTC)
                                                    .toLocalDateTime(TimeZone.UTC)
                                                updateQuote()
                                            },
                                            label = { Text("4 hours") },
                                            modifier = Modifier.weight(1f)
                                        )
                                        FilterChip(
                                            selected = currentDurationHours == 24L,
                                            onClick = {
                                                endDateTime = startDateTime.toInstant(TimeZone.UTC)
                                                    .plus(1, DateTimeUnit.DAY, TimeZone.UTC)
                                                    .toLocalDateTime(TimeZone.UTC)
                                                updateQuote()
                                            },
                                            label = { Text("1 day") },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                            
                            // Quote display
                            if (isLoadingQuote) {
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            } else if (quote != null) {
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "Price Quote",
                                            style = AppTheme.typography.titleLarge
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Duration:", style = AppTheme.typography.bodyLarge)
                                            Text(
                                                "${quote!!.durationHours} hours",
                                                style = AppTheme.typography.bodyLarge
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Rate:", style = AppTheme.typography.bodyLarge)
                                            Text(
                                                "€${quote!!.ratePerHour.roundSignificand(DecimalMode.US_CURRENCY).toPlainString()}/hr",
                                                style = AppTheme.typography.bodyLarge
                                            )
                                        }
                                        
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Total:", style = AppTheme.typography.titleLarge)
                                            Text(
                                                "€${quote!!.totalPrice.roundSignificand(DecimalMode.US_CURRENCY).toPlainString()}",
                                                style = AppTheme.typography.headlineMedium,
                                                color = AppTheme.colors.primary
                                            )
                                        }
                                    }
                                }
                            }
                            
                            // Error message
                            if (errorMessage != null && car != null) {
                                Text(
                                    text = errorMessage!!,
                                    color = AppTheme.colors.error,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                            
                            // Create reservation button
                            Button(
                                onClick = createReservation,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isCreatingReservation && quote != null && errorMessage == null
                            ) {
                                if (isCreatingReservation) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = AppTheme.colors.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(if (isCreatingReservation) "Creating..." else "Create Reservation")
                            }
                            
                            // Info text
                            Text(
                                text = "Your reservation will be pending until confirmed by the car owner.",
                                style = AppTheme.typography.bodySmall,
                                color = AppTheme.colors.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
