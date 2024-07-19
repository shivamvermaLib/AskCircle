package com.ask.category

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.ask.core.ID
import kotlinx.serialization.Serializable
import java.util.UUID


@Serializable
@Entity(tableName = TABLE_CATEGORY)
data class Category(
    @PrimaryKey
    @kotlinx.serialization.Transient
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(
    tableName = TABLE_SUB_CATEGORY, foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = [ID],
            childColumns = [CATEGORY_ID],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = [CATEGORY_ID])]
)
data class SubCategory(
    @PrimaryKey
    @kotlinx.serialization.Transient
    val id: String = UUID.randomUUID().toString(),
    val categoryId: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class CategoryWithSubCategory(
    @Embedded val category: Category,
    @Relation(
        parentColumn = ID,
        entityColumn = CATEGORY_ID,
        entity = SubCategory::class
    ) val subCategories: List<SubCategory>
)