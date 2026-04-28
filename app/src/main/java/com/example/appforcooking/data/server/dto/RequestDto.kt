package com.example.appforcooking.data.server.dto

import com.google.gson.annotations.SerializedName

data class PantryAddRequest(
    @SerializedName("userId")
    val userId: Long,

    @SerializedName("productId")
    val productId: Long,

    @SerializedName("isLow")
    val isLow: Boolean = false
)

data class PantryRemoveRequest(
    @SerializedName("userId")
    val userId: Long,

    @SerializedName("productId")
    val productId: Long
)

data class AllergyAddRequest(
    @SerializedName("userId")
    val userId: Long,

    @SerializedName("productId")
    val productId: Long
)

data class AllergyRemoveRequest(
    @SerializedName("userId")
    val userId: Long,

    @SerializedName("productId")
    val productId: Long
)

data class ProfileUpdateRequest(
    @SerializedName("userId")
    val userId: Long,

    @SerializedName("firstName")
    val firstName: String?,

    @SerializedName("lastName")
    val lastName: String?,

    @SerializedName("avatarUrl")
    val avatarUrl: String? = null,

    @SerializedName("birthDate")
    val birthDate: String? = null
)

data class ShoppingListAddRequest(
    @SerializedName("userId")
    val userId: Long,

    @SerializedName("productIds")
    val productIds: List<Long>
)

data class ShoppingListRemoveRequest(
    @SerializedName("userId")
    val userId: Long,

    @SerializedName("productId")
    val productId: Long
)

data class CookingHistoryAddRequest(
    @SerializedName("userId")
    val userId: Long,

    @SerializedName("recipeId")
    val recipeId: Long,

    @SerializedName("rating")
    val rating: Int? = null
)