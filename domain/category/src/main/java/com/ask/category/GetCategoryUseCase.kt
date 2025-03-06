package com.ask.category

import javax.inject.Inject

class GetCategoryUseCase @Inject constructor(private val categoryRepository: CategoryRepository) {

    operator fun invoke() = categoryRepository.getAllCategoriesFlow()

}