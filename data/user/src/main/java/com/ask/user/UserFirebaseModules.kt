package com.ask.user

import com.ask.core.FIREBASE_DB
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.MutableData
import com.google.firebase.storage.StorageReference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserFirebaseModules {

    @Singleton
    @Provides
    @Named(TABLE_USERS)
    fun provideUserStorageReference(@Named(FIREBASE_DB) storageReference: StorageReference): StorageReference {
        return storageReference.child(TABLE_USERS)
    }


    @Singleton
    @Provides
    @Named(TABLE_USERS)
    fun provideUserStorageSource(@Named(TABLE_USERS) storageReference: StorageReference): com.ask.core.FirebaseStorageSource {
        return com.ask.core.FirebaseStorageSource(storageReference)
    }


    @Singleton
    @Provides
    @Named(TABLE_USERS)
    fun provideUserReference(@Named(FIREBASE_DB) databaseReference: DatabaseReference): DatabaseReference {
        return databaseReference.child(TABLE_USERS)
    }

    @Singleton
    @Provides
    fun provideUserDataSource(@Named(TABLE_USERS) userReference: DatabaseReference): com.ask.core.FirebaseDataSource<UserWithLocation> =
        object : com.ask.core.FirebaseDataSource<UserWithLocation>(userReference) {
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
                    dataSnapshot.child(USER_LOCATION)
                        .getValue(User.UserLocation::class.java)!!,
                )
            }

            override fun getItemFromMutableData(mutableData: MutableData): UserWithLocation? {
                val user =
                    mutableData.child(USER).getValue(User::class.java) ?: return null
                return UserWithLocation(
                    user,
                    mutableData.child(USER_LOCATION)
                        .getValue(User.UserLocation::class.java)!!,
                )
            }
        }

}