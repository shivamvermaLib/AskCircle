package com.ask.category

import com.ask.core.FirebaseStorageSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Named

class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao,
    @Named(TABLE_CATEGORY) private val categoryFirebaseSource: FirebaseStorageSource,
    @Named("IO") private val dispatcher: CoroutineDispatcher
) {

    fun getAllCategories() = categoryDao.getAllCategories().flowOn(dispatcher)

    suspend fun syncCategories() = withContext(dispatcher) {
        if (categoryDao.hasCategory().not()) {
            val categoriesString = categoryFirebaseSource.download(CATEGORY_JSON)
            val jsonObject = Json.decodeFromString<JsonObject>(categoriesString)
            val categories = jsonObject["categories"]!!.jsonArray
                .map { jsonElement ->
                    val category =
                        Category(name = jsonElement.jsonObject["name"]!!.jsonPrimitive.content)
                    CategoryWithSubCategory(
                        category = category,
                        subCategories = jsonElement.jsonObject["subcategories"]!!.jsonArray
                            .map {
                                SubCategory(
                                    categoryId = category.id,
                                    title = it.jsonPrimitive.content
                                )
                            }
                    )
                }
            categoryDao.insertAll(
                categories.map { it.category },
                categories.map { it.subCategories }.flatten()
            )
        }
    }

    suspend fun clearAll() = withContext(dispatcher) {
        categoryDao.deleteAll()
    }

}