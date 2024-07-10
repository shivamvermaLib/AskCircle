package com.ask.app

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