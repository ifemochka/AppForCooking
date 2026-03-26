package com.example.appforcooking.domain.usecases

import com.example.appforcooking.data.repositories.ProductRepository
import com.example.appforcooking.domain.models.Product
import com.example.appforcooking.domain.models.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetAllProductsUseCase(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(): Flow<List<Product>> {
        return repository.getAllProducts().map { entities ->
            entities.map { it.toDomain() }
        }
    }
}