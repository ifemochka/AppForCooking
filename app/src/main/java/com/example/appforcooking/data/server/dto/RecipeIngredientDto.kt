package com.example.appforcooking.data.server.dto

import com.google.gson.annotations.SerializedName

data class RecipeIngredientDto(
    @SerializedName("recipe_ingredient_id")
    val recipeIngredientId: Long,

    @SerializedName("recipe_id")
    val recipeId: Long,

    @SerializedName("product_id")
    val productId: Long,

    @SerializedName("quantity")
    val quantity: Double,

    @SerializedName("unit")
    val unit: String
)