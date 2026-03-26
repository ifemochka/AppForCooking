package com.example.appforcooking.domain.usecases

import com.example.appforcooking.data.repositories.ProductRepository

class AddProductToPantryUseCase(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(userId: Long, productId: Long) {
        repository.addProductToUser(userId, productId)
    }
}