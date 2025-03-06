package com.ask.country

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCountryUseCase @Inject constructor(
    private val countryRepository: CountryRepository,
) {

    operator fun invoke(): Flow<List<Country>> {
        return countryRepository.getCountries()
    }

}