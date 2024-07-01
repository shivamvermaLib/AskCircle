package com.ask.app.di

import android.content.Context
import androidx.room.Room
import com.ask.app.AskAppDatabase
import com.ask.app.data.source.local.PollDao
import com.ask.app.data.source.local.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RoomModules {

    @Provides
    fun provideRoomDatabase(@ApplicationContext applicationContext: Context): AskAppDatabase {
        return Room.databaseBuilder(
            applicationContext,
            AskAppDatabase::class.java, "ask-db"
        ).build()
    }

    @Provides
    fun provideUserDao(askAppDatabase: AskAppDatabase): UserDao {
        return askAppDatabase.userDao()
    }

    @Provides
    fun providePollDao(askAppDatabase: AskAppDatabase): PollDao {
        return askAppDatabase.pollDao()
    }
}