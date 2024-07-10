package com.ask.app

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ask.app.data.models.Country
import com.ask.app.data.models.User
import com.ask.app.data.models.Widget
import com.ask.app.data.source.local.CountryDao
import com.ask.app.data.source.local.UserDao
import com.ask.app.data.source.local.WidgetDao

@Database(
    entities = [User::class, User.UserLocation::class, User.UserWidget::class, Widget::class, Widget.Option::class, Widget.TargetAudienceGender::class, Widget.TargetAudienceLocation::class, Widget.TargetAudienceAgeRange::class, Widget.Option.Vote::class, Country::class],
    version = 1
)
//@TypeConverters(PollConverters::class)
abstract class AskAppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun widgetDao(): WidgetDao
    abstract fun countryDao(): CountryDao
}