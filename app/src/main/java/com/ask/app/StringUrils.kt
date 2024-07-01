package com.ask.app

import java.util.Locale

fun String.checkIfUrl(): Boolean {
    val s: String = this.trim().lowercase(Locale.ROOT)
    return s.startsWith("http://") || s.startsWith("https://")
}

fun String.extension(): String {
    return substring(lastIndexOf("."))
}

fun String.fileNameWithExtension(): String {
    return substring(lastIndexOf("/") + 1)
}