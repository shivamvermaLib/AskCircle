package com.ask.user

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Transaction
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<com.ask.user.User>>

    @Upsert
    suspend fun insertAll(
        users: List<com.ask.user.User>,
        userLocations: List<com.ask.user.User.UserLocation>
    )

    @Upsert
    suspend fun insertUser(user: com.ask.user.User)

    @Delete
    suspend fun deleteUser(user: com.ask.user.User)

    @Transaction
    @Query("select * from users where id = :id")
    suspend fun getUserDetailsById(id: String): com.ask.user.UserWithLocation?

    @Query("SELECT * FROM users where id = :id")
    suspend fun getUserById(id: String): com.ask.user.User?

    @Query("select * from users where id in (:ids)")
    suspend fun getUsersByIds(ids: List<String>): List<com.ask.user.User>

    @Transaction
    @Query("select * from users where id = :id")
    fun getUserByIdLive(id: String): Flow<com.ask.user.UserWithLocation>
}