package com.example.appforcooking.data.repositories

import android.content.Context
import android.util.Log
import com.example.appforcooking.data.local.database.CookingDatabase
import com.example.appforcooking.data.local.database.entities.*
import com.example.appforcooking.data.server.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncRepository(
    private val context: Context,
    private val apiService: ApiService
) {

    private val TAG = "SyncRepository"

    suspend fun syncUserData(userId: Long): Boolean = withContext(Dispatchers.IO) {
        Log.d(TAG, "Синхронизация для userId: $userId")

        try {
            val database = CookingDatabase.getDatabase(context)

            Log.d(TAG, "Загрузка продуктов с сервера")
            val productsFromServer = apiService.getProducts()
            if (productsFromServer.isNotEmpty()) {
                database.productDao().deleteAllProducts()
                productsFromServer.forEach { productDto ->
                    database.productDao().insertProduct(
                        ProductEntity(
                            productId = productDto.productId,
                            name = productDto.name,
                            category = productDto.category,
                            defaultUnit = productDto.defaultUnit,
                            caloriesPer100g = productDto.caloriesPer100g,
                            barcode = productDto.barcode
                        )
                    )
                }
                Log.d(TAG, "Продукты загружены (${productsFromServer.size})")
            } else {
                Log.w(TAG, "Нет продуктов на сервере")
            }

            Log.d(TAG, "Загрузка рецептов с сервера")
            val recipesFromServer = apiService.getRecipes()
            if (recipesFromServer.isNotEmpty()) {
                database.recipeDao().deleteAllRecipes()
                recipesFromServer.forEach { recipeDto ->
                    database.recipeDao().insertRecipe(
                        RecipeEntity(
                            recipeId = recipeDto.recipeId,
                            title = recipeDto.title,
                            description = recipeDto.description,
                            cookingTimeMinutes = recipeDto.cookingTimeMinutes,
                            difficulty = recipeDto.difficulty,
                            imageUrl = recipeDto.imageUrl,
                            caloriesTotal = recipeDto.caloriesTotal,
                            instructions = recipeDto.instructions
                        )
                    )
                }
                Log.d(TAG, "Рецепты загружены (${recipesFromServer.size})")
            } else {
                Log.w(TAG, "Нет рецептов на сервере")
            }

            Log.d(TAG, "Загрузка ингредиентов рецептов с сервера")
            val recipesIngredientsFromServer = apiService.getRecipeIngredients()
            if (recipesIngredientsFromServer.isNotEmpty()) {
                database.recipeIngredientDao().deleteAllRecipeIngredients()
                recipesIngredientsFromServer.forEach { recipeIngredientDto ->
                    database.recipeIngredientDao().insertRecipeIngredient(
                        RecipeIngredientEntity(
                            recipeIngredientId = recipeIngredientDto.recipeIngredientId,
                            recipeId = recipeIngredientDto.recipeId,
                            productId = recipeIngredientDto.productId,
                            quantity = recipeIngredientDto.quantity,
                            unit = recipeIngredientDto.unit
                        )
                    )
                }
                Log.d(TAG, "Ингредиенты рецептов загружены (${recipesIngredientsFromServer.size})")
            } else {
                Log.w(TAG, "Нет ингредиентов рецептов на сервере")
            }


            Log.d(TAG, "Загрузка пользовательских данных для userId=$userId")
            val response = apiService.syncUserData(userId)
            Log.d(TAG, "Ответ сервера: success=${response.success}")

            if (response.success) {
                response.userProfile?.let { profile ->
                    database.userDao().deleteAllUsers()

                    val user = UserEntity(
                        userId = profile.userId,
                        email = profile.email,
                        passwordHash = "",
                        createdAt = System.currentTimeMillis()
                    )
                    database.userDao().insertOrReplace(user)
                    Log.d(TAG, "Создан пользователь в таблице user: userId=${profile.userId}, email=${profile.email}")



                    database.userProfileDao().deleteAllUsers()
                    val userProfile = UserProfileEntity(
                        profileId = profile.userId,
                        userId = profile.userId,
                        firstName = profile.firstName ?: "",
                        lastName = profile.lastName ?: "",
                        birthDate = profile.birthDate,
                        avatarUrl = profile.avatarUrl
                    )
                    database.userProfileDao().insertOrUpdate(userProfile)
                    Log.d(TAG, "Профиль сохранен: userId=${profile.userId}, email=${profile.email}")
                }

                Log.d(TAG, "Добавлено в инвентарь: ${response.pantryItems.size} продуктов")
                database.pantryItemDao().deleteAllForUser(userId)

                var pantryCount = 0
                response.pantryItems.forEach { item ->
                    if (item.productId > 0) {
                        val product = database.productDao().getProductById(item.productId)
                        if (product != null) {
                            database.pantryItemDao().insertPantryItem(
                                PantryItemEntity(
                                    pantryItemId = item.pantryItemId,
                                    userId = userId,
                                    productId = item.productId,
                                    isLow = item.isLow
                                )
                            )
                            pantryCount++
                            Log.d(TAG, "Добавлен продукт: ${product.name} (ID=${item.productId})")
                        } else {
                            Log.w(TAG, "Продукт ID=${item.productId} не найден в локальной БД")
                        }
                    } else {
                        Log.w(TAG, "Пропущен продукт с ID=${item.productId}")
                    }
                }
                Log.d(TAG, "Кладовка обновлена: добавлено $pantryCount из ${response.pantryItems.size}")

                Log.d(TAG, "Обработка аллергий: ${response.allergies.size}")
                database.allergyDao().deleteAllForUser(userId)

                var allergyCount = 0
                response.allergies.forEach { allergy ->
                    if (allergy.productId > 0) {
                        database.allergyDao().insertAllergy(
                            AllergyEntity(
                                allergyId = allergy.allergyId,
                                userId = userId,
                                productId = allergy.productId
                            )
                        )
                        allergyCount++
                    }
                }
                Log.d(TAG, "Аллергии обновлены: добавлено $allergyCount")

                Log.d(TAG, "Обработка списка покупок: ${response.shoppingList.size}")
                database.shoppingListDao().deleteAllForUser(userId)

                response.shoppingList.forEach { item ->
                    database.shoppingListDao().insert(
                        ShoppingListItemEntity(
                            id = item.itemId,
                            userId = userId,
                            productId = item.productId,
                            isPurchased = item.isPurchased
                        )
                    )
                }
                Log.d(TAG, "Список покупок обновлен")

                Log.d(TAG, "Обработка истории готовки: ${response.cookingHistory.size}")
                database.cookingHistoryDao().deleteAllForUser(userId)

                response.cookingHistory.forEach { history ->
                    database.cookingHistoryDao().insert(
                        CookingHistoryEntity(
                            historyId = history.historyId,
                            userId = userId,
                            recipeId = history.recipeId,
                            cookedAt = history.cookedAt,
                            rating = history.rating
                        )
                    )
                }
                Log.d(TAG, "История готовки обновлена")

                return@withContext true
            } else {
                Log.e(TAG, "Ошибка: response.success = false")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при синхронизации: ${e.message}", e)
        }
        return@withContext false
    }
}