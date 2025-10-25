package dev.koenv.rentmycar.dto.search

import dev.koenv.rentmycar.domain.enums.CarCategory
import dev.koenv.rentmycar.shared.serialization.BigDecimalSerializer
import dev.koenv.rentmycar.shared.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.util.*

@Serializable
data class CarSearchDto(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val brand: String,
    val model: String,
    val category: CarCategory,
    @Serializable(with = BigDecimalSerializer::class)
    val ratePerHour: BigDecimal,
    val distance: Double? = null,
    val locationLat: Double,
    val locationLng: Double,
    val thumbnailUrl: String? = null,
    val isActive: Boolean
)

@Serializable
data class SearchResultDto(
    val cars: List<CarSearchDto>,
    val totalCount: Int,
    val page: Int,
    val totalPages: Int,
    val hasNext: Boolean
)

@Serializable
data class NearbySearchRequestDto(
    val latitude: Double,
    val longitude: Double,
    val radius: Double = 10.0,
    val page: Int = 1,
    val limit: Int = 20
)

