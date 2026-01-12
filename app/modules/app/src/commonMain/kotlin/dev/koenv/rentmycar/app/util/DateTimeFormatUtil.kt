package dev.koenv.rentmycar.app.util

import kotlinx.datetime.LocalDateTime

/**
 * Formats a LocalDateTime to a human-readable string.
 * Example: "Jan 15, 2024 at 2:30 PM"
 */
fun formatDateTime(dateTime: LocalDateTime): String {
    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val month = months[dateTime.monthNumber - 1]
    val day = dateTime.dayOfMonth
    val year = dateTime.year
    
    val hour = if (dateTime.hour == 0) 12 else if (dateTime.hour > 12) dateTime.hour - 12 else dateTime.hour
    val minute = dateTime.minute.toString().padStart(2, '0')
    val amPm = if (dateTime.hour < 12) "AM" else "PM"
    
    return "$month $day, $year at $hour:$minute $amPm"
}
