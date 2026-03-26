package com.example.appforcooking.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appforcooking.data.local.database.CookingDatabase
import com.example.appforcooking.domain.models.Recipe
import com.example.appforcooking.domain.models.RecipeIngredient
import com.example.appforcooking.domain.usecases.GetRecipeByIdUseCase
import com.example.appforcooking.domain.usecases.GetRecipeIngredientsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecipeDetailViewModel(
    private val recipeId: Long,
    private val getRecipeByIdUseCase: GetRecipeByIdUseCase,
    private val getRecipeIngredientsUseCase: GetRecipeIngredientsUseCase
) : ViewModel() {

    private val _recipe = MutableStateFlow<Recipe?>(null)
    val recipe: StateFlow<Recipe?> = _recipe.asStateFlow()

    private val _ingredients = MutableStateFlow<List<RecipeIngredient>>(emptyList())
    val ingredients: StateFlow<List<RecipeIngredient>> = _ingredients.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _availableCount = MutableStateFlow(0)
    val availableCount: StateFlow<Int> = _availableCount.asStateFlow()

    init {
        loadRecipeData()
    }

    private fun loadRecipeData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val recipeData = getRecipeByIdUseCase(recipeId)
                _recipe.value = recipeData

                val userId = CookingDatabase.currentUserId
                val ingredientsList = getRecipeIngredientsUseCase(recipeId, userId)
                _ingredients.value = ingredientsList

                _availableCount.value = ingredientsList.count { it.isAvailable }

            } catch (e: Exception) {

            } finally {
                _isLoading.value = false
            }
        }
    }
}