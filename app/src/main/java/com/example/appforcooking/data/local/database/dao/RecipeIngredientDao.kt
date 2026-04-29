package com.example.appforcooking.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.appforcooking.data.local.database.entities.RecipeEntity
import com.example.appforcooking.data.local.database.entities.RecipeIngredientEntity

@Dao
interface RecipeIngredientDao {

    @Insert
    suspend fun insertRecipeIngredient(ingredient: RecipeIngredientEntity)

    @Query("DELETE FROM recipe_ingredient")
    suspend fun deleteAllRecipeIngredients()

    @Query("SELECT * FROM recipe_ingredient WHERE recipe_id = :recipeId")
    suspend fun getRecipeIngredients(recipeId: Long): List<RecipeIngredientEntity>
}