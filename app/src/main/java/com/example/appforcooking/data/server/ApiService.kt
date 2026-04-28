package com.example.appforcooking.data.server

import com.example.appforcooking.data.server.dto.*
import retrofit2.http.*
import retrofit2.Response

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

    @POST("api/user/pantry/add")
    suspend fun addToPantry(@Body request: PantryAddRequest): Response<Map<String, Any>>

    @POST("api/user/allergy/add")
    suspend fun addAllergy(@Body request: AllergyAddRequest): Response<Map<String, Any>>

    @DELETE("api/user/pantry/remove")
    suspend fun removeFromPantry(
        @Query("userId") userId: Long,
        @Query("productId") productId: Long
    ): Response<Map<String, Any>>

    @DELETE("api/user/allergy/remove")
    suspend fun removeAllergy(
        @Query("userId") userId: Long,
        @Query("productId") productId: Long
    ): Response<Map<String, Any>>

    @POST("api/user/profile/update")
    suspend fun updateProfile(@Body request: ProfileUpdateRequest): Response<Map<String, Any>>

    @POST("api/user/shopping-list/add")
    suspend fun addToShoppingList(@Body request: ShoppingListAddRequest): Response<Map<String, Any>>

    @DELETE("api/user/shopping-list/remove")
    suspend fun removeFromShoppingList(
        @Query("userId") userId: Long,
        @Query("productId") productId: Long
    ): Response<Map<String, Any>>

    @POST("api/user/cooking-history/add")
    suspend fun addToCookingHistory(@Body request: CookingHistoryAddRequest): Response<Map<String, Any>>
}