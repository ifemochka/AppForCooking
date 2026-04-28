package com.example.appforcooking.data.repositories

import android.util.Log
import com.example.appforcooking.data.server.ApiService
import com.example.appforcooking.data.server.dto.AllergyAddRequest
import com.example.appforcooking.data.server.dto.CookingHistoryAddRequest
import com.example.appforcooking.data.server.dto.PantryAddRequest
import com.example.appforcooking.data.server.dto.PantryRemoveRequest
import com.example.appforcooking.data.server.dto.ProfileUpdateRequest
import com.example.appforcooking.data.server.dto.ShoppingListAddRequest
import com.example.appforcooking.data.server.dto.UpdateShoppingItemStatusRequest

class SyncChangesRepository(
    private val apiService: ApiService
) {
    private val TAG = "SyncChangesRepository"

    suspend fun addProductToPantryOnServer(userId: Long, productId: Long, isLow: Boolean = false): Boolean {
        return try {
            Log.d(TAG, "Отправка на сервер: добавление продукта $productId пользователю $userId")
            val response = apiService.addToPantry(PantryAddRequest(userId, productId, isLow))
            val success = response.isSuccessful
            if (success) {
                Log.d(TAG, "Продукт $productId успешно добавлен на сервер")
            } else {
                Log.e(TAG, "Ошибка добавления на сервер: ${response.code()} ${response.message()}")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "Исключение при добавлении на сервер: ${e.message}", e)
            false
        }
    }

    suspend fun removeProductFromPantryOnServer(userId: Long, productId: Long): Boolean {
        return try {
            Log.d(TAG, "Отправка на сервер: удаление продукта $productId у пользователя $userId")
            val response = apiService.removeFromPantry(userId, productId)
            val success = response.isSuccessful
            if (success) {
                Log.d(TAG, "Продукт $productId успешно удален на сервере")
            } else {
                Log.e(TAG, "Ошибка удаления на сервере: ${response.code()} ${response.message()}")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "Исключение при удалении на сервере: ${e.message}", e)
            false
        }
    }

    suspend fun addAllergyOnServer(userId: Long, productId: Long): Boolean {
        return try {
            Log.d(TAG, "Отправка на сервер: добавление аллергии на продукт $productId")
            val response = apiService.addAllergy(AllergyAddRequest(userId, productId))
            val success = response.isSuccessful
            if (success) {
                Log.d(TAG, "Аллергия на продукт $productId добавлена на сервер")
            } else {
                Log.e(TAG, "Ошибка добавления аллергии: ${response.code()} ${response.message()}")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "Исключение: ${e.message}", e)
            false
        }
    }

    suspend fun removeAllergyOnServer(userId: Long, productId: Long): Boolean {
        return try {
            Log.d(TAG, "Отправка на сервер: удаление аллергии на продукт $productId")
            val response = apiService.removeAllergy(userId, productId)
            val success = response.isSuccessful
            if (success) {
                Log.d(TAG, "Аллергия на продукт $productId удалена на сервере")
            } else {
                Log.e(TAG, "Ошибка удаления аллергии: ${response.code()} ${response.message()}")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "Исключение: ${e.message}", e)
            false
        }
    }

    suspend fun updateProfileOnServer(
        userId: Long,
        firstName: String?,
        lastName: String?
    ): Boolean {
        return try {
            Log.d(TAG, "Отправка на сервер: обновление профиля пользователя $userId")
            val response = apiService.updateProfile(
                ProfileUpdateRequest(
                    userId = userId,
                    firstName = firstName,
                    lastName = lastName
                )
            )
            val success = response.isSuccessful
            if (success) {
                Log.d(TAG, "Профиль пользователя $userId обновлен на сервере")
            } else {
                Log.e(TAG, "шибка обновления профиля: ${response.code()} ${response.message()}")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "Исключение: ${e.message}", e)
            false
        }
    }

    suspend fun addToShoppingListOnServer(userId: Long, productIds: List<Long>): Boolean {
        return try {
            Log.d(TAG, "Отправка на сервер: добавление ${productIds.size} продуктов в список покупок")
            val response = apiService.addToShoppingList(ShoppingListAddRequest(userId, productIds))
            val success = response.isSuccessful
            if (success) {
                Log.d(TAG, "Продукты добавлены в список покупок на сервере")
            } else {
                Log.e(TAG, "Ошибка добавления в список покупок: ${response.code()} ${response.message()}")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "Исключение: ${e.message}", e)
            false
        }
    }

    suspend fun addToCookingHistoryOnServer(userId: Long, recipeId: Long, rating: Int? = null): Boolean {
        return try {
            Log.d(TAG, "Отправка на сервер: добавление рецепта $recipeId в историю приготовлений")
            val response = apiService.addToCookingHistory(
                CookingHistoryAddRequest(
                    userId,
                    recipeId,
                    rating
                )
            )
            val success = response.isSuccessful
            if (success) {
                Log.d(TAG, "Рецепт добавлен в историю приготовлений на сервере")
            } else {
                Log.e(TAG, "Ошибка добавления в историю: ${response.code()} ${response.message()}")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "Исключение: ${e.message}", e)
            false
        }
    }

    suspend fun removePurchasedProducts(userId: Long, productId: Long): Boolean {
        return try {
            Log.d(TAG, "Отправка на сервер: удаление продукта из списка покупок на продукт $productId")
            val response = apiService.removeFromShoppingList(userId, productId)
            val success = response.isSuccessful
            if (success) {
                Log.d(TAG, "Продукт из списка покупок $productId удален на сервере")
            } else {
                Log.e(TAG, "Ошибка удаления: ${response.code()} ${response.message()}")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "Исключение: ${e.message}", e)
            false
        }
    }

    suspend fun updateShoppingItemStatusOnServer(userId: Long, productId: Long, isPurchased: Boolean): Boolean {
        return try {
            Log.d(TAG, "Отправка на сервер: обновление статуса продукта $productId -> purchased=$isPurchased")
            val response = apiService.updateShoppingItemStatus(
                UpdateShoppingItemStatusRequest(userId, productId, isPurchased)
            )
            val success = response.isSuccessful
            if (success) {
                Log.d(TAG, "Статус продукта $productId обновлен на сервере")
            } else {
                Log.e(TAG, "Ошибка обновления статуса: ${response.code()} ${response.message()}")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "Исключение: ${e.message}", e)
            false
        }
    }
}