package com.ask.app.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ask.app.TABLE_COUNTRIES
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = TABLE_COUNTRIES)
data class Country(
    @PrimaryKey
    @kotlinx.serialization.Transient
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val emoji: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
