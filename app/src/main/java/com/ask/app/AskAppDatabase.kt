package com.ask.app

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ask.country.Country
import com.ask.country.CountryDao
import com.ask.user.User
import com.ask.user.UserDao
import com.ask.widget.Widget
import com.ask.widget.WidgetDao

@Database(
    entities = [User::class, User.UserLocation::class, Widget::class, Widget.Option::class, Widget.TargetAudienceGender::class, Widget.TargetAudienceLocation::class, Widget.TargetAudienceAgeRange::class, Widget.Option.Vote::class, Country::class],
    version = 1
)
//@TypeConverters(PollConverters::class)
abstract class AskAppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun widgetDao(): WidgetDao
    abstract fun countryDao(): CountryDao
}