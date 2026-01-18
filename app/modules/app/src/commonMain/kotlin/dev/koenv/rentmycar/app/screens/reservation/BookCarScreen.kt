package dev.koenv.rentmycar.app.screens.reservation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
    @OptIn(ExperimentalMaterial3Api::class)
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
        
        val now = Clock.System.now()
        val nextHour = now.plus(1, DateTimeUnit.HOUR, TimeZone.UTC)
        val defaultStart = nextHour.toLocalDateTime(TimeZone.UTC).let {
            LocalDateTime(it.year, it.monthNumber, it.dayOfMonth, it.hour, 0, 0, 0)
        }
        val defaultEnd = defaultStart.toInstant(TimeZone.UTC).plus(2, DateTimeUnit.HOUR, TimeZone.UTC).toLocalDateTime(TimeZone.UTC)
        
        var startDateTime by remember { mutableStateOf(defaultStart) }
        var endDateTime by remember { mutableStateOf(defaultEnd) }
        var validationMessage by remember { mutableStateOf<String?>(null) }

        var showStartDatePicker by remember { mutableStateOf(false) }
        var showEndDatePicker by remember { mutableStateOf(false) }
        var showStartTimeMenu by remember { mutableStateOf(false) }
        var showEndTimeMenu by remember { mutableStateOf(false) }

        val timeOptions = remember {
            buildList {
                for (hour in 0..23) {
                    for (minute in listOf(0, 15, 30, 45)) {
                        add(hour to minute)
                    }
                }
            }
        }

        fun formatTime(hour: Int, minute: Int): String =
            "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"

        val pickerFieldColors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppTheme.colors.primary,
            unfocusedBorderColor = AppTheme.colors.outline,
            focusedLabelColor = AppTheme.colors.primary,
            unfocusedLabelColor = AppTheme.colors.onSurfaceVariant,
            cursorColor = AppTheme.colors.primary,
            disabledBorderColor = AppTheme.colors.outline,
            disabledLabelColor = AppTheme.colors.onSurfaceVariant,
            disabledTextColor = AppTheme.colors.onSurface,
            disabledTrailingIconColor = AppTheme.colors.onSurfaceVariant
        )

        fun updateStartKeepingDuration(newStart: LocalDateTime) {
            val duration = endDateTime.toInstant(TimeZone.UTC) - startDateTime.toInstant(TimeZone.UTC)
            startDateTime = newStart
            endDateTime = newStart.toInstant(TimeZone.UTC).plus(duration).toLocalDateTime(TimeZone.UTC)
        }
        
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
        
        val updateQuote: () -> Unit = updateQuote@{
            if (startDateTime >= endDateTime) {
                validationMessage = "End time must be after start time"
                quote = null
                isLoadingQuote = false
                return@updateQuote
            }

            validationMessage = null
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

                                    // Start date picker dialog
                                    if (showStartDatePicker) {
                                        val startDatePickerState = rememberDatePickerState(
                                            initialSelectedDateMillis = startDateTime.date
                                                .atStartOfDayIn(TimeZone.UTC)
                                                .toEpochMilliseconds()
                                        )
                                        DatePickerDialog(
                                            onDismissRequest = { showStartDatePicker = false },
                                            confirmButton = {
                                                TextButton(
                                                    onClick = {
                                                        val millis = startDatePickerState.selectedDateMillis
                                                        if (millis != null) {
                                                            val selectedDate = Instant.fromEpochMilliseconds(millis)
                                                                .toLocalDateTime(TimeZone.UTC)
                                                                .date
                                                            updateStartKeepingDuration(
                                                                LocalDateTime(
                                                                    selectedDate.year,
                                                                    selectedDate.monthNumber,
                                                                    selectedDate.dayOfMonth,
                                                                    startDateTime.hour,
                                                                    startDateTime.minute,
                                                                    0,
                                                                    0
                                                                )
                                                            )
                                                            updateQuote()
                                                        }
                                                        showStartDatePicker = false
                                                    }
                                                ) { Text("OK") }
                                            },
                                            dismissButton = {
                                                TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
                                            }
                                        ) {
                                            DatePicker(state = startDatePickerState)
                                        }
                                    }

                                    // End date picker dialog
                                    if (showEndDatePicker) {
                                        val endDatePickerState = rememberDatePickerState(
                                            initialSelectedDateMillis = endDateTime.date
                                                .atStartOfDayIn(TimeZone.UTC)
                                                .toEpochMilliseconds()
                                        )
                                        DatePickerDialog(
                                            onDismissRequest = { showEndDatePicker = false },
                                            confirmButton = {
                                                TextButton(
                                                    onClick = {
                                                        val millis = endDatePickerState.selectedDateMillis
                                                        if (millis != null) {
                                                            val selectedDate = Instant.fromEpochMilliseconds(millis)
                                                                .toLocalDateTime(TimeZone.UTC)
                                                                .date
                                                            endDateTime = LocalDateTime(
                                                                selectedDate.year,
                                                                selectedDate.monthNumber,
                                                                selectedDate.dayOfMonth,
                                                                endDateTime.hour,
                                                                endDateTime.minute,
                                                                0,
                                                                0
                                                            )
                                                            updateQuote()
                                                        }
                                                        showEndDatePicker = false
                                                    }
                                                ) { Text("OK") }
                                            },
                                            dismissButton = {
                                                TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") }
                                            }
                                        ) {
                                            DatePicker(state = endDatePickerState)
                                        }
                                    }

                                    Text("Start", style = AppTheme.typography.bodyMedium)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { showStartDatePicker = true }
                                        ) {
                                            OutlinedTextField(
                                                value = startDateTime.date.toString(),
                                                onValueChange = {},
                                                readOnly = true,
                                                enabled = false,
                                                label = { Text("Start date") },
                                                trailingIcon = {
                                                    Icon(Icons.Filled.Event, contentDescription = "Pick start date")
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = pickerFieldColors,
                                                singleLine = true
                                            )
                                        }

                                        ExposedDropdownMenuBox(
                                            expanded = showStartTimeMenu,
                                            onExpandedChange = { showStartTimeMenu = it },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            OutlinedTextField(
                                                value = formatTime(startDateTime.hour, startDateTime.minute),
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text("Start time") },
                                                trailingIcon = {
                                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = showStartTimeMenu)
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .menuAnchor(),
                                                colors = pickerFieldColors,
                                                singleLine = true
                                            )
                                            ExposedDropdownMenu(
                                                expanded = showStartTimeMenu,
                                                onDismissRequest = { showStartTimeMenu = false }
                                            ) {
                                                timeOptions.forEach { (h, m) ->
                                                    DropdownMenuItem(
                                                        text = { Text(formatTime(h, m)) },
                                                        onClick = {
                                                            updateStartKeepingDuration(
                                                                LocalDateTime(
                                                                    startDateTime.year,
                                                                    startDateTime.monthNumber,
                                                                    startDateTime.dayOfMonth,
                                                                    h,
                                                                    m,
                                                                    0,
                                                                    0
                                                                )
                                                            )
                                                            showStartTimeMenu = false
                                                            updateQuote()
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text("End", style = AppTheme.typography.bodyMedium)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { showEndDatePicker = true }
                                        ) {
                                            OutlinedTextField(
                                                value = endDateTime.date.toString(),
                                                onValueChange = {},
                                                readOnly = true,
                                                enabled = false,
                                                label = { Text("End date") },
                                                trailingIcon = {
                                                    Icon(Icons.Filled.Event, contentDescription = "Pick end date")
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = pickerFieldColors,
                                                singleLine = true
                                            )
                                        }

                                        ExposedDropdownMenuBox(
                                            expanded = showEndTimeMenu,
                                            onExpandedChange = { showEndTimeMenu = it },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            OutlinedTextField(
                                                value = formatTime(endDateTime.hour, endDateTime.minute),
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text("End time") },
                                                trailingIcon = {
                                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = showEndTimeMenu)
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .menuAnchor(),
                                                colors = pickerFieldColors,
                                                singleLine = true
                                            )
                                            ExposedDropdownMenu(
                                                expanded = showEndTimeMenu,
                                                onDismissRequest = { showEndTimeMenu = false }
                                            ) {
                                                timeOptions.forEach { (h, m) ->
                                                    DropdownMenuItem(
                                                        text = { Text(formatTime(h, m)) },
                                                        onClick = {
                                                            endDateTime = LocalDateTime(
                                                                endDateTime.year,
                                                                endDateTime.monthNumber,
                                                                endDateTime.dayOfMonth,
                                                                h,
                                                                m,
                                                                0,
                                                                0
                                                            )
                                                            showEndTimeMenu = false
                                                            updateQuote()
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = "Selected: ${formatDateTime(startDateTime)} → ${formatDateTime(endDateTime)}",
                                        style = AppTheme.typography.bodySmall,
                                        color = AppTheme.colors.onSurface.copy(alpha = 0.7f)
                                    )

                                    if (validationMessage != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = validationMessage!!,
                                            color = AppTheme.colors.error,
                                            style = AppTheme.typography.bodySmall
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
