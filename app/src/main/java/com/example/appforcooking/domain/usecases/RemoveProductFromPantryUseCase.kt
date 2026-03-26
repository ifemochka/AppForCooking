package com.example.appforcooking.domain.usecases

import com.example.appforcooking.data.repositories.ProductRepository

class RemoveProductFromPantryUseCase(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(userId: Long, productId: Long) {
        repository.removeProductFromUser(userId, productId)
    }
}