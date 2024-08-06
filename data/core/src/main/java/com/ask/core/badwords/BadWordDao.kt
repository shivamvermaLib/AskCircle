package com.ask.core.badwords

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ask.core.TABLE_BAD_WORDS
import kotlinx.coroutines.flow.Flow

@Dao
interface BadWordDao {

    @Upsert
    suspend fun insertAll(badWords: List<BadWord>)

    @Query("SELECT * FROM $TABLE_BAD_WORDS")
    fun getAllBadWords(): Flow<List<BadWord>>

    @Query("SELECT EXISTS (SELECT 1 FROM bad_words WHERE :providedString LIKE '%' || english || '%')")
    fun containsBadWord(providedString: String): Boolean
}