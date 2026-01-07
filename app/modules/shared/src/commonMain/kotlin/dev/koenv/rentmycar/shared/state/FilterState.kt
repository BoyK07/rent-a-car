package dev.koenv.rentmycar.shared.state

/**
 * Persistent filter state for the car list.
 * Stored in SharedModule to survive navigation.
 */
data class FilterState(
    var filtersExpanded: Boolean = false,
    var showAvailableOnly: Boolean = true,
    var searchNearby: Boolean = false,
    var maxDistance: Int = 50, // km
    var brandFilter: String = "",
    var selectedCategories: Set<String> = emptySet(),
    var minRate: Double = 0.0,
    var maxRate: Double = 100.0,
    var sortBy: String = "Price (Low to High)"
)
