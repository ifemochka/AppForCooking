package com.example.appforcooking.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.appforcooking.data.local.database.entities.RecipeEntity
import com.example.appforcooking.data.local.database.entities.RecipeIngredientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipe ORDER BY title")
    fun getAllRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipe WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchRecipes(query: String): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipe LIMIT :limit")
    fun getRecipesWithLimit(limit: Int): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipe WHERE recipe_id = :recipeId")
    suspend fun getRecipeById(recipeId: Long): RecipeEntity?

    @Query("SELECT * FROM recipe")
    suspend fun getAllRecipesSync(): List<RecipeEntity>

    @Query("SELECT * FROM recipe_ingredient WHERE recipe_id = :recipeId")
    suspend fun getRecipeIngredients(recipeId: Long): List<RecipeIngredientEntity>

    @Insert
    suspend fun insertRecipe(recipe: RecipeEntity)

    @Query("DELETE FROM recipe")
    suspend fun deleteAllRecipes()

    @Insert
    suspend fun insertRecipeIngredient(ingredient: RecipeIngredientEntity)
}