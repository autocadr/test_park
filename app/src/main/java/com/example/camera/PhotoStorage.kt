package com.example.camera

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

data class PendingPhotoCapture(
    val file: File,
    val contentUri: Uri,
)

object PhotoStorage {
    fun createCaptureTarget(context: Context): PendingPhotoCapture {
        val dir = File(context.filesDir, "parking_photos").apply {
            if (!exists()) mkdirs()
        }
        val file = File(dir, "pillar_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        return PendingPhotoCapture(file = file, contentUri = uri)
    }

    fun isCapturedFileValid(file: File?): Boolean {
        return file != null && file.exists() && file.length() > 0L
    }

    fun deleteFileQuietly(path: String?) {
        if (path.isNullOrBlank()) return
        runCatching { File(path).delete() }
    }
}
