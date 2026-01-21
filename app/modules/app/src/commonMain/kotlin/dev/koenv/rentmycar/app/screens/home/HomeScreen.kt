package dev.koenv.rentmycar.app.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.SubcomposeAsyncImage
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import dev.koenv.rentmycar.app.screens.car.AddCarScreen
import dev.koenv.rentmycar.app.screens.car.CarDetailScreen
import dev.koenv.rentmycar.app.screens.map.MapScreen
import dev.koenv.rentmycar.app.screens.profile.ProfileScreen
import dev.koenv.rentmycar.app.screens.admin.AdminScreen
import dev.koenv.rentmycar.app.location.rememberUserLocation
import dev.koenv.rentmycar.app.ui.AppTheme
import dev.koenv.rentmycar.app.ui.components.Button
import dev.koenv.rentmycar.app.ui.components.Icon
import dev.koenv.rentmycar.app.ui.components.IconButton
import dev.koenv.rentmycar.app.ui.components.IconButtonVariant
import dev.koenv.rentmycar.app.ui.components.Scaffold
import dev.koenv.rentmycar.app.ui.components.Switch
import dev.koenv.rentmycar.app.ui.components.Text
import dev.koenv.rentmycar.app.ui.components.topbar.TopBar
import dev.koenv.rentmycar.app.ui.components.card.Card
import dev.koenv.rentmycar.app.ui.layout.MainLayoutBottomBar
import dev.koenv.rentmycar.shared.SharedModule
import dev.koenv.rentmycar.shared.dto.car.CarDto
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.sqrt

/**
 * Home screen displaying available cars with comprehensive filtering and sorting capabilities.
 * 
 * Features:
 * - Car list with real-time filtering
 * - Multi-criteria filtering (availability, brand, category, price, distance)
 * - Sort options (price, distance, brand)
 * - Persistent filter state across navigation
 * - Refresh capability with pull-to-refresh indicator
 * - Role-based FAB for adding cars (ADMIN/DRIVER only)
 * - Distance calculation for nearby search using Haversine formula
 */
class HomeScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val carsRepository = remember { SharedModule.carsRepository }
        val authRepository = remember { SharedModule.authRepository }
        val filterState = remember { SharedModule.filterState }
        
        var cars by remember { mutableStateOf<List<CarDto>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var isRefreshing by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        
        var filtersExpanded by remember { mutableStateOf(filterState.filtersExpanded) }
        var showAvailableOnly by remember { mutableStateOf(filterState.showAvailableOnly) }
        var searchNearby by remember { mutableStateOf(filterState.searchNearby) }
        var userLat by remember { mutableStateOf(filterState.userLat) }
        var userLng by remember { mutableStateOf(filterState.userLng) }
        var maxDistance by remember { mutableStateOf(filterState.maxDistance) }
        var brandFilter by remember { mutableStateOf(filterState.brandFilter) }
        var selectedCategories by remember { mutableStateOf(filterState.selectedCategories) }
        var minRate by remember { mutableStateOf(filterState.minRate) }
        var maxRate by remember { mutableStateOf(filterState.maxRate) }
        var sortBy by remember { mutableStateOf(filterState.sortBy) }

        rememberUserLocation { lat, lng ->
            userLat = lat
            userLng = lng
        }

        LaunchedEffect(filtersExpanded, showAvailableOnly, searchNearby, userLat, userLng, maxDistance, brandFilter, selectedCategories, minRate, maxRate, sortBy) {
            filterState.filtersExpanded = filtersExpanded
            filterState.showAvailableOnly = showAvailableOnly
            filterState.searchNearby = searchNearby
            filterState.userLat = userLat
            filterState.userLng = userLng
            filterState.maxDistance = maxDistance
            filterState.brandFilter = brandFilter
            filterState.selectedCategories = selectedCategories
            filterState.minRate = minRate
            filterState.maxRate = maxRate
            filterState.sortBy = sortBy
        }
        
        val currentUser by authRepository.currentUser.collectAsState()
        val isAdmin = currentUser?.role?.name == "ADMIN"
        val isDriver = currentUser?.role?.name == "DRIVER"
        val canAddCar = isAdmin || isDriver
        
        val scope = rememberCoroutineScope()
        
        val refreshCars = {
            scope.launch {
                isRefreshing = true
                carsRepository.getCars(forceRefresh = true).onSuccess { carsList ->
                    cars = carsList
                    isRefreshing = false
                }.onFailure { error ->
                    isRefreshing = false
                    errorMessage = error.message
                }
            }
        }
        
        LaunchedEffect(Unit) {
            scope.launch {
                carsRepository.getCars().onSuccess { carsList ->
                    cars = carsList
                    isLoading = false
                }.onFailure { error ->
                    errorMessage = error.message ?: "Failed to load cars"
                    isLoading = false
                }
            }
        }
        
        val filteredCars = remember(cars, showAvailableOnly, searchNearby, maxDistance, brandFilter, selectedCategories, minRate, maxRate, sortBy) {
            var result = cars
            
            if (showAvailableOnly) {
                result = result.filter { it.isActive }
            }
            
            if (brandFilter.isNotBlank()) {
                result = result.filter { car ->
                    car.brand.contains(brandFilter, ignoreCase = true)
                }
            }
            
            if (selectedCategories.isNotEmpty()) {
                result = result.filter { car ->
                    car.category?.label in selectedCategories
                }
            }
            
            result = result.filter { car ->
                car.ratePerHour >= minRate && car.ratePerHour <= maxRate
            }
            
            if (searchNearby) {
                result = result.filter { car ->
                    val distance = calculateDistance(
                        userLat, userLng,
                        car.locationLat, car.locationLng
                    )
                    distance <= maxDistance
                }
            }
            
            when (sortBy) {
                "Price (Low to High)" -> result.sortedBy { it.ratePerHour }
                "Price (High to Low)" -> result.sortedByDescending { it.ratePerHour }
                "Distance" -> {
                    if (searchNearby) {
                        result.sortedBy { car ->
                            calculateDistance(userLat, userLng, car.locationLat, car.locationLng)
                        }
                    } else result
                }
                "Brand" -> result.sortedBy { it.brand }
                else -> result
            }
        }
        
        Scaffold(
            topBar = {
                Column {
                    TopBar {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Available Cars",
                                style = AppTheme.typography.titleLarge,
                                modifier = Modifier.weight(1f).padding(start = 8.dp)
                            )
                            Row {
                                IconButton(
                                    onClick = {
                                        navigator.push(
                                            MapScreen(
                                                cars = filteredCars,
                                                userLat = userLat,
                                                userLng = userLng,
                                                showNearby = searchNearby,
                                                maxDistanceKm = maxDistance
                                            )
                                        )
                                    },
                                    variant = IconButtonVariant.Ghost
                                ) {
                                    Icon(
                                        Icons.Default.Map,
                                        contentDescription = "View map"
                                    )
                                }
                                IconButton(
                                    onClick = { refreshCars() },
                                    enabled = !isRefreshing,
                                    variant = IconButtonVariant.Ghost
                                ) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = "Refresh cars"
                                    )
                                }
                            }
                        }
                    }
                    // LinearProgressIndicator when refreshing
                    if (isRefreshing) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            },
            bottomBar = {
                MainLayoutBottomBar(selectedRoute = "home")
            },
            floatingActionButton = {
                if (canAddCar) {
                    FloatingActionButton(
                        onClick = { navigator.push(AddCarScreen()) },
                        containerColor = AppTheme.colors.primaryContainer,
                        contentColor = AppTheme.colors.onPrimaryContainer
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Car")
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { filtersExpanded = !filtersExpanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Filters",
                                style = AppTheme.typography.titleMedium
                            )
                            Icon(
                                imageVector = if (filtersExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (filtersExpanded) "Collapse filters" else "Expand filters"
                            )
                        }
                        
                        if (filtersExpanded) {
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Show available cars only")
                                    Switch(
                                        checked = showAvailableOnly,
                                        onCheckedChange = { showAvailableOnly = it }
                                    )
                                }
                                
                                Divider()
                                
                                OutlinedTextField(
                                    value = brandFilter,
                                    onValueChange = { brandFilter = it },
                                    label = { Text("Brand") },
                                    placeholder = { Text("e.g., Tesla, BMW") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                
                                Divider()
                                
                                Column {
                                    Text(
                                        text = "Category",
                                        style = AppTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf("ICE", "BEV", "FCEV").forEach { category ->
                                            FilterChip(
                                                selected = category in selectedCategories,
                                                onClick = {
                                                    selectedCategories = if (category in selectedCategories) {
                                                        selectedCategories - category
                                                    } else {
                                                        selectedCategories + category
                                                    }
                                                },
                                                label = { Text(category) }
                                            )
                                        }
                                    }
                                }
                                
                                Divider()
                                
                                Column {
                                    Text(
                                        text = "Rate per hour: €${minRate.toInt()} - €${maxRate.toInt()}",
                                        style = AppTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("€${minRate.toInt()}", style = AppTheme.typography.bodySmall)
                                        androidx.compose.material3.RangeSlider(
                                            value = minRate.toFloat()..maxRate.toFloat(),
                                            onValueChange = { range ->
                                                minRate = range.start.toDouble()
                                                maxRate = range.endInclusive.toDouble()
                                            },
                                            valueRange = 0f..100f,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text("€${maxRate.toInt()}", style = AppTheme.typography.bodySmall)
                                    }
                                }
                                
                                Divider()
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Nearby search")
                                        Text(
                                            "Within $maxDistance km",
                                            style = AppTheme.typography.bodySmall,
                                            color = AppTheme.colors.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                    Switch(
                                        checked = searchNearby,
                                        onCheckedChange = { searchNearby = it }
                                    )
                                }
                                
                                if (searchNearby) {
                                    Slider(
                                        value = maxDistance.toFloat(),
                                        onValueChange = { maxDistance = it.toInt() },
                                        valueRange = 5f..100f,
                                        steps = 18,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                
                                Divider()
                                
                                Column {
                                    Text(
                                        text = "Sort by",
                                        style = AppTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    var expandedSortMenu by remember { mutableStateOf(false) }
                                    OutlinedButton(
                                        onClick = { expandedSortMenu = true },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(sortBy)
                                        Spacer(modifier = Modifier.weight(1f))
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Sort options")
                                    }
                                    DropdownMenu(
                                        expanded = expandedSortMenu,
                                        onDismissRequest = { expandedSortMenu = false }
                                    ) {
                                        listOf(
                                            "Price (Low to High)",
                                            "Price (High to Low)",
                                            "Distance",
                                            "Brand"
                                        ).forEach { option ->
                                            DropdownMenuItem(
                                                text = { Text(option) },
                                                onClick = {
                                                    sortBy = option
                                                    expandedSortMenu = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                Text(
                    text = "${filteredCars.size} car${if (filteredCars.size != 1) "s" else ""} found",
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
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
                                Button(onClick = {
                                    isLoading = true
                                    errorMessage = null
                                    scope.launch {
                                        carsRepository.getCars().onSuccess { carsList ->
                                            cars = carsList
                                            isLoading = false
                                        }.onFailure { error ->
                                            errorMessage = error.message ?: "Failed to load cars"
                                            isLoading = false
                                        }
                                    }
                                }) {
                                    Text("Retry")
                                }
                            }
                        }
                        filteredCars.isEmpty() -> {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (cars.isEmpty()) "No cars available" else "No cars match your filters",
                                    style = AppTheme.typography.bodyLarge
                                )
                                if (cars.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Try adjusting your filters",
                                        style = AppTheme.typography.bodyMedium,
                                        color = AppTheme.colors.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredCars) { car ->
                                    CarListItem(
                                        car = car,
                                        userLat = userLat,
                                        userLng = userLng,
                                        showDistance = searchNearby,
                                        onClick = { navigator.push(CarDetailScreen(car.id)) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Calculates the great-circle distance between two geographic coordinates using the Haversine formula.
 *
 * @param lat1 Latitude of first point in degrees
 * @param lon1 Longitude of first point in degrees
 * @param lat2 Latitude of second point in degrees
 * @param lon2 Longitude of second point in degrees
 * @return Distance in kilometers
 */
private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadiusKm = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(sqrt(a), sqrt(1 - a))
    return earthRadiusKm * c
}

/**
 * Renders a single car item in the list with relevant details.
 *
 * @param car The car data to display
 * @param userLat User's current latitude for distance calculation
 * @param userLng User's current longitude for distance calculation
 * @param showDistance Whether to display distance from user
 * @param onClick Callback when the car item is clicked
 */
@Composable
private fun CarListItem(
    car: CarDto,
    userLat: Double,
    userLng: Double,
    showDistance: Boolean,
    onClick: () -> Unit
) {
    val distance = if (showDistance) {
        calculateDistance(userLat, userLng, car.locationLat, car.locationLng)
    } else null
    val carPhotoApi = remember { SharedModule.carPhotoApi }
    var photoUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(car.id) {
        carPhotoApi.getCarPhotosByCarId(car.id).onSuccess { photos ->
            val primary = photos.firstOrNull { it.isPrimary } ?: photos.firstOrNull()
            photoUrl = primary?.url
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (photoUrl != null) {
                SubcomposeAsyncImage(
                    model = photoUrl,
                    contentDescription = "Car photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
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
                    if (distance != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = AppTheme.colors.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "%.1f km away".format(distance),
                                style = AppTheme.typography.bodySmall,
                                color = AppTheme.colors.primary
                            )
                        }
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "\u20ac${car.ratePerHour.roundSignificand(DecimalMode.US_CURRENCY).toPlainString()}/hr",
                        style = AppTheme.typography.titleMedium,
                        color = AppTheme.colors.primary
                    )
                    if (car.fuelType != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = car.fuelType!!.name,
                            style = AppTheme.typography.bodySmall,
                            color = AppTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (car.isActive) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Available",
                        tint = AppTheme.colors.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Available",
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.primary
                    )
                } else {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Unavailable",
                        tint = AppTheme.colors.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Unavailable",
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.error
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeScreenPreview() {
    AppTheme {
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
                        Text(
                            text = "Available Cars",
                            style = AppTheme.typography.titleLarge,
                            modifier = Modifier.weight(1f).padding(start = 8.dp)
                        )
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.Person, contentDescription = "Profile")
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
                // Preview with sample data
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(3) { index ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Tesla Model ${index + 1}",
                                            style = AppTheme.typography.titleLarge
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "SEDAN",
                                            style = AppTheme.typography.bodyMedium,
                                            color = AppTheme.colors.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                    Text(
                                        text = "€${25 + index * 5}/hr",
                                        style = AppTheme.typography.titleMedium,
                                        color = AppTheme.colors.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Available",
                                        tint = AppTheme.colors.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Available",
                                        style = AppTheme.typography.bodySmall,
                                        color = AppTheme.colors.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun HomeScreenPreviewWrapper() {
    HomeScreenPreview()
}
