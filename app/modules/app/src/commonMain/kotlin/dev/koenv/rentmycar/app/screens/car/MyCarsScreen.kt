package dev.koenv.rentmycar.app.screens.car

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import dev.koenv.rentmycar.app.util.rememberImagePicker
import dev.koenv.rentmycar.shared.SharedModule
import dev.koenv.rentmycar.shared.dto.car.CarDto
import dev.koenv.rentmycar.shared.dto.car.CarPhotoDto
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

/**
 * Shows cars owned by the current user with quick access to edit.
 */
class MyCarsScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val carsRepository = remember { SharedModule.carsRepository }
        val authRepository = remember { SharedModule.authRepository }
        val carPhotoApi = remember { SharedModule.carPhotoApi }

        var cars by remember { mutableStateOf<List<CarDto>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var isRefreshing by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var carToDelete by remember { mutableStateOf<CarDto?>(null) }
        val photosByCar = remember { mutableStateMapOf<Uuid, List<CarPhotoDto>>() }
        var uploadTargetId by remember { mutableStateOf<Uuid?>(null) }

        val scope = rememberCoroutineScope()

        val pickImage = rememberImagePicker { fileName, fileBytes ->
            val carId = uploadTargetId
            uploadTargetId = null
            if (carId == null) return@rememberImagePicker
            scope.launch {
                carPhotoApi.uploadCarPhoto(carId, fileName, fileBytes).onSuccess {
                    carPhotoApi.getCarPhotosByCarId(carId).onSuccess { photos ->
                        photosByCar[carId] = photos
                    }
                }.onFailure { error ->
                    errorMessage = error.message ?: "Failed to upload photo"
                }
            }
        }

        val currentUser by authRepository.currentUser.collectAsState()
        val loadCars: (Boolean) -> Unit = { force ->
            scope.launch {
                if (force) {
                    isRefreshing = true
                } else {
                    isLoading = true
                }
                errorMessage = null

                val userId = currentUser?.id
                if (userId == null) {
                    errorMessage = "User not logged in"
                    isLoading = false
                    isRefreshing = false
                    return@launch
                }

                carsRepository.getCarsByOwner(userId).onSuccess { list ->
                    cars = list
                    list.forEach { car ->
                        if (!photosByCar.containsKey(car.id)) {
                            carPhotoApi.getCarPhotosByCarId(car.id).onSuccess { photos ->
                                photosByCar[car.id] = photos
                            }
                        }
                    }
                }.onFailure { error ->
                    errorMessage = error.message ?: "Failed to load cars"
                }

                isLoading = false
                isRefreshing = false
            }
        }

        LaunchedEffect(currentUser?.id) {
            loadCars(false)
        }

        if (carToDelete != null) {
            AlertDialog(
                onDismissRequest = { carToDelete = null },
                title = { Text("Delete Car") },
                text = { Text("Are you sure you want to delete '${carToDelete?.brand} ${carToDelete?.model}'?") },
                confirmButton = {
                    Button(onClick = {
                        val car = carToDelete
                        carToDelete = null
                        if (car != null) {
                            scope.launch {
                                carsRepository.deleteCar(car.id).onSuccess {
                                    loadCars(true)
                                }.onFailure { error ->
                                    errorMessage = error.message ?: "Failed to delete car"
                                }
                            }
                        }
                    }) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    Button(onClick = { carToDelete = null }, variant = ButtonVariant.Ghost) {
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
                                text = "My Cars",
                                style = AppTheme.typography.titleLarge,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        IconButton(
                            onClick = { loadCars(true) },
                            enabled = !isRefreshing,
                            variant = IconButtonVariant.Ghost
                        ) {
                            if (isRefreshing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
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
                            Text(text = errorMessage ?: "", color = AppTheme.colors.error)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { loadCars(true) }) {
                                Text("Retry")
                            }
                        }
                    }
                    cars.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "You have no cars yet",
                                style = AppTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Add a car to start renting",
                                style = AppTheme.typography.bodyMedium,
                                color = AppTheme.colors.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(cars) { car ->
                                MyCarListItem(
                                    car = car,
                                    onEdit = { navigator.push(EditCarScreen(car.id)) },
                                    onDelete = { carToDelete = car },
                                    photoCount = photosByCar[car.id]?.size ?: 0,
                                    onAddPhoto = {
                                        uploadTargetId = car.id
                                        pickImage()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MyCarListItem(
    car: CarDto,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    photoCount: Int,
    onAddPhoto: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${car.brand} ${car.model}",
                        style = AppTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = car.category.label,
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onEdit,
                        variant = IconButtonVariant.Ghost
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit car",
                            tint = AppTheme.colors.primary
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        variant = IconButtonVariant.Ghost
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete car",
                            tint = AppTheme.colors.error
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "€${car.ratePerHour.roundSignificand(DecimalMode.US_CURRENCY).toPlainString()}/hr",
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (car.isActive) "Active" else "Inactive",
                style = AppTheme.typography.bodySmall,
                color = if (car.isActive) AppTheme.colors.success else AppTheme.colors.onSurface.copy(alpha = 0.6f)
            )
            if (photoCount == 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onAddPhoto, variant = ButtonVariant.Secondary) {
                    Text("Add Photo")
                }
            }
        }
    }
}
