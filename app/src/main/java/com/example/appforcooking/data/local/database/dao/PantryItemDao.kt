package com.example.appforcooking.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.appforcooking.data.local.database.entities.PantryItemEntity
import com.example.appforcooking.data.local.database.entities.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PantryItemDao {
    @Insert
    suspend fun insertPantryItem(pantryItem: PantryItemEntity)

    @Query("""
        SELECT p.* FROM product p
        INNER JOIN pantry_item pi ON p.product_id = pi.product_id
        WHERE pi.user_id = :userId
    """)
    fun getUserProducts(userId: Long): Flow<List<ProductEntity>>

    @Query("""
        SELECT p.*, pi.is_low FROM product p
        INNER JOIN pantry_item pi ON p.product_id = pi.product_id
        WHERE pi.user_id = :userId AND p.category = :category
    """)
    suspend fun getUserProductsByCategory(userId: Long, category: String): List<ProductEntity>

    @Query("SELECT * FROM pantry_item WHERE user_id = :userId AND product_id = :productId")
    suspend fun getPantryItem(userId: Long, productId: Long): PantryItemEntity?

    @Query("DELETE FROM pantry_item WHERE user_id = :userId AND product_id = :productId")
    suspend fun removeFromPantry(userId: Long, productId: Long)

    @Query("UPDATE pantry_item SET is_low = :isLow WHERE user_id = :userId AND product_id = :productId")
    suspend fun updateLowStatus(userId: Long, productId: Long, isLow: Boolean)

    @Query("""
        SELECT p.* FROM product p
        INNER JOIN pantry_item pi ON p.product_id = pi.product_id
        WHERE pi.user_id = :userId
    """)
    suspend fun getUserProductsSync(userId: Long): List<ProductEntity>
}