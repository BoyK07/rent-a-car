package dev.koenv.rentmycar.app.screens.reservation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
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
import dev.koenv.rentmycar.shared.dto.car.CarAvailabilityDto
import dev.koenv.rentmycar.shared.dto.car.CarDto
import dev.koenv.rentmycar.shared.dto.reservation.CreateReservationRequestDto
import dev.koenv.rentmycar.shared.dto.reservation.ReservationQuoteRequestDto
import dev.koenv.rentmycar.shared.dto.reservation.ReservationQuoteResponseDto
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlin.time.Clock
import kotlin.uuid.Uuid

/**
 * Car booking screen with real-time price quotes and reservation creation.
 * 
 * Features:
 * - Car details display
 * - Date and time selection for booking period
 * - Real-time price quote calculation
 * - Quote breakdown (rate, hours, total cost)
 * - Quick duration selection chips (2h, 4h, 8h, 1 day)
 * - Create reservation with loading state
 * - Validation and error handling
 * - Navigation to reservation details after successful booking
 */
data class BookCarScreen(
    val carId: Uuid
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val carsRepository = remember { SharedModule.carsRepository }
        val reservationRepository = remember { SharedModule.reservationRepository }
        val availabilityApi = remember { SharedModule.carAvailabilityApi }
        
        var car by remember { mutableStateOf<CarDto?>(null) }
        var quote by remember { mutableStateOf<ReservationQuoteResponseDto?>(null) }
        var isLoadingCar by remember { mutableStateOf(true) }
        var isLoadingQuote by remember { mutableStateOf(false) }
        var isCreatingReservation by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var availabilityWindows by remember { mutableStateOf<List<CarAvailabilityDto>>(emptyList()) }
        var isLoadingAvailability by remember { mutableStateOf(false) }
        var availabilityError by remember { mutableStateOf<String?>(null) }
        
        val now = Clock.System.now()
        val nextHour = now.plus(1, DateTimeUnit.HOUR, TimeZone.UTC)
        val defaultStart = nextHour.toLocalDateTime(TimeZone.UTC).let {
            LocalDateTime(it.year, it.monthNumber, it.dayOfMonth, it.hour, 0, 0, 0)
        }
        val defaultEnd = defaultStart.toInstant(TimeZone.UTC).plus(2, DateTimeUnit.HOUR, TimeZone.UTC).toLocalDateTime(TimeZone.UTC)
        val scheduleWindowStart = LocalDateTime(
            defaultStart.year,
            defaultStart.monthNumber,
            defaultStart.dayOfMonth,
            0,
            0,
            0,
            0
        )
        
        var startDateTime by remember { mutableStateOf(defaultStart) }
        var endDateTime by remember { mutableStateOf(defaultEnd) }
        
        val scope = rememberCoroutineScope()
        
        LaunchedEffect(carId) {
            scope.launch {
                carsRepository.getCar(carId).onSuccess { carDto ->
                    car = carDto
                    isLoadingCar = false
                    
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

        LaunchedEffect(carId) {
            scope.launch {
                isLoadingAvailability = true
                availabilityError = null
                availabilityApi.getCarAvailability(carId).onSuccess { windows ->
                    availabilityWindows = windows
                    isLoadingAvailability = false
                }.onFailure { error ->
                    availabilityError = error.message ?: "Failed to load availability"
                    availabilityWindows = emptyList()
                    isLoadingAvailability = false
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

                            // Availability schedule
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Availability (next 14 days)",
                                        style = AppTheme.typography.titleLarge
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    AvailabilityLegend()

                                    when {
                                        isLoadingAvailability -> {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 16.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CircularProgressIndicator()
                                            }
                                        }
                                        availabilityError != null -> {
                                            Text(
                                                text = availabilityError ?: "",
                                                color = AppTheme.colors.error,
                                                style = AppTheme.typography.bodySmall
                                            )
                                        }
                                        availabilityWindows.isEmpty() -> {
                                            Text(
                                                text = "No availability windows have been set for this car yet.",
                                                color = AppTheme.colors.onSurface.copy(alpha = 0.6f),
                                                style = AppTheme.typography.bodySmall
                                            )
                                        }
                                        else -> {
                                            AvailabilitySchedule(
                                                availabilityWindows = availabilityWindows,
                                                windowStart = scheduleWindowStart,
                                                days = 14,
                                                slotHours = 2,
                                                now = now.toLocalDateTime(TimeZone.UTC)
                                            )
                                        }
                                    }
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

@Composable
private fun AvailabilityLegend() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AvailabilityLegendItem(label = "Available", color = AppTheme.colors.success)
        AvailabilityLegendItem(label = "Unavailable", color = AppTheme.colors.surfaceVariant)
        AvailabilityLegendItem(label = "Past", color = AppTheme.colors.disabled)
    }
}

@Composable
private fun AvailabilityLegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color = color, shape = RoundedCornerShape(3.dp))
                .border(1.dp, AppTheme.colors.outline, shape = RoundedCornerShape(3.dp))
        )
        Text(text = label, style = AppTheme.typography.bodySmall)
    }
}

