package com.example.appforcooking.domain.usecases

import com.example.appforcooking.data.repositories.RecipeRepository
import com.example.appforcooking.domain.models.RecipeIngredient

class GetRecipeIngredientsUseCase(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(recipeId: Long, userId: Long): List<RecipeIngredient> {
        return repository.getRecipeIngredientsWithAvailability(recipeId, userId)
    }
}