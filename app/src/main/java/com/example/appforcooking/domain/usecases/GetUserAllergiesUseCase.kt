    package com.example.appforcooking.domain.usecases

import com.example.appforcooking.data.repositories.ProductRepository
import com.example.appforcooking.domain.models.Product
import com.example.appforcooking.domain.models.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetUserAllergiesUseCase(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(userId: Long): Flow<List<Product>> {
        return repository.getUserAllergies(userId).map{ entities ->
            entities.map { it.toDomain() }
        }
    }
}