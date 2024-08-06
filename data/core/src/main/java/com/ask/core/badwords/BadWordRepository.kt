package com.ask.core.badwords

import com.ask.core.BAD_WORDS_JSON
import com.ask.core.DISPATCHER_IO
import com.ask.core.FirebaseStorageSource
import com.ask.core.TABLE_BAD_WORDS
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Named

class BadWordRepository @Inject constructor(
    @Named(TABLE_BAD_WORDS) private val badWordFirebaseSource: FirebaseStorageSource,
    @Named(DISPATCHER_IO) private val dispatcher: CoroutineDispatcher,
    private val badWordDao: BadWordDao
) {

    suspend fun syncBadWords() = withContext(dispatcher) {
        val badWordString = badWordFirebaseSource.download(BAD_WORDS_JSON)
        val badWordList = Json.decodeFromString<List<String>>(badWordString).let {
            it.map { word -> BadWord(english = word) }
        }
        badWordDao.insertAll(badWordList)
    }

    fun getAllBadWords() = badWordDao.getAllBadWords().flowOn(dispatcher)

    fun checkIfBadWordExists(sentence: String) = badWordDao.containsBadWord(sentence)
}