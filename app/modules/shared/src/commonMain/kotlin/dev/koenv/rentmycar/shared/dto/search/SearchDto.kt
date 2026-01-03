package dev.koenv.rentmycar.shared.dto.search

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import dev.koenv.rentmycar.shared.domain.enums.CarCategory
import dev.koenv.rentmycar.shared.serialization.BigDecimalSerializer
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class CarSearchDto(
    val id: Uuid,
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
