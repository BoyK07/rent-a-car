package dev.koenv.rentmycar.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.datetime.LocalDateTime

@Composable
expect fun DateTimePickerField(
    label: String,
    dateTime: LocalDateTime,
    onDateTimeSelected: (LocalDateTime) -> Unit,
    modifier: Modifier = Modifier
)
