package com.example.appforcooking.domain.models

data class RecipeIngredient(
    val recipeIngredientId: Long,
    val recipeId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Double,
    val unit: String,
    val isAvailable: Boolean = false
)