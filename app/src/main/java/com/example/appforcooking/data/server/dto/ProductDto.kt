package com.example.appforcooking.data.server.dto

import com.google.gson.annotations.SerializedName

data class ProductDto(
    @SerializedName("product_id")
    val productId: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("category")
    val category: String,

    @SerializedName("default_unit")
    val defaultUnit: String,

    @SerializedName("calories_per_100g")
    val caloriesPer100g: Int,

    @SerializedName("barcode")
    val barcode: String
)