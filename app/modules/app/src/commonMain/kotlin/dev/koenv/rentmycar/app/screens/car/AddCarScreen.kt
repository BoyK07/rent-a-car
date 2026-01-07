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
import dev.koenv.rentmycar.shared.dto.car.CreateCarRequestDto
import kotlinx.coroutines.launch

/**
 * Screen for adding a new car to the platform.
 * Only accessible to users with DRIVER or ADMIN roles.
 */
class AddCarScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val carsRepository = remember { SharedModule.carsRepository }
        
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
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Add New Car") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
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
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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
                
                // Location Latitude field
                OutlinedTextField(
                    value = locationLat,
                    onValueChange = { locationLat = it },
                    label = { Text("Location Latitude") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = locationLat.isBlank() && errorMessage != null,
                    supportingText = { Text("e.g., 52.3676") }
                )
                
                // Location Longitude field
                OutlinedTextField(
                    value = locationLng,
                    onValueChange = { locationLng = it },
                    label = { Text("Location Longitude") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = locationLng.isBlank() && errorMessage != null,
                    supportingText = { Text("e.g., 4.9041") }
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
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it }
                    )
                }
                
                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                // Add button
                Button(
                    onClick = {
                        // Validate inputs
                        if (brand.isBlank() || model.isBlank() || ratePerHour.isBlank() || 
                            category == null || locationLat.isBlank() || locationLng.isBlank()) {
                            errorMessage = "Please fill in all required fields"
                            return@Button
                        }
                        
                        val rate = runCatching { BigDecimal.parseString(ratePerHour) }.getOrNull()
                        if (rate == null) {
                            errorMessage = "Invalid rate per hour"
                            return@Button
                        }
                        
                        val lat = locationLat.toDoubleOrNull()
                        val lng = locationLng.toDoubleOrNull()
                        if (lat == null || lng == null) {
                            errorMessage = "Invalid location coordinates"
                            return@Button
                        }
                        
                        scope.launch {
                            isSaving = true
                            errorMessage = null
                            
                            val createRequest = CreateCarRequestDto(
                                brand = brand,
                                model = model,
                                category = category!!,
                                fuelType = fuelType,
                                ratePerHour = rate,
                                locationLat = lat,
                                locationLng = lng,
                                isActive = isActive
                            )
                            
                            carsRepository.createCar(createRequest).onSuccess {
                                navigator.pop()
                            }.onFailure { error ->
                                errorMessage = error.message ?: "Failed to add car"
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
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (isSaving) "Adding..." else "Add Car")
                }
            }
        }
    }
}
