package com.example.appforcooking.data.repositories

import android.util.Log
import com.example.appforcooking.data.server.ApiService
import com.example.appforcooking.data.server.dto.AllergyAddRequest
import com.example.appforcooking.data.server.dto.PantryAddRequest
import com.example.appforcooking.data.server.dto.PantryRemoveRequest
import com.example.appforcooking.data.server.dto.ProfileUpdateRequest

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
                Log.d(TAG, "✅ Аллергия на продукт $productId добавлена на сервер")
            } else {
                Log.e(TAG, "❌ Ошибка добавления аллергии: ${response.code()} ${response.message()}")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "❌ Исключение: ${e.message}", e)
            false
        }
    }

    suspend fun removeAllergyOnServer(userId: Long, productId: Long): Boolean {
        return try {
            Log.d(TAG, "Отправка на сервер: удаление аллергии на продукт $productId")
            val response = apiService.removeAllergy(userId, productId)
            val success = response.isSuccessful
            if (success) {
                Log.d(TAG, "✅ Аллергия на продукт $productId удалена на сервере")
            } else {
                Log.e(TAG, "❌ Ошибка удаления аллергии: ${response.code()} ${response.message()}")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "❌ Исключение: ${e.message}", e)
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
                Log.d(TAG, "✅ Профиль пользователя $userId обновлен на сервере")
            } else {
                Log.e(TAG, "❌ Ошибка обновления профиля: ${response.code()} ${response.message()}")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "❌ Исключение: ${e.message}", e)
            false
        }
    }
}