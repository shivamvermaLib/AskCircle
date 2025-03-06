package com.ask.core

data class CreateRemoteConfig(
    val minAge: Int,
    val maxAge: Int,
    val maxYearAllowed: Int,
    val maxOptionSize: Int,
)