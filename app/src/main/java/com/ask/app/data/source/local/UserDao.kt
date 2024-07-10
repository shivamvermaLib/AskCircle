package com.ask.app.data.source.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.ask.app.data.models.User
import com.ask.app.data.models.UserWithLocation
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Transaction
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Upsert
    suspend fun insertAll(
        users: List<User>,
        userLocations: List<User.UserLocation>
    )

    @Upsert
    suspend fun insertUser(user: User)

    @Upsert
    suspend fun insertUserWidgets(userWidgets: List<User.UserWidget>)

    @Delete
    suspend fun deleteUser(user: User)

    @Transaction
    @Query("select * from users where id = :id")
    suspend fun getUserDetailsById(id: String): UserWithLocation?

    @Query("SELECT * FROM users where id = :id")
    suspend fun getUserById(id: String): User?

    @Query("select * from users where id in (:ids)")
    suspend fun getUsersByIds(ids: List<String>): List<User>

    @Transaction
    @Query("select * from users where id = :id")
    fun getUserByIdLive(id: String): Flow<UserWithLocation>
}