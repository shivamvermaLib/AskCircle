package com.ask.app

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ask.app.data.models.Poll
import com.ask.app.data.models.PollConverters
import com.ask.app.data.models.User
import com.ask.app.data.source.local.PollDao
import com.ask.app.data.source.local.UserDao

@Database(
    entities = [User::class, User.UserSearchFields::class, Poll::class, Poll.Option::class, Poll.TargetAudience::class, Poll.Option.Vote::class],
    version = 1
)
@TypeConverters(PollConverters::class)
abstract class AskAppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun pollDao(): PollDao
}