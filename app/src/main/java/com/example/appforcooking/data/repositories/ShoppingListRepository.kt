package com.example.appforcooking.data.repositories

import com.example.appforcooking.data.local.database.dao.ShoppingListDao
import com.example.appforcooking.data.local.database.dao.ShoppingListItemWithProduct
import com.example.appforcooking.data.local.database.entities.ShoppingListItemEntity
import kotlinx.coroutines.flow.Flow

class ShoppingListRepository(
    private val dao: ShoppingListDao
) {
    suspend fun addMissingIngredients(userId: Long, productIds: List<Long>): Boolean {
        return try {
            for (productId in productIds) {
                if (!dao.exists(userId, productId)) {
                    dao.insert(
                        ShoppingListItemEntity(
                            userId = userId,
                            productId = productId,
                            isPurchased = false
                        )
                    )
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getItems(userId: Long): Flow<List<ShoppingListItemWithProduct>> {
        return dao.getItems(userId)
    }

    suspend fun togglePurchased(itemId: Long, currentStatus: Boolean) {
        // Получаем существующий элемент
        val existingItem = dao.getItemById(itemId)
        if (existingItem != null) {
            // Обновляем только поле isPurchased, сохраняя остальные данные
            val updatedItem = existingItem.copy(isPurchased = !currentStatus)
            dao.update(updatedItem)
        }
    }

    suspend fun clearPurchased(userId: Long) {
        dao.deletePurchased(userId)
    }
}