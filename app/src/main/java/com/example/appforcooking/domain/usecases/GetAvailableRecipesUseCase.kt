package com.example.appforcooking.domain.usecases

import com.example.appforcooking.data.repositories.RecipeRepository
import com.example.appforcooking.domain.models.Recipe
import kotlinx.coroutines.flow.Flow

class GetAvailableRecipesUseCase(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(userId: Long): Flow<List<Recipe>> {
        return repository.getAvailableRecipes(userId)
    }
}