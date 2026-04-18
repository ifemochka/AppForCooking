package com.example.appforcooking.data.server

import com.example.appforcooking.data.server.dto.AuthResponse
import com.example.appforcooking.data.server.dto.LoginRequest
import com.example.appforcooking.data.server.dto.ProductDto
import com.example.appforcooking.data.server.dto.RecipeDto
import com.example.appforcooking.data.server.dto.RecipeIngredientDto
import com.example.appforcooking.data.server.dto.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @GET("api/products")
    suspend fun getProducts(): List<ProductDto>

    @GET("api/products/{id}")
    suspend fun getProductById(@Path("id") id: Long): ProductDto

    @GET("api/products/search/{query}")
    suspend fun searchProducts(@Path("query") query: String): List<ProductDto>

    @GET("api/recipes")
    suspend fun getRecipes(): List<RecipeDto>

    @GET("api/recipes/{id}")
    suspend fun getRecipeById(@Path("id") id: Long): RecipeDto

    @GET("api/recipes/{recipeId}/ingredients")
    suspend fun getRecipeIngredients(@Path("recipeId") recipeId: Long): List<RecipeIngredientDto>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse
}