package com.example.appforcooking.data.server.dto

import com.google.gson.annotations.SerializedName


data class UserProfileDto(
    @SerializedName("userId")
    val userId: Long,

    val email: String,

    @SerializedName("firstName")
    val firstName: String? = null,

    @SerializedName("lastName")
    val lastName: String? = null,

    @SerializedName("avatarUrl")
    val avatarUrl: String? = null,

    @SerializedName("birthDate")
    val birthDate: String? = null
)
data class PantryItemDto(
    @SerializedName("pantry_item_id")
    val pantryItemId: Long,

    @SerializedName("product_id")
    val productId: Long,

    @SerializedName("product_name")
    val productName: String? = null,

    @SerializedName("is_low")
    val isLow: Boolean = false
)

data class AllergyDto(
    @SerializedName("allergy_id")
    val allergyId: Long,

    @SerializedName("product_id")
    val productId: Long,

    @SerializedName("product_name")
    val productName: String? = null
)

data class ShoppingListItemDto(
    @SerializedName("item_id")
    val itemId: Long,

    @SerializedName("product_id")
    val productId: Long,

    @SerializedName("product_name")
    val productName: String? = null,

    @SerializedName("is_purchased")
    val isPurchased: Boolean = false
)

data class CookingHistoryDto(
    @SerializedName("history_id")
    val historyId: Long,

    @SerializedName("recipe_id")
    val recipeId: Long,

    @SerializedName("recipe_title")
    val recipeTitle: String? = null,

    @SerializedName("cooked_at")
    val cookedAt: Long,

    val rating: Int? = null
)

data class SyncResponse(
    val success: Boolean,

    @SerializedName("userProfile")
    val userProfile: UserProfileDto? = null,

    @SerializedName("pantryItems")
    val pantryItems: List<PantryItemDto> = emptyList(),

    @SerializedName("allergies")
    val allergies: List<AllergyDto> = emptyList(),

    @SerializedName("shoppingList")
    val shoppingList: List<ShoppingListItemDto> = emptyList(),

    @SerializedName("cookingHistory")
    val cookingHistory: List<CookingHistoryDto> = emptyList()
)