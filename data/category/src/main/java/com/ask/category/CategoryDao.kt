package com.ask.category

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Upsert
    suspend fun insertAll(categories: List<Category>, subCategories: List<SubCategory>)

    @Transaction
    @Query("select * from category")
    fun getAllCategories(): Flow<List<CategoryWithSubCategory>>

    @Query("delete from category")
    suspend fun deleteAll()

    @Query("delete from sub_category")
    suspend fun deleteAllSubCategory()

    @Query("select (select count(*) from category)  != 0")
    suspend fun hasCategory(): Boolean

}