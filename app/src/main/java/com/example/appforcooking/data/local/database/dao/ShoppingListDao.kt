package com.example.appforcooking.data.local.database.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.appforcooking.data.local.database.entities.ShoppingListItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {

    @Insert
    suspend fun insert(item: ShoppingListItemEntity): Long

    @Update
    suspend fun update(item: ShoppingListItemEntity)

    @Query("SELECT * FROM shopping_list_item WHERE id = :itemId")
    suspend fun getItemById(itemId: Long): ShoppingListItemEntity?

    @Query("""
        SELECT sli.*, p.name as product_name, p.category, p.default_unit
        FROM shopping_list_item sli
        INNER JOIN product p ON sli.product_id = p.product_id
        WHERE sli.user_id = :userId
        ORDER BY sli.is_purchased ASC, sli.id DESC
    """)
    fun getItems(userId: Long): Flow<List<ShoppingListItemWithProduct>>

    @Query("SELECT COUNT(*) > 0 FROM shopping_list_item WHERE user_id = :userId AND product_id = :productId")
    suspend fun exists(userId: Long, productId: Long): Boolean

    @Query("DELETE FROM shopping_list_item WHERE user_id = :userId AND is_purchased = 1")
    suspend fun deletePurchased(userId: Long)

    @Query("DELETE FROM shopping_list_item WHERE user_id = :userId")
    suspend fun deleteAllForUser(userId: Long)
}

data class ShoppingListItemWithProduct(
    @ColumnInfo(name = "id") val id: Long,
    @ColumnInfo(name = "user_id") val userId: Long,
    @ColumnInfo(name = "product_id") val productId: Long,
    @ColumnInfo(name = "is_purchased") val isPurchased: Boolean,
    @ColumnInfo(name = "product_name") val productName: String,
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "default_unit") val defaultUnit: String
)