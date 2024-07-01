package com.ask.app.di

import com.ask.app.data.models.Poll
import com.ask.app.data.models.User
import com.ask.app.data.source.remote.FirebaseDataSource
import com.ask.app.data.source.remote.FirebaseStorageSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModules {

    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
        return Firebase.auth
    }

    @Provides
    fun provideFirebaseDatabase(): FirebaseDatabase {
        return Firebase.database
    }

    @Provides
    fun provideFirebaseStorage(): FirebaseStorage {
        return Firebase.storage
    }

    @Provides
    @Named("users")
    fun provideUserStorageReference(firebaseStorage: FirebaseStorage): StorageReference {
        return firebaseStorage.getReference("users")
    }

    @Provides
    @Named("polls")
    fun providePollStorageReference(firebaseStorage: FirebaseStorage): StorageReference {
        return firebaseStorage.getReference("polls")
    }

    @Provides
    @Named("polls-options")
    fun providePollOptionStorageReference(firebaseStorage: FirebaseStorage): StorageReference {
        return firebaseStorage.getReference("polls-options")
    }

    @Provides
    @Named("users")
    fun provideUserStorageSource(@Named("users") storageReference: StorageReference): FirebaseStorageSource {
        return FirebaseStorageSource(storageReference)
    }


    @Provides
    @Named("polls")
    fun providePollStorageSource(@Named("polls") storageReference: StorageReference): FirebaseStorageSource {
        return FirebaseStorageSource(storageReference)
    }

    @Provides
    @Named("poll-options")
    fun providePollOptionStorageSource(@Named("polls-options") storageReference: StorageReference): FirebaseStorageSource {
        return FirebaseStorageSource(storageReference)
    }

    @Provides
    @Named("users")
    fun provideUserReference(firebaseDatabase: FirebaseDatabase): DatabaseReference {
        return firebaseDatabase.getReference("users")
    }

    @Provides
    @Named("users-search-fields")
    fun provideUserSearchFieldsReference(firebaseDatabase: FirebaseDatabase): DatabaseReference {
        return firebaseDatabase.getReference("users-search-fields")
    }

    @Provides
    @Named("polls")
    fun providePollReference(firebaseDatabase: FirebaseDatabase): DatabaseReference {
        return firebaseDatabase.getReference("polls")
    }

    @Provides
    @Named("polls-options")
    fun providePollOptionsReference(firebaseDatabase: FirebaseDatabase): DatabaseReference {
        return firebaseDatabase.getReference("polls-options")
    }

    @Provides
    @Named("polls-option-votes")
    fun providePollOptionVotesReference(firebaseDatabase: FirebaseDatabase): DatabaseReference {
        return firebaseDatabase.getReference("polls-option-votes")
    }

    @Provides
    @Named("polls-target-audience")
    fun providePollTargetOptionsReference(firebaseDatabase: FirebaseDatabase): DatabaseReference {
        return firebaseDatabase.getReference("polls-target-audience")
    }

    @Provides
    fun provideUserDataSource(@Named("users") userReference: DatabaseReference): FirebaseDataSource<User> =
        object : FirebaseDataSource<User>(userReference) {
            override fun updateIdForItem(t: User, id: String): User {
                return t.copy(id = id)
            }

            override fun getIdForItem(t: User): String {
                return t.id
            }

            override fun getItemFromDataSnapshot(dataSnapshot: DataSnapshot): User? {
                return dataSnapshot.getValue(User::class.java)
            }
        }

    @Provides
    fun provideUserWithSearchFieldsDataSource(@Named("users-search-fields") userSearchFieldsReference: DatabaseReference): FirebaseDataSource<User.UserSearchFields> =
        object : FirebaseDataSource<User.UserSearchFields>(userSearchFieldsReference) {
            override fun updateIdForItem(t: User.UserSearchFields, id: String): User.UserSearchFields {
                return t.copy(id = id)
            }

            override fun getIdForItem(t: User.UserSearchFields): String {
                return t.id
            }

            override fun getItemFromDataSnapshot(dataSnapshot: DataSnapshot): User.UserSearchFields? {
                return dataSnapshot.getValue(User.UserSearchFields::class.java)
            }
        }


    @Provides
    fun providePollDataSource(@Named("polls") pollReference: DatabaseReference): FirebaseDataSource<Poll> =
        object : FirebaseDataSource<Poll>(pollReference) {
            override fun updateIdForItem(t: Poll, id: String): Poll {
                return t.copy(id = id)
            }

            override fun getIdForItem(t: Poll): String {
                return t.id
            }

            override fun getItemFromDataSnapshot(dataSnapshot: DataSnapshot): Poll? {
                return dataSnapshot.getValue(Poll::class.java)
            }
        }

    @Provides
    fun providePollOptionsDataSource(@Named("polls-options") pollOptionReference: DatabaseReference): FirebaseDataSource<Poll.Option> =
        object : FirebaseDataSource<Poll.Option>(pollOptionReference) {
            override fun updateIdForItem(t: Poll.Option, id: String): Poll.Option {
                return t.copy(id = id)
            }

            override fun getIdForItem(t: Poll.Option): String {
                return t.id
            }

            override fun getItemFromDataSnapshot(dataSnapshot: DataSnapshot): Poll.Option? {
                return dataSnapshot.getValue(Poll.Option::class.java)
            }
        }

    @Provides
    fun providePollTargetAudienceDataSource(@Named("polls-target-audience") pollTargetAudienceReference: DatabaseReference): FirebaseDataSource<Poll.TargetAudience> =
        object : FirebaseDataSource<Poll.TargetAudience>(pollTargetAudienceReference) {
            override fun updateIdForItem(t: Poll.TargetAudience, id: String): Poll.TargetAudience {
                return t.copy(id = id)
            }

            override fun getIdForItem(t: Poll.TargetAudience): String {
                return t.id
            }

            override fun getItemFromDataSnapshot(dataSnapshot: DataSnapshot): Poll.TargetAudience? {
                return dataSnapshot.getValue(Poll.TargetAudience::class.java)
            }
        }

    @Provides
    fun providePollOptionVoteDataSource(@Named("polls-option-votes") pollOptionVotesReference: DatabaseReference): FirebaseDataSource<Poll.Option.Vote> =
        object : FirebaseDataSource<Poll.Option.Vote>(pollOptionVotesReference) {
            override fun updateIdForItem(t: Poll.Option.Vote, id: String): Poll.Option.Vote {
                return t.copy(id = id)
            }

            override fun getIdForItem(t: Poll.Option.Vote): String {
                return t.id
            }

            override fun getItemFromDataSnapshot(dataSnapshot: DataSnapshot): Poll.Option.Vote? {
                return dataSnapshot.getValue(Poll.Option.Vote::class.java)
            }
        }
}