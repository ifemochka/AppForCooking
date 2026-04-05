package com.example.appforcooking.domain.usecases

import com.example.appforcooking.data.repositories.CookingHistoryRepository

class AddToCookingHistoryUseCase(
    private val repository: CookingHistoryRepository
) {
    suspend operator fun invoke(userId: Long, recipeId: Long): Boolean {
        return repository.addToHistory(userId, recipeId)
    }
}