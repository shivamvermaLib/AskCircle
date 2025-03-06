package com.ask.core.badwords

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ask.core.TABLE_BAD_WORDS
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = TABLE_BAD_WORDS)
data class BadWord(
    @PrimaryKey
    @kotlinx.serialization.Transient
    val id: String = UUID.randomUUID().toString(),
    val english: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)