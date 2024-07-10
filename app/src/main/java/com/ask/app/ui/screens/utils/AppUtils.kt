package com.ask.app.ui.screens.utils

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap

fun Context.getExtension(path: String): String? {
    return MimeTypeMap.getSingleton()
        .getExtensionFromMimeType(contentResolver.getType(Uri.parse(path)))
}

fun Context.getByteArray(path: String): ByteArray? {
    return contentResolver.openInputStream(Uri.parse(path))?.use { it.buffered().readBytes() }
}