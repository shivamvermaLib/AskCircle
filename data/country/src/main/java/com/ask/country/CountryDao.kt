package com.ask.country

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CountryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(countries: List<Country>)

    @Query("select (select count(*) from countries)  != 0")
    suspend fun hasCountries(): Boolean

    @Query("select * from countries")
    fun getAllCountriesFlow(): Flow<List<Country>>

    @Query("select * from countries")
    fun getCountryList() : List<Country>

    @Query("delete from countries")
    suspend fun deleteAll()
}