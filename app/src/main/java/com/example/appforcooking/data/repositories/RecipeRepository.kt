package com.example.appforcooking.data.repositories

import com.example.appforcooking.data.local.database.dao.PantryItemDao
import com.example.appforcooking.data.local.database.dao.ProductDao
import com.example.appforcooking.data.local.database.dao.RecipeDao
import com.example.appforcooking.data.local.mappers.RecipeMapper
import com.example.appforcooking.domain.models.Recipe
import com.example.appforcooking.domain.models.RecipeIngredient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class RecipeRepository(
    private val recipeDao: RecipeDao,
    private val pantryItemDao: PantryItemDao,
    private val productDao: ProductDao
) {
    fun getAllRecipes(): Flow<List<Recipe>> {
        return recipeDao.getAllRecipes().map { RecipeMapper.toDomain(it) }
    }

    fun searchRecipes(query: String): Flow<List<Recipe>> {
        return recipeDao.searchRecipes(query).map { RecipeMapper.toDomain(it) }
    }

    suspend fun getRecipeById(recipeId: Long): Recipe? {
        return recipeDao.getRecipeById(recipeId)?.let { RecipeMapper.toDomain(it) }
    }

    fun getAvailableRecipes(userId: Long): Flow<List<Recipe>> = flow {
        val allRecipes = recipeDao.getAllRecipesSync()

        val userProducts = pantryItemDao.getUserProductsSync(userId)
        val userProductIds = userProducts.map { it.productId }.toSet()

        val availableRecipes = allRecipes.filter { recipe ->
            val requiredIngredients = recipeDao.getRecipeIngredients(recipe.recipeId)
            requiredIngredients.all { ingredient ->
                ingredient.productId in userProductIds
            }
        }

        emit(RecipeMapper.toDomain(availableRecipes))

    }

    //Получение продуктов - ингридиентов рецепта
    suspend fun getRecipeIngredientsWithAvailability(
        recipeId: Long,
        userId: Long
    ): List<RecipeIngredient> {
        val ingredients = recipeDao.getRecipeIngredients(recipeId)

        val userProducts = pantryItemDao.getUserProductsSync(userId)
        val userProductIds = userProducts.map { it.productId }.toSet()

        val result = mutableListOf<RecipeIngredient>()

        for (ingredient in ingredients) {
            val product = productDao.getProductById(ingredient.productId)
            result.add(
                RecipeIngredient(
                    recipeIngredientId = ingredient.recipeIngredientId,
                    recipeId = ingredient.recipeId,
                    productId = ingredient.productId,
                    productName = product.name,
                    quantity = ingredient.quantity,
                    unit = ingredient.unit,
                    isAvailable = ingredient.productId in userProductIds
                )
            )
        }

        return result
    }
}