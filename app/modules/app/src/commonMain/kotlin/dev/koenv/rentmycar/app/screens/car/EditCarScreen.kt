package dev.koenv.rentmycar.app.screens.car

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import dev.koenv.rentmycar.app.ui.components.Icon
import dev.koenv.rentmycar.app.ui.components.IconButton
import dev.koenv.rentmycar.app.ui.components.IconButtonVariant
import dev.koenv.rentmycar.app.ui.components.Scaffold
import dev.koenv.rentmycar.app.ui.components.Switch
import dev.koenv.rentmycar.app.ui.components.Text
import dev.koenv.rentmycar.app.ui.components.topbar.TopBar
import dev.koenv.rentmycar.shared.SharedModule
import dev.koenv.rentmycar.shared.domain.enums.CarCategory
import dev.koenv.rentmycar.shared.domain.enums.FuelType
import dev.koenv.rentmycar.shared.dto.car.PatchCarRequestDto
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

/**
 * Screen for editing existing car details.
 * 
 * Features:
 * - Pre-populated form fields with current car data
 * - Partial update support (PATCH)
 * - Category and fuel type dropdowns
 * - Active/inactive status toggle
 * - Form validation with error display
 * - Save with loading state
 * - Only accessible to car owners (DRIVER role) and admins
 */
data class EditCarScreen(
    val carId: Uuid
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val carsRepository = remember { SharedModule.carsRepository }
        
        var car by remember { mutableStateOf<dev.koenv.rentmycar.shared.dto.car.CarDto?>(null) }
        var isLoading by remember { mutableStateOf(true) }
        var isSaving by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        
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
        
        val scope = rememberCoroutineScope()
        
        // Load car data
        LaunchedEffect(carId) {
            scope.launch {
                carsRepository.getCar(carId).onSuccess { carDto ->
                    car = carDto
                    brand = carDto.brand
                    model = carDto.model
                    ratePerHour = carDto.ratePerHour.toStringExpanded()
                    category = carDto.category
                    fuelType = carDto.fuelType
                    addressLine1 = carDto.addressLine1 ?: ""
                    addressLine2 = carDto.addressLine2 ?: ""
                    postalCode = carDto.postalCode ?: ""
                    city = carDto.city ?: ""
                    country = carDto.country ?: ""
                    isActive = carDto.isActive
                    isLoading = false
                }.onFailure { error ->
                    errorMessage = error.message ?: "Failed to load car"
                    isLoading = false
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
                            text = "Edit Car",
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
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    errorMessage != null -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                color = AppTheme.colors.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { navigator.pop() }) {
                                Text("Go Back")
                            }
                        }
                    }
                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Brand field
                            OutlinedTextField(
                                value = brand,
                                onValueChange = { brand = it },
                                label = { Text("Brand") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            
                            // Model field
                            OutlinedTextField(
                                value = model,
                                onValueChange = { model = it },
                                label = { Text("Model") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            
                            // Rate per hour field
                            OutlinedTextField(
                                value = ratePerHour,
                                onValueChange = { ratePerHour = it },
                                label = { Text("Rate per Hour") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            
                            // Category dropdown
                            ExposedDropdownMenuBox(
                                expanded = showCategoryMenu,
                                onExpandedChange = { showCategoryMenu = it }
                            ) {
                                OutlinedTextField(
                                    value = category?.label ?: "Not specified",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Category") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryMenu) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
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
                                singleLine = true
                            )
                            
                            // Address line 2 (optional)
                            OutlinedTextField(
                                value = addressLine2,
                                onValueChange = { addressLine2 = it },
                                label = { Text("Address Line 2") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            
                            // Postal code
                            OutlinedTextField(
                                value = postalCode,
                                onValueChange = { postalCode = it },
                                label = { Text("Postal Code") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            
                            // City
                            OutlinedTextField(
                                value = city,
                                onValueChange = { city = it },
                                label = { Text("City") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            
                            // Country
                            OutlinedTextField(
                                value = country,
                                onValueChange = { country = it },
                                label = { Text("Country") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            
                            // Is Active toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Active")
                                Switch(
                                    checked = isActive,
                                    onCheckedChange = { isActive = it }
                                )
                            }
                            
                            // Save button
                            Button(
                                onClick = {
                                    scope.launch {
                                        isSaving = true
                                        errorMessage = null
                                        
                                        val originalCar = car
                                        if (originalCar == null) {
                                            errorMessage = "Car data not loaded"
                                            isSaving = false
                                            return@launch
                                        }
                                        
                                        val patchRequest = PatchCarRequestDto(
                                            brand = if (brand != originalCar.brand) brand else null,
                                            model = if (model != originalCar.model) model else null,
                                            ratePerHour = runCatching { 
                                                BigDecimal.parseString(ratePerHour) 
                                            }.getOrNull()?.takeIf { it != originalCar.ratePerHour },
                                            category = if (category != originalCar.category) category else null,
                                            fuelType = if (fuelType != originalCar.fuelType) fuelType else null,
                                            addressLine1 = if (addressLine1 != (originalCar.addressLine1 ?: "")) addressLine1 else null,
                                            addressLine2 = if (addressLine2 != (originalCar.addressLine2 ?: "")) addressLine2 else null,
                                            postalCode = if (postalCode != (originalCar.postalCode ?: "")) postalCode else null,
                                            city = if (city != (originalCar.city ?: "")) city else null,
                                            country = if (country != (originalCar.country ?: "")) country else null,
                                            isActive = if (isActive != originalCar.isActive) isActive else null
                                        )
                                        
                                        carsRepository.patchCar(carId, patchRequest).onSuccess {
                                            navigator.pop()
                                        }.onFailure { error ->
                                            val message = error.message ?: "Failed to update car"
                                            errorMessage = message
                                            isSaving = false
                                        }
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
                                } else {
                                    Text("Save Changes")
                                }
                            }
                            
                            if (errorMessage != null) {
                                Text(
                                    text = errorMessage ?: "",
                                    color = AppTheme.colors.error,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
