package com.example.appforcooking.data.repositories

import com.example.appforcooking.data.local.database.dao.CookingHistoryDao
import com.example.appforcooking.data.local.database.dao.CookingHistoryWithRecipe
import com.example.appforcooking.data.local.database.entities.CookingHistoryEntity
import kotlinx.coroutines.flow.Flow

class CookingHistoryRepository(
    private val historyDao: CookingHistoryDao
) {

    suspend fun addToHistory(userId: Long, recipeId: Long): Boolean {
        return try {
            historyDao.insert(
                CookingHistoryEntity(
                    userId = userId,
                    recipeId = recipeId
                )
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getUserHistory(userId: Long): Flow<List<CookingHistoryWithRecipe>> {
        return historyDao.getHistory(userId)
    }
}