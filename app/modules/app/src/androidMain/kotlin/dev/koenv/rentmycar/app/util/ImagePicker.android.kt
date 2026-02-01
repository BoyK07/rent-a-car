package dev.koenv.rentmycar.app.util

import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.io.ByteArrayOutputStream

@Composable
actual fun rememberImagePicker(
    onImagePicked: (fileName: String, fileBytes: ByteArray) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val fileName = getFileName(context, uri) ?: "photo.jpg"
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@rememberLauncherForActivityResult
        onImagePicked(fileName, bytes)
    }

    return { launcher.launch("image/*") }
}

@Composable
actual fun rememberCameraCapture(
    onImageCaptured: (fileName: String, fileBytes: ByteArray) -> Unit
): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap == null) return@rememberLauncherForActivityResult
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        onImageCaptured("camera_photo.jpg", outputStream.toByteArray())
    }

    return { launcher.launch(null) }
}

private fun getFileName(context: android.content.Context, uri: Uri): String? {
    var name: String? = null
    if (uri.scheme == "content") {
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    name = it.getString(index)
                }
            }
        }
    }
    return name ?: uri.lastPathSegment
}
