package com.ask.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ask.app.AskAppDatabase
import com.ask.category.CategoryDao
import com.ask.core.badwords.BadWordDao
import com.ask.country.CountryDao
import com.ask.user.UserDao
import com.ask.widget.WidgetDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModules {

    @Singleton
    @Provides
    fun provideRoomDatabase(@ApplicationContext applicationContext: Context): AskAppDatabase {
        return Room.databaseBuilder(
            applicationContext, AskAppDatabase::class.java, "ask-db"
        ).setJournalMode(RoomDatabase.JournalMode.AUTOMATIC)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideUserDao(askAppDatabase: AskAppDatabase): UserDao {
        return askAppDatabase.userDao()
    }

    @Singleton
    @Provides
    fun providePollDao(askAppDatabase: AskAppDatabase): WidgetDao {
        return askAppDatabase.widgetDao()
    }

    @Singleton
    @Provides
    fun provideCountryDao(askAppDatabase: AskAppDatabase): CountryDao {
        return askAppDatabase.countryDao()
    }

    @Singleton
    @Provides
    fun provideCategoryDao(askAppDatabase: AskAppDatabase): CategoryDao {
        return askAppDatabase.categoryDao()
    }

    @Singleton
    @Provides
    fun provideBadWordDao(askAppDatabase: AskAppDatabase): BadWordDao {
        return askAppDatabase.badWordDao()
    }

    @Singleton
    @Provides
    @Named("db_version")
    fun provideDbVersion(askAppDatabase: AskAppDatabase): Int {
        return askAppDatabase.getVersion()
    }
}