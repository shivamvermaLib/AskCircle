package com.ask.widget

import com.ask.core.FIREBASE_DB
import com.ask.core.FirebaseDataSource
import com.ask.user.User
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
object WidgetFirebaseModules {
    @Singleton
    @Provides
    @Named(TABLE_WIDGETS)
    fun provideWidgetStorageReference(@Named(FIREBASE_DB) storageReference: StorageReference): StorageReference {
        return storageReference.child(TABLE_WIDGETS)
    }


    @Singleton
    @Provides
    @Named(TABLE_WIDGETS)
    fun provideWidgetStorageSource(@Named(TABLE_WIDGETS) storageReference: StorageReference): com.ask.core.FirebaseStorageSource {
        return com.ask.core.FirebaseStorageSource(storageReference)
    }


    @Singleton
    @Provides
    @Named(TABLE_WIDGETS)
    fun provideWidgetReference(@Named(FIREBASE_DB) databaseReference: DatabaseReference): DatabaseReference {
        return databaseReference.child(TABLE_WIDGETS)
    }

    @Singleton
    @Provides
    @Named(TABLE_WIDGET_IDS)
    fun provideWidgetIdsReference(@Named(FIREBASE_DB) databaseReference: DatabaseReference): DatabaseReference {
        return databaseReference.child(TABLE_WIDGET_IDS)
    }


    @Singleton
    @Provides
    fun provideWidgetDataSource(@Named(TABLE_WIDGETS) widgetReference: DatabaseReference): FirebaseDataSource<WidgetWithOptionsAndVotesForTargetAudience> =
        object :
            FirebaseDataSource<WidgetWithOptionsAndVotesForTargetAudience>(
                widgetReference
            ) {
            override fun updateIdForItem(
                t: WidgetWithOptionsAndVotesForTargetAudience,
                id: String
            ): WidgetWithOptionsAndVotesForTargetAudience {
                return t.copy(widget = t.widget.copy(id = id))
            }

            override fun getIdForItem(t: WidgetWithOptionsAndVotesForTargetAudience): String {
                return t.widget.id
            }

            override fun getItemFromDataSnapshot(dataSnapshot: DataSnapshot): WidgetWithOptionsAndVotesForTargetAudience {
                val widget =
                    dataSnapshot.child(WIDGET).getValue(Widget::class.java)!!
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
                    User(id = widget.creatorId),
                    dataSnapshot.child(TABLE_WIDGET_CATEGORIES).children.map {
                        it.getValue(Widget.WidgetCategory::class.java)!!
                    },
                    false
                )
            }

            override fun getItemFromMutableData(mutableData: MutableData): WidgetWithOptionsAndVotesForTargetAudience? {
                val widget = mutableData.child(WIDGET).getValue(Widget::class.java)
                    ?: return null
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
                    User(id = widget.creatorId),
                    mutableData.child(TABLE_WIDGET_CATEGORIES).children.map {
                        it.getValue(Widget.WidgetCategory::class.java)!!
                    },
                    false
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