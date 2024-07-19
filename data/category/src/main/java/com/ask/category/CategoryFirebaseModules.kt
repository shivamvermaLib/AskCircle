package com.ask.category

import com.ask.core.CONFIGURATIONS
import com.ask.core.FirebaseStorageSource
import com.google.firebase.storage.StorageReference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CategoryFirebaseModules {

    @Singleton
    @Provides
    @Named(TABLE_CATEGORY)
    fun provideCategoryStorageSource(@Named(CONFIGURATIONS) storageReference: StorageReference): FirebaseStorageSource {
        return FirebaseStorageSource(storageReference)
    }
}