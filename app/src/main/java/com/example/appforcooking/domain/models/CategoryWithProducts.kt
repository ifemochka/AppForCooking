package com.example.appforcooking.domain.models

data class CategoryWithProducts(
    val category: String,
    val products: List<Product>,
    var isExpanded: Boolean = false
)
