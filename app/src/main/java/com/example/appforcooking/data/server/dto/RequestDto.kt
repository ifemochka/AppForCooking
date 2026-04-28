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