package dev.koenv.rentmycar.app.util

import androidx.compose.runtime.Composable

@Composable
expect fun rememberImagePicker(
    onImagePicked: (fileName: String, fileBytes: ByteArray) -> Unit
): () -> Unit

@Composable
expect fun rememberCameraCapture(
    onImageCaptured: (fileName: String, fileBytes: ByteArray) -> Unit
): () -> Unit
