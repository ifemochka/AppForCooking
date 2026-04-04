package com.example.appforcooking.presentation.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appforcooking.data.local.database.CookingDatabase
import com.example.appforcooking.data.repositories.ShoppingListRepository
import com.example.appforcooking.domain.models.Recipe
import com.example.appforcooking.domain.models.RecipeIngredient
import com.example.appforcooking.domain.usecases.GetRecipeByIdUseCase
import com.example.appforcooking.domain.usecases.GetRecipeIngredientsUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecipeDetailViewModel(
    private val recipeId: Long,
    private val context: Context,
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

    private val _isAddingToShoppingList = MutableStateFlow(false)
    val isAddingToShoppingList: StateFlow<Boolean> = _isAddingToShoppingList.asStateFlow()

    private val _shoppingListMessage = MutableStateFlow<String?>(null)
    val shoppingListMessage: StateFlow<String?> = _shoppingListMessage.asStateFlow()

    init {
        loadRecipeData()
    }

    fun addMissingIngredientsToShoppingList() {
        viewModelScope.launch {
            _isAddingToShoppingList.value = true
            try {
                val userId = CookingDatabase.currentUserId
                val database = CookingDatabase.getDatabase(context)
                val repository = ShoppingListRepository(database.shoppingListDao())

                val missingIngredients = _ingredients.value.filter { !it.isAvailable }

                if (missingIngredients.isEmpty()) {
                    _shoppingListMessage.value = "Все продукты уже есть в холодильнике"
                    return@launch
                }

                val productIds = missingIngredients.map { it.productId }
                val success = repository.addMissingIngredients(userId, productIds)

                if (success) {
                    _shoppingListMessage.value = "Добавлено ${missingIngredients.size} продуктов в список покупок"
                } else {
                    _shoppingListMessage.value = "Ошибка добавления в список"
                }

                delay(3000)
                _shoppingListMessage.value = null

            } catch (e: Exception) {
                _shoppingListMessage.value = "Ошибка: ${e.message}"
            } finally {
                _isAddingToShoppingList.value = false
            }
        }
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