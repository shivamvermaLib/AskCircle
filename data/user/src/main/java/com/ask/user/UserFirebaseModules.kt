package com.ask.user

import com.ask.core.FIREBASE_DB
import com.ask.core.FirebaseDataSource
import com.ask.core.FirebaseStorageSource
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
    fun provideUserStorageSource(@Named(TABLE_USERS) storageReference: StorageReference): FirebaseStorageSource {
        return FirebaseStorageSource(storageReference)
    }

    @Singleton
    @Provides
    @Named(TABLE_USERS)
    fun provideUserReference(@Named(FIREBASE_DB) databaseReference: DatabaseReference): DatabaseReference {
        return databaseReference.child(TABLE_USERS)
    }

    @Singleton
    @Provides
    fun provideUserDataSource(@Named(TABLE_USERS) userReference: DatabaseReference): FirebaseDataSource<UserWithLocationCategory> =
        object : FirebaseDataSource<UserWithLocationCategory>(userReference) {
            override fun updateIdForItem(
                t: UserWithLocationCategory,
                id: String
            ): UserWithLocationCategory {
                return t.copy(user = t.user.copy(id = id))
            }

            override fun getIdForItem(t: UserWithLocationCategory): String {
                return t.user.id
            }

            override fun getItemFromDataSnapshot(dataSnapshot: DataSnapshot): UserWithLocationCategory {
                return UserWithLocationCategory(
                    dataSnapshot.child(USER).getValue(User::class.java)!!,
                    dataSnapshot.child(USER_LOCATION)
                        .getValue(User.UserLocation::class.java)!!,
                    dataSnapshot.child(USER_CATEGORY).children.map {
                        it.getValue(User.UserCategory::class.java)!!
                    },
                    dataSnapshot.child(USER_WIDGET_BOOKMARK).children.map {
                        it.getValue(User.UserWidgetBookmarks::class.java)!!
                    }
                )
            }

            override fun getItemFromMutableData(mutableData: MutableData): UserWithLocationCategory? {
                val user =
                    mutableData.child(USER).getValue(User::class.java) ?: return null
                return UserWithLocationCategory(
                    user,
                    mutableData.child(USER_LOCATION).getValue(User.UserLocation::class.java)!!,
                    mutableData.child(USER_CATEGORY).children.map {
                        it.getValue(User.UserCategory::class.java)!!
                    },
                    mutableData.child(USER_WIDGET_BOOKMARK).children.map {
                        it.getValue(User.UserWidgetBookmarks::class.java)!!
                    }
                )
            }
        }

}