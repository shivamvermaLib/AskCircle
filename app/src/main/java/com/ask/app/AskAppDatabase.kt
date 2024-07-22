package com.ask.app

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ask.category.Category
import com.ask.category.CategoryDao
import com.ask.category.SubCategory
import com.ask.country.Country
import com.ask.country.CountryDao
import com.ask.user.User
import com.ask.user.UserDao
import com.ask.widget.Widget
import com.ask.widget.WidgetDao

@Database(
    entities = [User::class, User.UserLocation::class, User.UserCategory::class, User.UserWidgetBookmarks::class, Widget::class, Widget.Option::class, Widget.TargetAudienceGender::class, Widget.TargetAudienceLocation::class, Widget.TargetAudienceAgeRange::class, Widget.WidgetCategory::class, Widget.Option.Vote::class, Country::class, Category::class, SubCategory::class],
    version = 7
)
abstract class AskAppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun widgetDao(): WidgetDao
    abstract fun countryDao(): CountryDao
    abstract fun categoryDao(): CategoryDao
    fun getVersion(): Int {
        return this.openHelper.writableDatabase.version
    }
}