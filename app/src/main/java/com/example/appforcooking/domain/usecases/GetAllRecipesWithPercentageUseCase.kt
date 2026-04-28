package com.example.appforcooking.domain.usecases

import com.example.appforcooking.data.repositories.RecipeRepository
import com.example.appforcooking.domain.models.RecipeWithPercentage
import kotlinx.coroutines.flow.Flow

class GetAllRecipesWithPercentageUseCase(
    private val repository: RecipeRepository
) {
    operator fun invoke(userId: Long): Flow<List<RecipeWithPercentage>> {
        return repository.getAllRecipesWithPercentage(userId)
    }
}