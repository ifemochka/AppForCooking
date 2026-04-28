package com.example.appforcooking.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appforcooking.data.local.database.CookingDatabase
import com.example.appforcooking.data.local.database.dao.AllergyDao
import com.example.appforcooking.data.local.database.dao.RecipeDao
import com.example.appforcooking.domain.models.Recipe
import com.example.appforcooking.domain.models.RecipeWithPercentage
import com.example.appforcooking.domain.models.ShownRecipes
import com.example.appforcooking.domain.usecases.GetAllRecipesUseCase
import com.example.appforcooking.domain.usecases.GetAllRecipesWithPercentageUseCase
import com.example.appforcooking.domain.usecases.GetAvailableRecipesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RecipeViewModel(
    private val getAllRecipesUseCase: GetAllRecipesUseCase,
    private val getAllRecipesWithPercentageUseCase: GetAllRecipesWithPercentageUseCase,
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

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

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
                val userId = CookingDatabase.currentUserId

                if (_selectedTab.value == 0) {
                    getAllRecipesWithPercentageUseCase(userId).collectLatest { recipesWithPercentage ->
                        _allRecipes.value = recipesWithPercentage.map { it.recipe }
                        applyFilter()
                        _isLoading.value = false
                    }
                } else {
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

    private fun applyFilter() {
        viewModelScope.launch {
            val config = _shownRecipesConfig.value
            val userId = CookingDatabase.currentUserId

            val allergyProductIds = allergyDao.getAllergiesForUserSync(userId).map { it.productId }.toSet()

            _shownRecipes.value = _allRecipes.value.filter { recipe ->
                val difficultyMatches = when (recipe.difficulty) {
                    "Легко" -> config.showEasy
                    "Средне" -> config.showMid
                    "Сложно" -> config.showHard
                    else -> true
                }

                val allergyMatches = if (config.hideAllergyRecipes) {
                    true
                } else {
                    val ingredients = recipeDao.getRecipeIngredients(recipe.recipeId)
                    val hasAllergy = ingredients.any { ingredient ->
                        ingredient.productId in allergyProductIds
                    }
                    !hasAllergy
                }

                difficultyMatches && allergyMatches
            }
        }
    }

    fun updateShownRecipesConfig(newConfig: ShownRecipes) {
        _shownRecipesConfig.value = newConfig
        applyFilter()
    }
}