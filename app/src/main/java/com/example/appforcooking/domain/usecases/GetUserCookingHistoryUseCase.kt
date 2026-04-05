package com.example.appforcooking.domain.usecases

import com.example.appforcooking.data.local.database.dao.CookingHistoryWithRecipe
import com.example.appforcooking.data.repositories.CookingHistoryRepository
import kotlinx.coroutines.flow.Flow

class GetUserCookingHistoryUseCase(
    private val repository: CookingHistoryRepository
) {
    suspend operator fun invoke(userId: Long): Flow<List<CookingHistoryWithRecipe>> {
        return repository.getUserHistory(userId)
    }
}