package dev.koenv.rentmycar.app.screens.car

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
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import dev.koenv.rentmycar.app.ui.components.Button
import dev.koenv.rentmycar.app.ui.components.Text
import dev.koenv.rentmycar.shared.SharedModule
import dev.koenv.rentmycar.shared.domain.enums.CarCategory
import dev.koenv.rentmycar.shared.domain.enums.FuelType
import dev.koenv.rentmycar.shared.dto.car.PatchCarRequestDto
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

/**
 * Screen for editing car details.
 * Only accessible to car owners (DRIVER role) and admins.
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
        var locationLat by remember { mutableStateOf("") }
        var locationLng by remember { mutableStateOf("") }
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
                    locationLat = carDto.locationLat.toString()
                    locationLng = carDto.locationLng.toString()
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
                TopAppBar(
                    title = { Text("Edit Car") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
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
                                color = MaterialTheme.colorScheme.error
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
                            
                            // Location Latitude field
                            OutlinedTextField(
                                value = locationLat,
                                onValueChange = { locationLat = it },
                                label = { Text("Location Latitude") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            
                            // Location Longitude field
                            OutlinedTextField(
                                value = locationLng,
                                onValueChange = { locationLng = it },
                                label = { Text("Location Longitude") },
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
                                            locationLat = locationLat.toDoubleOrNull()?.takeIf { it != originalCar.locationLat },
                                            locationLng = locationLng.toDoubleOrNull()?.takeIf { it != originalCar.locationLng },
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
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Text("Save Changes")
                                }
                            }
                            
                            if (errorMessage != null) {
                                Text(
                                    text = errorMessage ?: "",
                                    color = MaterialTheme.colorScheme.error,
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
