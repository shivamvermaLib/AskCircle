package com.ask.app.di

import com.ask.app.OPTION
import com.ask.app.OPTIONS
import com.ask.app.TABLE_COUNTRIES
import com.ask.app.TABLE_USERS
import com.ask.app.TABLE_WIDGETS
import com.ask.app.TABLE_WIDGET_IDS
import com.ask.app.TARGET_AUDIENCE_AGE_RANGES
import com.ask.app.TARGET_AUDIENCE_GENDER
import com.ask.app.TARGET_AUDIENCE_LOCATIONS
import com.ask.app.USER
import com.ask.app.USER_LOCATION
import com.ask.app.VOTES
import com.ask.app.WIDGET
import com.ask.app.data.models.User
import com.ask.app.data.models.UserWithLocation
import com.ask.app.data.models.Widget
import com.ask.app.data.models.WidgetId
import com.ask.app.data.models.WidgetWithOptionsAndVotesForTargetAudience
import com.ask.app.data.source.remote.FirebaseDataSource
import com.ask.app.data.source.remote.FirebaseStorageSource
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
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModules {

    @Singleton
    @Provides
    fun provideRemoteConfig() : FirebaseRemoteConfig {
        return Firebase.remoteConfig.apply {
            val configSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = 3600
            }
            setConfigSettingsAsync(configSettings)
        }
    }

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
    @Named(TABLE_USERS)
    fun provideUserStorageReference(firebaseStorage: FirebaseStorage): StorageReference {
        return firebaseStorage.getReference(TABLE_USERS)
    }

    @Singleton
    @Provides
    @Named(TABLE_WIDGETS)
    fun provideWidgetStorageReference(firebaseStorage: FirebaseStorage): StorageReference {
        return firebaseStorage.getReference(TABLE_WIDGETS)
    }

    @Singleton
    @Provides
    @Named(TABLE_COUNTRIES)
    fun provideCountriesStorageReference(firebaseStorage: FirebaseStorage): StorageReference {
        return firebaseStorage.getReference(TABLE_COUNTRIES)
    }

    @Singleton
    @Provides
    @Named(TABLE_USERS)
    fun provideUserStorageSource(@Named(TABLE_USERS) storageReference: StorageReference): FirebaseStorageSource {
        return FirebaseStorageSource(storageReference)
    }

    @Singleton
    @Provides
    @Named(TABLE_WIDGETS)
    fun provideWidgetStorageSource(@Named(TABLE_WIDGETS) storageReference: StorageReference): FirebaseStorageSource {
        return FirebaseStorageSource(storageReference)
    }

    @Singleton
    @Provides
    @Named(TABLE_COUNTRIES)
    fun provideCountriesStorageSource(@Named(TABLE_COUNTRIES) storageReference: StorageReference): FirebaseStorageSource {
        return FirebaseStorageSource(storageReference)
    }

    @Singleton
    @Provides
    @Named(TABLE_USERS)
    fun provideUserReference(firebaseDatabase: FirebaseDatabase): DatabaseReference {
        return firebaseDatabase.getReference(TABLE_USERS)
    }

    @Singleton
    @Provides
    @Named(TABLE_WIDGETS)
    fun provideWidgetReference(firebaseDatabase: FirebaseDatabase): DatabaseReference {
        return firebaseDatabase.getReference(TABLE_WIDGETS)
    }

    @Singleton
    @Provides
    @Named(TABLE_WIDGET_IDS)
    fun provideWidgetIdsReference(firebaseDatabase: FirebaseDatabase): DatabaseReference {
        return firebaseDatabase.getReference(TABLE_WIDGET_IDS)
    }

    @Singleton
    @Provides
    fun provideUserDataSource(@Named(TABLE_USERS) userReference: DatabaseReference): FirebaseDataSource<UserWithLocation> =
        object : FirebaseDataSource<UserWithLocation>(userReference) {
            override fun updateIdForItem(
                t: UserWithLocation,
                id: String
            ): UserWithLocation {
                return t.copy(user = t.user.copy(id = id))
            }

            override fun getIdForItem(t: UserWithLocation): String {
                return t.user.id
            }

            override fun getItemFromDataSnapshot(dataSnapshot: DataSnapshot): UserWithLocation {
                return UserWithLocation(
                    dataSnapshot.child(USER).getValue(User::class.java)!!,
                    dataSnapshot.child(USER_LOCATION).getValue(User.UserLocation::class.java)!!,
                )
            }

            override fun getItemFromMutableData(mutableData: MutableData): UserWithLocation? {
                val user = mutableData.child(USER).getValue(User::class.java) ?: return null
                return UserWithLocation(
                    user,
                    mutableData.child(USER_LOCATION).getValue(User.UserLocation::class.java)!!,
                )
            }
        }

    @Singleton
    @Provides
    fun provideWidgetDataSource(@Named(TABLE_WIDGETS) widgetReference: DatabaseReference): FirebaseDataSource<WidgetWithOptionsAndVotesForTargetAudience> =
        object : FirebaseDataSource<WidgetWithOptionsAndVotesForTargetAudience>(widgetReference) {
            override fun updateIdForItem(
                t: WidgetWithOptionsAndVotesForTargetAudience,
                id: String
            ): WidgetWithOptionsAndVotesForTargetAudience {
                return t.copy(widget = t.widget.copy(id = id))
            }

            override fun getIdForItem(t: WidgetWithOptionsAndVotesForTargetAudience): String {
                return t.widget.id
            }

            override fun getItemFromDataSnapshot(dataSnapshot: DataSnapshot): WidgetWithOptionsAndVotesForTargetAudience? {
                val widget = dataSnapshot.child(WIDGET).getValue(Widget::class.java)!!
                return WidgetWithOptionsAndVotesForTargetAudience(
                    widget,
                    dataSnapshot.child(OPTIONS).children.map {
                        WidgetWithOptionsAndVotesForTargetAudience.OptionWithVotes(
                            it.child(OPTION).getValue(Widget.Option::class.java)!!,
                            it.child(VOTES).children.map { vote ->
                                vote.getValue(Widget.Option.Vote::class.java)!!
                            }
                        )
                    },
                    dataSnapshot.child(TARGET_AUDIENCE_GENDER)
                        .getValue(Widget.TargetAudienceGender::class.java)!!,
                    dataSnapshot.child(TARGET_AUDIENCE_LOCATIONS).children.map {
                        it.getValue(Widget.TargetAudienceLocation::class.java)!!
                    },
                    dataSnapshot.child(TARGET_AUDIENCE_AGE_RANGES)
                        .getValue(Widget.TargetAudienceAgeRange::class.java)!!,
                    User(id = widget.creatorId)
                )
            }

            override fun getItemFromMutableData(mutableData: MutableData): WidgetWithOptionsAndVotesForTargetAudience? {
                val widget = mutableData.child(WIDGET).getValue(Widget::class.java) ?: return null
                return WidgetWithOptionsAndVotesForTargetAudience(
                    widget,
                    mutableData.child(OPTIONS).children.map {
                        WidgetWithOptionsAndVotesForTargetAudience.OptionWithVotes(
                            it.child(OPTION).getValue(Widget.Option::class.java)!!,
                            it.child(VOTES).children.map { vote ->
                                vote.getValue(Widget.Option.Vote::class.java)!!
                            }
                        )
                    },
                    mutableData.child(TARGET_AUDIENCE_GENDER)
                        .getValue(Widget.TargetAudienceGender::class.java)!!,
                    mutableData.child(TARGET_AUDIENCE_LOCATIONS).children.map {
                        it.getValue(Widget.TargetAudienceLocation::class.java)!!
                    },
                    mutableData.child(TARGET_AUDIENCE_AGE_RANGES)
                        .getValue(Widget.TargetAudienceAgeRange::class.java)!!,
                    User(id = widget.creatorId)
                )
            }
        }

    @Singleton
    @Provides
    fun provideCreatedWidgetIdDataSource(@Named(TABLE_WIDGET_IDS) widgetReference: DatabaseReference): FirebaseDataSource<WidgetId> =
        object : FirebaseDataSource<WidgetId>(widgetReference) {
            override fun updateIdForItem(
                t: WidgetId,
                id: String
            ): WidgetId {
                return t.copy(id = id)
            }

            override fun getIdForItem(t: WidgetId): String {
                return t.id
            }

            override fun getItemFromDataSnapshot(dataSnapshot: DataSnapshot): WidgetId? {
                return dataSnapshot.getValue(WidgetId::class.java)
            }

            override fun getItemFromMutableData(mutableData: MutableData): WidgetId? {
                return mutableData.getValue(WidgetId::class.java)
            }
        }

}