package dev.koenv.rentmycar.shared.dto.common

import kotlinx.serialization.Serializable

/**
 * Universal pagination wrapper for list responses.
 * Provides consistent pagination metadata across all endpoints.
 */
@Serializable
data class PagedResponseDto<T>(
    val data: List<T>,
    val page: Int,
    val pageSize: Int,
    val totalCount: Int,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
) {
    companion object {
        fun <T> of(
            data: List<T>,
            page: Int,
            pageSize: Int,
            totalCount: Int
        ): PagedResponseDto<T> {
            val totalPages = if (totalCount == 0) 0 else (totalCount + pageSize - 1) / pageSize
            return PagedResponseDto(
                data = data,
                page = page,
                pageSize = pageSize,
                totalCount = totalCount,
                totalPages = totalPages,
                hasNext = page < totalPages,
                hasPrevious = page > 1
            )
        }
    }
}
