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
    fun getAllUsers(): Flow<List<User>>

    @Upsert
    suspend fun insertAll(
        users: List<User>,
        userLocations: List<User.UserLocation>,
        userCategories: List<User.UserCategory>
    )

    @Upsert
    suspend fun insertUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Transaction
    @Query("select * from users where id = :id")
    suspend fun getUserDetailsById(id: String): UserWithLocationCategory?

    @Query("SELECT * FROM users where id = :id")
    suspend fun getUserById(id: String): User?

    @Query("select * from users where id in (:ids)")
    suspend fun getUsersByIds(ids: List<String>): List<User>

    @Transaction
    @Query("select * from users where id = :id")
    fun getUserByIdLive(id: String): Flow<UserWithLocationCategory>

    @Query("DELETE FROM users")
    suspend fun deleteAll()
}