package com.ask.core

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreFirebaseModules {
    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
        return Firebase.auth
    }

    @Singleton
    @Provides
    fun provideFirebaseDatabase(): FirebaseDatabase {
        return Firebase.database
    }

    @Singleton
    @Provides
    fun provideFirebaseStorage(): FirebaseStorage {
        return Firebase.storage
    }

    @Singleton
    @Provides
    @Named(TABLE_UPDATED_TIME)
    fun provideUpdatedTimeReference(firebaseDatabase: FirebaseDatabase): DatabaseReference {
        return firebaseDatabase.getReference(TABLE_UPDATED_TIME)
    }

    @Singleton
    @Provides
    fun provideUpdatedTimeDataOneSource(@Named(TABLE_UPDATED_TIME) databaseReference: DatabaseReference): FirebaseOneDataSource<UpdatedTime> =
        object : FirebaseOneDataSource<UpdatedTime>(databaseReference) {
            override fun getItemFromMutableData(mutableData: MutableData): UpdatedTime? {
                return mutableData.getValue(UpdatedTime::class.java)
            }

            override fun getItemFromDataSnapshot(dataSnapshot: DataSnapshot): UpdatedTime? {
                return dataSnapshot.getValue(UpdatedTime::class.java)
            }

        }

    @Singleton
    @Provides
    fun provideAnalytics(): FirebaseAnalytics {
        return Firebase.analytics
    }

    @Singleton
    @Provides
    fun provideRemoteConfig(): FirebaseRemoteConfig {
        return Firebase.remoteConfig.apply {
            val configSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = 3600
            }
            setConfigSettingsAsync(configSettings)
        }
    }
}