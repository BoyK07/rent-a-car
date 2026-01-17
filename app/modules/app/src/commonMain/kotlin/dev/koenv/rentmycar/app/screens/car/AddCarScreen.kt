package dev.koenv.rentmycar.app.screens.car

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import dev.koenv.rentmycar.app.ui.AppTheme
import dev.koenv.rentmycar.app.ui.components.Button
import dev.koenv.rentmycar.app.ui.components.ButtonVariant
import dev.koenv.rentmycar.app.ui.components.DateTimePickerField
import dev.koenv.rentmycar.app.ui.components.Icon
import dev.koenv.rentmycar.app.ui.components.IconButton
import dev.koenv.rentmycar.app.ui.components.IconButtonVariant
import dev.koenv.rentmycar.app.ui.components.Scaffold
import dev.koenv.rentmycar.app.ui.components.Switch
import dev.koenv.rentmycar.app.ui.components.Text
import dev.koenv.rentmycar.app.ui.components.card.Card
import dev.koenv.rentmycar.app.ui.components.topbar.TopBar
import dev.koenv.rentmycar.shared.SharedModule
import dev.koenv.rentmycar.shared.domain.enums.CarCategory
import dev.koenv.rentmycar.shared.domain.enums.FuelType
import dev.koenv.rentmycar.shared.dto.car.CreateCarAvailabilityRequestDto
import dev.koenv.rentmycar.shared.dto.car.CreateCarRequestDto
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlin.time.Clock
import kotlin.uuid.Uuid

/**
 * Screen for adding a new car listing to the platform.
 * 
 * Features:
 * - Form with all required car details (brand, model, category, fuel, location, pricing)
 * - Category selection dropdown (ICE, BEV, FCEV)
 * - Fuel type selection dropdown
 * - Address input (geocoded server-side)
 * - Active status toggle
 * - Form validation
 * - Save with loading state
 * - Error display
 * - Navigation back on success
 * 
 * Access: DRIVER and ADMIN roles only
 */
class AddCarScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val carsRepository = remember { SharedModule.carsRepository }
        val availabilityApi = remember { SharedModule.carAvailabilityApi }
        
        var isSaving by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var createdCarId by remember { mutableStateOf<Uuid?>(null) }
        
        // Form fields
        var brand by remember { mutableStateOf("") }
        var model by remember { mutableStateOf("") }
        var ratePerHour by remember { mutableStateOf("") }
        var category by remember { mutableStateOf<CarCategory?>(null) }
        var fuelType by remember { mutableStateOf<FuelType?>(null) }
        var addressLine1 by remember { mutableStateOf("") }
        var addressLine2 by remember { mutableStateOf("") }
        var postalCode by remember { mutableStateOf("") }
        var city by remember { mutableStateOf("") }
        var country by remember { mutableStateOf("") }
        var isActive by remember { mutableStateOf(true) }
        
        var showCategoryMenu by remember { mutableStateOf(false) }
        var showFuelTypeMenu by remember { mutableStateOf(false) }

        val now = Clock.System.now()
        val nextHour = now.plus(1, DateTimeUnit.HOUR, TimeZone.UTC)
        val defaultStart = nextHour.let {
            val local = it.toLocalDateTime(TimeZone.UTC)
            LocalDateTime(local.year, local.monthNumber, local.dayOfMonth, local.hour, 0, 0, 0)
        }
        val defaultEnd = defaultStart.toInstant(TimeZone.UTC)
            .plus(2, DateTimeUnit.HOUR, TimeZone.UTC)
            .toLocalDateTime(TimeZone.UTC)

        var availabilityWindows by remember {
            mutableStateOf(
                listOf(AvailabilityWindow(start = defaultStart, end = defaultEnd))
            )
        }
        
        val scope = rememberCoroutineScope()
        
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
                            text = "Add New Car",
                            style = AppTheme.typography.titleLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Enter car details",
                    style = AppTheme.typography.titleMedium,
                    color = AppTheme.colors.onSurface.copy(alpha = 0.7f)
                )
                
                // Brand field
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Brand") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = brand.isBlank() && errorMessage != null
                )
                
                // Model field
                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("Model") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = model.isBlank() && errorMessage != null
                )
                
                // Rate per hour field
                OutlinedTextField(
                    value = ratePerHour,
                    onValueChange = { ratePerHour = it },
                    label = { Text("Rate per Hour (€)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = ratePerHour.isBlank() && errorMessage != null,
                    supportingText = { Text("Enter the hourly rental rate") }
                )
                
                // Category dropdown
                ExposedDropdownMenuBox(
                    expanded = showCategoryMenu,
                    onExpandedChange = { showCategoryMenu = it }
                ) {
                    OutlinedTextField(
                        value = category?.label ?: "Select category",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryMenu) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        isError = category == null && errorMessage != null
                    )
                    ExposedDropdownMenu(
                        expanded = showCategoryMenu,
                        onDismissRequest = { showCategoryMenu = false }
                    ) {
                        CarCategory.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.label) },
                                onClick = {
                                    category = cat
                                    showCategoryMenu = false
                                }
                            )
                        }
                    }
                }
                
                // Fuel type dropdown
                ExposedDropdownMenuBox(
                    expanded = showFuelTypeMenu,
                    onExpandedChange = { showFuelTypeMenu = it }
                ) {
                    OutlinedTextField(
                        value = fuelType?.name ?: "Not specified",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fuel Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showFuelTypeMenu) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = showFuelTypeMenu,
                        onDismissRequest = { showFuelTypeMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Not specified") },
                            onClick = {
                                fuelType = null
                                showFuelTypeMenu = false
                            }
                        )
                        FuelType.entries.forEach { fuel ->
                            DropdownMenuItem(
                                text = { Text(fuel.name) },
                                onClick = {
                                    fuelType = fuel
                                    showFuelTypeMenu = false
                                }
                            )
                        }
                    }
                }
                
                // Address line 1
                OutlinedTextField(
                    value = addressLine1,
                    onValueChange = { addressLine1 = it },
                    label = { Text("Street and Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = addressLine1.isBlank() && errorMessage != null,
                    supportingText = { Text("e.g., Coolsingel 1") }
                )
                
                // Address line 2 (optional)
                OutlinedTextField(
                    value = addressLine2,
                    onValueChange = { addressLine2 = it },
                    label = { Text("Address Line 2") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = { Text("Apartment, floor, etc. (optional)") }
                )
                
                // Postal code
                OutlinedTextField(
                    value = postalCode,
                    onValueChange = { postalCode = it },
                    label = { Text("Postal Code") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = postalCode.isBlank() && errorMessage != null,
                    supportingText = { Text("e.g., 1012 JS") }
                )
                
                // City
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("City") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = city.isBlank() && errorMessage != null,
                    supportingText = { Text("e.g., Amsterdam") }
                )
                
                // Country
                OutlinedTextField(
                    value = country,
                    onValueChange = { country = it },
                    label = { Text("Country") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = country.isBlank() && errorMessage != null,
                    supportingText = { Text("e.g., Netherlands") }
                )
                
                // Is Active toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Make car available immediately")
                        Text(
                            "Car will be visible to renters",
                            style = AppTheme.typography.bodySmall,
                            color = AppTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it }
                    )
                }

                // Availability windows
                Text(
                    text = "Availability windows",
                    style = AppTheme.typography.titleMedium
                )
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    availabilityWindows.forEachIndexed { index, window ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Window ${index + 1}",
                                        style = AppTheme.typography.bodyMedium
                                    )
                                    if (availabilityWindows.size > 1) {
                                        IconButton(
                                            onClick = {
                                                availabilityWindows = availabilityWindows
                                                    .filterIndexed { i, _ -> i != index }
                                            },
                                            variant = IconButtonVariant.Ghost
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Delete,
                                                contentDescription = "Remove window"
                                            )
                                        }
                                    }
                                }

                                DateTimePickerField(
                                    label = "Start time",
                                    dateTime = window.start,
                                    onDateTimeSelected = { selected ->
                                        availabilityWindows = availabilityWindows.updateWindow(index) {
                                            it.copy(start = selected)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                DateTimePickerField(
                                    label = "End time",
                                    dateTime = window.end,
                                    onDateTimeSelected = { selected ->
                                        availabilityWindows = availabilityWindows.updateWindow(index) {
                                            it.copy(end = selected)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        availabilityWindows = availabilityWindows + AvailabilityWindow(
                            start = defaultStart,
                            end = defaultEnd
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    variant = ButtonVariant.Secondary
                ) {
                    Text("Add availability window")
                }
                
                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = AppTheme.colors.error,
                        style = AppTheme.typography.bodyMedium
                    )
                }
                
                // Add button
                Button(
                    onClick = {
                        // Validate inputs
                        if (brand.isBlank() || model.isBlank() || ratePerHour.isBlank() || 
                            category == null || addressLine1.isBlank() || postalCode.isBlank() ||
                            city.isBlank() || country.isBlank()) {
                            errorMessage = "Please fill in all required fields"
                            return@Button
                        }

                        val invalidWindow = availabilityWindows.any { it.start >= it.end }
                        if (invalidWindow) {
                            errorMessage = "Availability windows must have a start before end"
                            return@Button
                        }
                        
                        val rate = runCatching { BigDecimal.parseString(ratePerHour) }.getOrNull()
                        if (rate == null) {
                            errorMessage = "Invalid rate per hour"
                            return@Button
                        }
                        
                        scope.launch {
                            isSaving = true
                            errorMessage = null
                            
                            val carId = createdCarId ?: run {
                                val createRequest = CreateCarRequestDto(
                                    brand = brand,
                                    model = model,
                                    category = category!!,
                                    fuelType = fuelType,
                                    ratePerHour = rate,
                                    addressLine1 = addressLine1,
                                    addressLine2 = addressLine2.takeIf { it.isNotBlank() },
                                    postalCode = postalCode,
                                    city = city,
                                    country = country,
                                    isActive = isActive
                                )
                                
                                val created = carsRepository.createCar(createRequest).getOrElse { error ->
                                    errorMessage = error.message ?: "Failed to add car"
                                    isSaving = false
                                    return@launch
                                }
                                createdCarId = created.id
                                created.id
                            }

                            val availabilityErrors = availabilityWindows.mapNotNull { window ->
                                availabilityApi.createCarAvailability(
                                    carId = carId,
                                    request = CreateCarAvailabilityRequestDto(
                                        startTime = window.start,
                                        endTime = window.end
                                    )
                                ).exceptionOrNull()
                            }

                            if (availabilityErrors.isNotEmpty()) {
                                errorMessage = "Car created, but availability could not be saved."
                                isSaving = false
                                return@launch
                            }

                            navigator.pop()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = AppTheme.colors.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (isSaving) "Adding..." else "Add Car")
                }
            }
        }
    }
}

private data class AvailabilityWindow(
    val start: LocalDateTime,
    val end: LocalDateTime
)

private fun List<AvailabilityWindow>.updateWindow(
    index: Int,
    updater: (AvailabilityWindow) -> AvailabilityWindow
): List<AvailabilityWindow> {
    return mapIndexed { i, window ->
        if (i == index) updater(window) else window
    }
}
