package dev.koenv.rentmycar.shared.http

import kotlinx.serialization.Serializable

/**
 * Unified API response envelope for all endpoints.
 * Always contains success flag, statusCode, and either data or error details.
 * 
 * This design follows best practices from the Kotlin community:
 * - Single response type for consistency
 * - Clear success/failure discrimination
 * - Nullable fields ensure frontend can handle missing data
 * - No need for sealed classes - simple discriminated union via success flag
 *
 * Success response example:
 * ```json
 * {
 *   "success": true,
 *   "data": { "id": 1, "name": "Example" },
 *   "message": null,
 *   "statusCode": 200,
 *   "error": null
 * }
 * ```
 *
 * Error response example:
 * ```json
 * {
 *   "success": false,
 *   "data": null,
 *   "message": "Resource not found",
 *   "statusCode": 404,
 *   "error": {
 *     "code": "NOT_FOUND",
 *     "traceId": "abc-123",
 *     "details": null
 *   }
 * }
 * ```
 */
@Serializable
data class ApiResponse<T>(
    /**
     * Indicates whether the request was successful.
     * True = success, false = error
     */
    val success: Boolean,
    
    /**
     * The response data when success=true, null when success=false
     */
    val data: T? = null,
    
    /**
     * Human-readable message.
     * - null or empty for successful responses
     * - Error description for failed responses
     */
    val message: String? = null,
    
    /**
     * HTTP status code (200, 201, 404, 500, etc.)
     */
    val statusCode: Int,
    
    /**
     * Structured error information when success=false, null when success=true
     */
    val error: ErrorDetails? = null
) {
    companion object {
        /**
         * Convenience function to create success response
         */
        fun <T> success(
            data: T,
            statusCode: Int = 200,
            message: String? = null
        ): ApiResponse<T> = ApiResponse(
            success = true,
            data = data,
            message = message,
            statusCode = statusCode,
            error = null
        )

        /**
         * Convenience function to create error response
         */
        fun <T> error(
            statusCode: Int,
            message: String,
            code: String,
            traceId: String? = null,
            details: Map<String, String>? = null
        ): ApiResponse<T> = ApiResponse(
            success = false,
            data = null,
            message = message,
            statusCode = statusCode,
            error = ErrorDetails(code = code, traceId = traceId, details = details)
        )
    }
}

/**
 * Structured error details for failed responses
 */
@Serializable
data class ErrorDetails(
    /**
     * Machine-readable error code (e.g., "NOT_FOUND", "VALIDATION_ERROR")
     */
    val code: String,
    
    /**
     * Request trace ID for debugging
     */
    val traceId: String? = null,
    
    /**
     * Additional error details (field-specific errors, etc.)
     */
    val details: Map<String, String>? = null
)
