package com.example.appforcooking.data.repositories

import com.example.appforcooking.data.server.RetrofitClient
import com.example.appforcooking.data.server.dto.ProductDto
import com.example.appforcooking.data.server.dto.RecipeDto
import com.example.appforcooking.data.server.dto.RecipeIngredientDto
import com.example.appforcooking.domain.models.Product
import com.example.appforcooking.domain.models.Recipe
import com.example.appforcooking.domain.models.RecipeIngredient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ServerRepository {

    suspend fun getAllProducts(): List<Product> = withContext(Dispatchers.IO) {
        try {
            val productsDto = RetrofitClient.apiService.getProducts()
            productsDto.map { dto ->
                Product(
                    productId = dto.productId,
                    name = dto.name,
                    category = dto.category,
                    defaultUnit = dto.defaultUnit,
                    caloriesPer100g = dto.caloriesPer100g,
                    barcode = dto.barcode
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun searchProducts(query: String): List<Product> = withContext(Dispatchers.IO) {
        try {
            if (query.length >= 2) {
                val productsDto = RetrofitClient.apiService.searchProducts(query)
                productsDto.map { dto ->
                    Product(
                        productId = dto.productId,
                        name = dto.name,
                        category = dto.category,
                        defaultUnit = dto.defaultUnit,
                        caloriesPer100g = dto.caloriesPer100g,
                        barcode = dto.barcode
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAllRecipes(): List<Recipe> = withContext(Dispatchers.IO) {
        try {
            val recipesDto = RetrofitClient.apiService.getRecipes()
            recipesDto.map { dto ->
                Recipe(
                    recipeId = dto.recipeId,
                    title = dto.title,
                    description = dto.description,
                    cookingTimeMinutes = dto.cookingTimeMinutes,
                    difficulty = dto.difficulty,
                    imageUrl = dto.imageUrl,
                    caloriesTotal = dto.caloriesTotal,
                    instructions = dto.instructions
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getRecipeById(recipeId: Long): Recipe? = withContext(Dispatchers.IO) {
        try {
            val dto = RetrofitClient.apiService.getRecipeById(recipeId)
            Recipe(
                recipeId = dto.recipeId,
                title = dto.title,
                description = dto.description,
                cookingTimeMinutes = dto.cookingTimeMinutes,
                difficulty = dto.difficulty,
                imageUrl = dto.imageUrl,
                caloriesTotal = dto.caloriesTotal,
                instructions = dto.instructions
            )
        } catch (e: Exception) {
            null
        }
    }
}

object RecipeMapper {

    fun toDomain(dto: ProductDto): Product {
        return Product(
            productId = dto.productId,
            name = dto.name,
            category = dto.category,
            defaultUnit = dto.defaultUnit,
            caloriesPer100g = dto.caloriesPer100g,
            barcode = dto.barcode
        )
    }

    fun toDomain(dto: RecipeDto): Recipe {
        return Recipe(
            recipeId = dto.recipeId,
            title = dto.title,
            description = dto.description,
            cookingTimeMinutes = dto.cookingTimeMinutes,
            difficulty = dto.difficulty,
            imageUrl = dto.imageUrl,
            caloriesTotal = dto.caloriesTotal,
            instructions = dto.instructions
        )
    }

    fun toDomain(dto: RecipeIngredientDto, productName: String = ""): RecipeIngredient {
        return RecipeIngredient(
            recipeIngredientId = dto.recipeIngredientId,
            recipeId = dto.recipeId,
            productId = dto.productId,
            productName = productName,
            quantity = dto.quantity,
            unit = dto.unit,
            isAvailable = false
        )
    }

    fun toDomainList(recipes: List<RecipeDto>): List<Recipe> {
        return recipes.map { toDomain(it) }
    }

    fun toDomainProductList(products: List<ProductDto>): List<Product> {
        return products.map { toDomain(it) }
    }
}