@Composable
private fun AvailabilitySchedule(
    availabilityWindows: List<CarAvailabilityDto>,
    windowStart: LocalDateTime,
    days: Int,
    slotHours: Int,
    now: LocalDateTime
) {
    val slots = remember(slotHours) { (0 until 24 step slotHours).toList() }
    val dayStarts = remember(windowStart, days) {
        (0 until days).map { dayOffset ->
            windowStart.toInstant(TimeZone.UTC)
                .plus(dayOffset, DateTimeUnit.DAY, TimeZone.UTC)
                .toLocalDateTime(TimeZone.UTC)
        }
    }
    val scrollState = rememberScrollState()
    val labelWidth = 84.dp
    val slotWidth = 34.dp
    val slotHeight = 18.dp
    val slotShape = RoundedCornerShape(4.dp)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Day",
            style = AppTheme.typography.bodySmall,
            modifier = Modifier.width(labelWidth)
        )
        Row(modifier = Modifier.horizontalScroll(scrollState)) {
            slots.forEach { hour ->
                Text(
                    text = formatHourLabel(hour),
                    style = AppTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(slotWidth)
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(6.dp))

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        dayStarts.forEach { dayStart ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatDayLabel(dayStart),
                    style = AppTheme.typography.bodySmall,
                    modifier = Modifier.width(labelWidth)
                )
                Row(modifier = Modifier.horizontalScroll(scrollState)) {
                    slots.forEach { hour ->
                        val slotStart = dayStart.toInstant(TimeZone.UTC)
                            .plus(hour, DateTimeUnit.HOUR, TimeZone.UTC)
                            .toLocalDateTime(TimeZone.UTC)
                        val slotEnd = slotStart.toInstant(TimeZone.UTC)
                            .plus(slotHours, DateTimeUnit.HOUR, TimeZone.UTC)
                            .toLocalDateTime(TimeZone.UTC)
                        val isPast = slotEnd <= now
                        val isAvailable = availabilityWindows.any { window ->
                            window.startTime <= slotStart && window.endTime >= slotEnd
                        }
                        val color = when {
                            isPast -> AppTheme.colors.disabled
                            isAvailable -> AppTheme.colors.success
                            else -> AppTheme.colors.surfaceVariant
                        }

                        Box(
                            modifier = Modifier
                                .size(slotWidth, slotHeight)
                                .background(color, slotShape)
                                .border(1.dp, AppTheme.colors.outline, slotShape)
                        )
                    }
                }
            }
        }
    }
}

private fun formatDayLabel(dateTime: LocalDateTime): String {
    val date = LocalDate(dateTime.year, dateTime.monthNumber, dateTime.dayOfMonth)
    val dayName = when (date.dayOfWeek) {
        DayOfWeek.MONDAY -> "Mon"
        DayOfWeek.TUESDAY -> "Tue"
        DayOfWeek.WEDNESDAY -> "Wed"
        DayOfWeek.THURSDAY -> "Thu"
        DayOfWeek.FRIDAY -> "Fri"
        DayOfWeek.SATURDAY -> "Sat"
        DayOfWeek.SUNDAY -> "Sun"
        else -> "Day"
    }
    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val month = months[date.monthNumber - 1]
    return "$dayName ${date.dayOfMonth} $month"
}

private fun formatHourLabel(hour: Int): String {
    return hour.toString().padStart(2, '0')
}
