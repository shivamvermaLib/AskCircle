package com.ask.common

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.ask.core.ImageSizeType
import com.ask.core.checkIfUrl
import java.io.ByteArrayOutputStream
import java.net.URL

fun Context.getExtension(path: String): String? {
    if (path.checkIfUrl()) {
        return URL(path).path.substringAfterLast('.', "")
    }
    return MimeTypeMap.getSingleton()
        .getExtensionFromMimeType(contentResolver.getType(Uri.parse(path)))
}

fun Context.getByteArray(path: String): ByteArray? {
    return contentResolver.openInputStream(Uri.parse(path))?.use { it.buffered().readBytes() }
}


suspend fun Context.getResizedImageByteArray(
    imageUrl: String,
    quality: Int = 80,
    listOfSizes: List<ImageSizeType> = ImageSizeType.entries
): Map<ImageSizeType, ByteArray> {
    // Initialize Coil's ImageLoader
    val imageLoader = ImageLoader(this)

    return listOfSizes.associateWith {
        val height = it.height
        val width = it.width
        // Create an ImageRequest with desired size
        val requestBuilder = ImageRequest.Builder(this)
            .data(imageUrl)
            .allowHardware(false) // Disable hardware bitmaps to be able to convert to byte array

        if (width > 0 && height > 0) {
            requestBuilder.size(width, height) // Resize to specified width and height
        }
        // Build the request
        val request = requestBuilder.build()
        // Execute the request and get the drawable
        val result = (imageLoader.execute(request) as SuccessResult).drawable
        // Convert drawable to bitmap
        val bitmap = result.toBitmap()
        // Convert bitmap to byte array
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
        byteArrayOutputStream.toByteArray()
    }
}

