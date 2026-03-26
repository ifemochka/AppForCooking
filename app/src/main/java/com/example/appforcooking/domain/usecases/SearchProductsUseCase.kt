package com.example.appforcooking.domain.usecases

import com.example.appforcooking.data.repositories.ProductRepository
import com.example.appforcooking.domain.models.Product
import com.example.appforcooking.domain.models.toDomain

class SearchProductsUseCase(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(query: String): List<Product> {
        return repository.searchProducts(query).map { it.toDomain() }
    }
}