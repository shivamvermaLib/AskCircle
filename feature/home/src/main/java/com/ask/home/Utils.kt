package com.ask.home

import android.util.Patterns

fun isValidEmail(target: String): Boolean {
    return target.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(target).matches()
}