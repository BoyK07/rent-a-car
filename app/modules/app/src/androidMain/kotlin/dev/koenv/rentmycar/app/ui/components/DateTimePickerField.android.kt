package dev.koenv.rentmycar.app.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.text.format.DateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import dev.koenv.rentmycar.app.util.formatDateTime
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

@Composable
actual fun DateTimePickerField(
    label: String,
    dateTime: LocalDateTime,
    onDateTimeSelected: (LocalDateTime) -> Unit,
    modifier: Modifier
) {
    val context = LocalContext.current
    val formatted = formatDateTime(dateTime)

    fun openPicker() {
        val date = LocalDate(dateTime.year, dateTime.monthNumber, dateTime.dayOfMonth)
        val is24Hour = DateFormat.is24HourFormat(context)

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newDate = LocalDate(year, month + 1, dayOfMonth)
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        val updated = LocalDateTime(
                            newDate.year,
                            newDate.monthNumber,
                            newDate.dayOfMonth,
                            hour,
                            minute,
                            0,
                            0
                        )
                        onDateTimeSelected(updated)
                    },
                    dateTime.hour,
                    dateTime.minute,
                    is24Hour
                ).apply {
                    setTitle("$label - time")
                }.show()
            },
            date.year,
            date.monthNumber - 1,
            date.dayOfMonth
        ).apply {
            setTitle("$label - date")
        }.show()
    }

    Box(modifier = modifier.clickable { openPicker() }) {
        OutlinedTextField(
            value = formatted,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
