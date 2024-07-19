package com.ask.app

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    entities = [User::class, User.UserLocation::class, User.UserCategory::class, Widget::class, Widget.Option::class, Widget.TargetAudienceGender::class, Widget.TargetAudienceLocation::class, Widget.TargetAudienceAgeRange::class, Widget.WidgetCategory::class, Widget.Option.Vote::class, Country::class, Category::class, SubCategory::class],
    version = 5
)
//@TypeConverters(PollConverters::class)
abstract class AskAppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun widgetDao(): WidgetDao
    abstract fun countryDao(): CountryDao
    abstract fun categoryDao(): CategoryDao
    fun getVersion(): Int {
        return this.openHelper.writableDatabase.version
    }

    companion object {
        val migration_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE category ( id TEXT NOT NULL, name TEXT NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL, PRIMARY KEY(id))")
                db.execSQL("CREATE TABLE sub_category ( id TEXT NOT NULL, categoryId TEXT NOT NULL, title TEXT NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL, PRIMARY KEY(id), FOREIGN KEY(categoryId) REFERENCES category(id) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX index_sub_category_categoryId ON sub_category (categoryId)")
            }
        }
    }
}