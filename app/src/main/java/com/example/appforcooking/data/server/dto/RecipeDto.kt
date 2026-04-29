package com.example.appforcooking.data.server.dto

import com.google.gson.annotations.SerializedName

data class RecipeDto(
    @SerializedName("recipe_id")
    val recipeId: Long,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("cooking_time_minutes")
    val cookingTimeMinutes: Int,

    @SerializedName("difficulty")
    val difficulty: String,

    @SerializedName("image_url")
    val imageUrl: String?,

    @SerializedName("calories_total")
    val caloriesTotal: Int,

    @SerializedName("instructions")
    val instructions: String
)