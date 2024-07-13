package com.ask.country

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
    fun provideCountriesStorageReference(firebaseStorage: FirebaseStorage): StorageReference {
        return firebaseStorage.getReference(TABLE_COUNTRIES)
    }

    @Singleton
    @Provides
    @Named(TABLE_COUNTRIES)
    fun provideCountriesStorageSource(@Named(TABLE_COUNTRIES) storageReference: StorageReference): FirebaseStorageSource {
        return FirebaseStorageSource(storageReference)
    }

}