package com.example.appforcooking.domain.models

data class Recipe(
    val recipeId: Long,
    val title: String,
    val description: String,
    val cookingTimeMinutes: Int ,
    val difficulty: String,
    val imageUrl: String? = null,
    val caloriesTotal: Int,
    val instructions: String
)


data class RecipeWithPercentage(
    val recipe: Recipe,
    val percentage: Double
)