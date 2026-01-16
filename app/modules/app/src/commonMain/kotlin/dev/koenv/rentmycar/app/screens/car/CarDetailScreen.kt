package dev.koenv.rentmycar.app.screens.car

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import dev.koenv.rentmycar.app.screens.reservation.BookCarScreen
import dev.koenv.rentmycar.app.ui.AppTheme
import dev.koenv.rentmycar.app.ui.components.*
import dev.koenv.rentmycar.app.ui.components.card.Card
import dev.koenv.rentmycar.app.ui.components.topbar.TopBar
import dev.koenv.rentmycar.shared.SharedModule
import dev.koenv.rentmycar.shared.dto.car.CarDto
import dev.koenv.rentmycar.shared.dto.car.CarPhotoDto
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

/**
 * Car detail screen showing comprehensive information about a specific vehicle.
 * 
 * Features:
 * - Car specifications (brand, model, category, fuel type, location)
 * - Pricing information (hourly rate, TCO, maintenance costs)
 * - Photo gallery with image loading
 * - Availability status
 * - Edit and delete actions (owner/admin only)
 * - Delete confirmation dialog
 * - Book now button for available cars
 * - Automatic car view tracking
 */
data class CarDetailScreen(
    val carId: Uuid
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val carsRepository = remember { SharedModule.carsRepository }
        val authRepository = remember { SharedModule.authRepository }

        var car by remember { mutableStateOf<CarDto?>(null) }
        var carPhotos by remember { mutableStateOf<List<CarPhotoDto>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var isLoadingPhotos by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var showDeleteDialog by remember { mutableStateOf(false) }

        val currentUser by authRepository.currentUser.collectAsState()
        val canEdit = remember(currentUser, car) {
            val user = currentUser ?: return@remember false
            val carData = car ?: return@remember false
            val role = user.role.name
            role == "ADMIN" || (role == "DRIVER" && carData.ownerId == user.id)
        }

        val scope = rememberCoroutineScope()

        LaunchedEffect(carId) {
            scope.launch {
                carsRepository.getCar(carId).onSuccess { carDto ->
                    car = carDto
                    isLoading = false

                    isLoadingPhotos = true
                    SharedModule.carPhotoApi.getCarPhotosByCarId(carId).onSuccess { photos ->
                        carPhotos = photos
                        isLoadingPhotos = false
                    }.onFailure {
                        isLoadingPhotos = false
                    }
                }.onFailure { error ->
                    errorMessage = error.message ?: "Failed to load car details"
                    isLoading = false
                }
            }
        }

        if (showDeleteDialog && car != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Car") },
                text = {
                    Text(
                        "Are you sure you want to delete '${car?.brand} ${car?.model}'? " +
                                "This action cannot be undone."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteDialog = false
                            scope.launch {
                                carsRepository.deleteCar(carId).onSuccess {
                                    navigator.pop()
                                }.onFailure { error ->
                                    errorMessage = error.message ?: "Failed to delete car"
                                }
                            }
                        },
                        variant = ButtonVariant.Destructive
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showDeleteDialog = false },
                        variant = ButtonVariant.Ghost
                    ) {
                        Text("Cancel")
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { navigator.pop() },
                                variant = IconButtonVariant.Ghost
                            ) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                            }
                            Text(
                                text = "Car Details",
                                style = AppTheme.typography.titleLarge,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        if (canEdit && car != null) {
                            Row {
                                IconButton(
                                    onClick = { navigator.push(EditCarScreen(carId)) },
                                    variant = IconButtonVariant.Ghost
                                ) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Edit Car")
                                }
                                IconButton(
                                    onClick = { showDeleteDialog = true },
                                    variant = IconButtonVariant.Ghost
                                ) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete Car")
                                }
                            }
                        }
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

                    car != null -> {
                        CarDetailContent(car = car!!, carPhotos = carPhotos)
                    }
                }
            }
        }
    }
}

@Composable
private fun CarDetailContent(car: CarDto, carPhotos: List<CarPhotoDto>) {
    val navigator = LocalNavigator.currentOrThrow
    val authRepository = remember { SharedModule.authRepository }
    val currentUser by authRepository.currentUser.collectAsState()

    // Check if user can book this car (not the owner and car is active)
    val canBook = remember(currentUser, car) {
        val user = currentUser ?: return@remember false
        val isActive = car.isActive
        val isNotOwner = car.ownerId != user.id
        isActive && isNotOwner
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Car photos
        if (carPhotos.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Photos",
                        style = AppTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Display primary photo or first photo
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

        // Main info card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "${car.brand} ${car.model}",
                    style = AppTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = car.category.name,
                    style = AppTheme.typography.titleMedium,
                    color = AppTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        // Pricing card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Pricing",
                    style = AppTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Rate per hour:")
                    Text(
                        text = "\u20ac${car.ratePerHour.roundSignificand(DecimalMode.US_CURRENCY).toPlainString()}",
                        style = AppTheme.typography.titleMedium,
                        color = AppTheme.colors.primary
                    )
                }
            }
        }

        // Details card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Details",
                    style = AppTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(12.dp))

                DetailRow("Category", car.category.name)
                if (car.fuelType != null) {
                    DetailRow("Fuel Type", car.fuelType!!.name)
                }
                DetailRow(
                    "Status",
                    if (car.isActive) "Available" else "Unavailable"
                )
                DetailRow("Car ID", car.id.toString().take(8) + "...")
            }
        }

        // Location card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Location",
                    style = AppTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(12.dp))

                DetailRow("Latitude", car.locationLat.toString())
                DetailRow("Longitude", car.locationLng.toString())
            }
        }

        // Book Now button
        if (canBook) {
            Button(
                onClick = {
                    navigator.push(BookCarScreen(car.id))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Book Now")
            }
        } else if (!car.isActive) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "This car is currently unavailable for booking",
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = AppTheme.typography.bodyMedium
        )
    }
}
