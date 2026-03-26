package com.example.appforcooking.domain.usecases

import com.example.appforcooking.data.repositories.ProductRepository

class RemoveAllergyFromUserUseCase(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(userId: Long, productId: Long) {
        repository.removeAllergyFromUser(userId, productId)
    }
}