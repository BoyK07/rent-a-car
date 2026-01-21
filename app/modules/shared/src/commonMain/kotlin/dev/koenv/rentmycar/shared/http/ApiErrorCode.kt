package dev.koenv.rentmycar.shared.http

/**
 * Standardized error codes for programmatic error handling in mobile apps.
 * These codes provide type-safe error identification across the API.
 */
enum class ApiErrorCode {
    // Client errors (4xx)
    BAD_REQUEST,
    UNAUTHORIZED,
    FORBIDDEN,
    NOT_FOUND,
    CONFLICT,
    VALIDATION_ERROR,

    // Business logic errors
    RESERVATION_CONFLICT,
    INVALID_STATE_TRANSITION,
    OWNERSHIP_REQUIRED,
    CAR_NOT_AVAILABLE,
    INVALID_TIME_RANGE,
    DURATION_TOO_SHORT,
    DURATION_TOO_LONG,
    PAST_BOOKING_NOT_ALLOWED,
    CANNOT_RENT_OWN_CAR,

    // Server errors (5xx)
    INTERNAL_SERVER_ERROR,
    SERVICE_UNAVAILABLE
}
