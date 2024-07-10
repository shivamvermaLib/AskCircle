package com.ask.app.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ask.app.data.models.Country
import kotlinx.coroutines.flow.Flow

@Dao
interface CountryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(countries: List<Country>)

    @Query("select (select count(*) from countries)  != 0")
    suspend fun hasCountries(): Boolean

    @Query("select * from countries")
    fun getAlCountries(): Flow<List<Country>>
}