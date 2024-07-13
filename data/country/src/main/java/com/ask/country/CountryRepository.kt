package com.ask.country

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Named

class CountryRepository @Inject constructor(
    @Named(TABLE_COUNTRIES) private val countryStorageSource: com.ask.core.FirebaseStorageSource,
    private val countryDao: CountryDao,
    @Named("IO") private val dispatcher: CoroutineDispatcher
) {

    suspend fun syncCountries() = withContext(dispatcher) {
        if (countryDao.hasCountries().not()) {
            val countriesString = countryStorageSource.download(COUNTRY_JSON)
            val countries =
                Json { ignoreUnknownKeys = true }.decodeFromString<List<Country>>(countriesString)
            countryDao.insertAll(countries)
        }
    }

    fun getCountries() = countryDao.getAlCountries().flowOn(dispatcher)

}