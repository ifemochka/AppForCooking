package com.example.appforcooking.data.server

import com.example.appforcooking.data.server.dto.*
import retrofit2.http.*

interface ApiService {

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @GET("api/products")
    suspend fun getProducts(): List<ProductDto>

    @GET("api/products/{id}")
    suspend fun getProductById(@Path("id") id: Long): ProductDto

    @GET("api/products/search/{query}")
    suspend fun searchProducts(@Path("query") query: String): List<ProductDto>

    @GET("api/recipes")
    suspend fun getRecipes(): List<RecipeDto>

    @GET("api/recipeIngredients")
    suspend fun getRecipeIngredients(): List<RecipeIngredientDto>

    @GET("api/recipes/{id}")
    suspend fun getRecipeById(@Path("id") id: Long): RecipeDto

    @GET("api/user/sync")
    suspend fun syncUserData(@Query("userId") userId: Long): SyncResponse
}