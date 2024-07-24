package com.ask.core

import android.text.format.DateFormat
import android.text.format.DateUtils
import java.util.Locale

fun String.checkIfUrl(): Boolean {
    val s: String = this.trim().lowercase(Locale.ROOT)
    return (s.startsWith(HTTP) || s.startsWith(HTTPS) && s.contains(FIREBASE))
}

fun String.extension(): String {
    return substring(lastIndexOf(DOT))
}

fun String.fileNameWithExtension(): String {
    return substring(lastIndexOf(SLASH) + 1)
}

fun String?.toSearchNeededField(predicate: ((String) -> Boolean)? = null): String? {
    return this?.takeIf { it.isNotBlank() && predicate?.invoke(it) ?: true }?.lowercase()
        ?.replace(" ", "-")
}


fun String?.getAllImages(): List<String> {
    return this?.split(IMAGE_SPLIT_FACTOR) ?: emptyList()
}

fun String?.getImage(imageSizeType: ImageSizeType): String? {
    val images = this?.split(IMAGE_SPLIT_FACTOR) ?: return null
    if (images.isEmpty()) return null
    if (images.size == 1) {
        return images[0]
    }
    return images.find { it.contains(imageSizeType.name) } ?: images.find {
        it.contains(
            ImageSizeType.SIZE_ORIGINAL.name
        )
    } ?: images[0]
}

fun Long.toTimeAgo() = DateUtils.getRelativeTimeSpanString(
    this,
    System.currentTimeMillis(),
    DateUtils.MINUTE_IN_MILLIS,
    DateUtils.FORMAT_ABBREV_RELATIVE
).toString()

fun Long.toDate() = DateFormat.format("dd/MM/yyyy", this).toString()