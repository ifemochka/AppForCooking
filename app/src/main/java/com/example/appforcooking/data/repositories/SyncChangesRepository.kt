package com.example.appforcooking.data.repositories

import android.util.Log
import com.example.appforcooking.data.server.ApiService
import com.example.appforcooking.data.server.dto.PantryAddRequest
import com.example.appforcooking.data.server.dto.PantryRemoveRequest

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
}