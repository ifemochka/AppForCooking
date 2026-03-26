package com.example.appforcooking.domain.usecases

import com.example.appforcooking.data.repositories.RecipeRepository
import com.example.appforcooking.domain.models.Recipe
import kotlinx.coroutines.flow.Flow

class GetAllRecipesUseCase (
    private val recipeRepository: RecipeRepository
) {
    operator fun invoke(): Flow<List<Recipe>> {
        return recipeRepository.getAllRecipes()
    }
}