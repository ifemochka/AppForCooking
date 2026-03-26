// presentation/viewmodels/RecipeViewModel.kt
package com.example.appforcooking.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appforcooking.data.local.database.CookingDatabase
import com.example.appforcooking.data.local.database.dao.AllergyDao
import com.example.appforcooking.data.local.database.dao.PantryItemDao
import com.example.appforcooking.data.local.database.dao.RecipeDao
import com.example.appforcooking.domain.models.Recipe
import com.example.appforcooking.domain.models.ShownRecipes
import com.example.appforcooking.domain.usecases.GetAllRecipesUseCase
import com.example.appforcooking.domain.usecases.GetAvailableRecipesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RecipeViewModel(
    private val getAllRecipesUseCase: GetAllRecipesUseCase,
    private val getAvailableRecipesUseCase: GetAvailableRecipesUseCase,
    private val recipeDao: RecipeDao,
    private val allergyDao: AllergyDao
) : ViewModel() {

    private val _allRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    private val _shownRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val shownRecipes: StateFlow<List<Recipe>> = _shownRecipes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _selectedTab = MutableStateFlow(0) // 0 - все рецепты, 1 - доступные
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Объект для фильтрации
    private val _shownRecipesConfig = MutableStateFlow(ShownRecipes.all())
    val shownRecipesConfig: StateFlow<ShownRecipes> = _shownRecipesConfig.asStateFlow()

    init {
        loadRecipes()
    }

    fun loadRecipes() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                if (_selectedTab.value == 0) {
                    // Загружаем все рецепты
                    getAllRecipesUseCase().collectLatest { recipesList ->
                        _allRecipes.value = recipesList
                        applyFilter()
                        _isLoading.value = false
                    }
                } else {
                    // Загружаем только доступные рецепты
                    val userId = CookingDatabase.currentUserId
                    getAvailableRecipesUseCase(userId).collectLatest { recipesList ->
                        _allRecipes.value = recipesList
                        applyFilter()
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки рецептов: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun selectTab(index: Int) {
        if (_selectedTab.value != index) {
            _selectedTab.value = index
            loadRecipes()
        }
    }

    // Применяем фильтр к рецептам
    private fun applyFilter() {
        viewModelScope.launch {
            val config = _shownRecipesConfig.value
            val userId = CookingDatabase.currentUserId

            // Получаем ID продуктов, на которые есть аллергия
            val allergyProductIds = allergyDao.getAllergiesForUserSync(userId).map { it.productId }.toSet()

            _shownRecipes.value = _allRecipes.value.filter { recipe ->
                // Фильтр по сложности
                val difficultyMatches = when (recipe.difficulty) {
                    "Легко" -> config.showEasy
                    "Средне" -> config.showMid
                    "Сложно" -> config.showHard
                    else -> true
                }

                // Фильтр по аллергиям
                val allergyMatches = if (config.hideAllergyRecipes) {
                    // Если hideAllergyRecipes = true, показываем все рецепты
                    true
                } else {
                    // Если hideAllergyRecipes = false, проверяем, нет ли аллергий в рецепте
                    val ingredients = recipeDao.getRecipeIngredients(recipe.recipeId)
                    val hasAllergy = ingredients.any { ingredient ->
                        ingredient.productId in allergyProductIds
                    }
                    !hasAllergy // Показываем только рецепты без аллергенов
                }

                difficultyMatches && allergyMatches
            }
        }
    }

    // Обновляем настройки фильтрации
    fun updateShownRecipesConfig(newConfig: ShownRecipes) {
        _shownRecipesConfig.value = newConfig
        applyFilter()
    }
}