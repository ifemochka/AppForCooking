package com.example.appforcooking.domain.usecases

import com.example.appforcooking.data.repositories.RecipeRepository
import com.example.appforcooking.domain.models.Recipe

class GetRecipeByIdUseCase(
    private val recipeRepository: RecipeRepository
) {
    suspend operator fun invoke(recipeId: Long): Recipe? {
        return recipeRepository.getRecipeById(recipeId)
    }
}