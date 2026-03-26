package com.example.appforcooking.domain.models

data class Product(
    val productId: Long = 0,
    val name: String,
    val category: String,
    val defaultUnit: String,
    val caloriesPer100g: Int,
    val barcode: String = ""
)

fun Product.toEntity(): com.example.appforcooking.data.local.database.entities.ProductEntity {
    return com.example.appforcooking.data.local.database.entities.ProductEntity(
        productId = productId,
        name = name,
        category = category,
        defaultUnit = defaultUnit,
        caloriesPer100g = caloriesPer100g,
        barcode = barcode
    )
}

fun com.example.appforcooking.data.local.database.entities.ProductEntity.toDomain(): Product {
    return Product(
        productId = productId,
        name = name,
        category = category,
        defaultUnit = defaultUnit,
        caloriesPer100g = caloriesPer100g,
        barcode = barcode
    )
}