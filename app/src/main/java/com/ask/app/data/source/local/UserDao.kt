package com.ask.app.data.source.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.ask.app.data.models.User
import com.ask.app.data.models.UserWithSearchFields
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Transaction
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserWithSearchFields>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg users: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg userSearchFields: User.UserSearchFields)

    @Delete
    fun deleteUser(user: User)

    @Delete
    fun deleteUserSearchFields(userSearchFields: User.UserSearchFields)

    @Transaction
    @Query("select * from users where id = :id")
    fun getUserById(id: String): UserWithSearchFields?

    @Transaction
    @Query("select * from users where id = :id")
    fun getUserByIdLive(id: String): Flow<UserWithSearchFields>
}