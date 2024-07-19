package com.ask.country

import com.ask.core.CONFIGURATIONS
import com.ask.core.FirebaseStorageSource
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CountryFirebaseModules {

    @Singleton
    @Provides
    @Named(TABLE_COUNTRIES)
    fun provideCountriesStorageSource(@Named(CONFIGURATIONS) storageReference: StorageReference): FirebaseStorageSource {
        return FirebaseStorageSource(storageReference)
    }

}