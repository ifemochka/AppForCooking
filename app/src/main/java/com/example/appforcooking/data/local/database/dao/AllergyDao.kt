package com.example.appforcooking.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.appforcooking.data.local.database.entities.AllergyEntity
import com.example.appforcooking.data.local.database.entities.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AllergyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllergy(allergy: AllergyEntity): Long

    @Query("SELECT * FROM allergy WHERE user_id = :userId AND product_id = :productId")
    suspend fun getAllergy(userId: Long, productId: Long): AllergyEntity?

    @Query("""
        SELECT p.* FROM product p
        INNER JOIN allergy a ON p.product_id = a.product_id
        WHERE a.user_id = :userId
    """)
    fun getUserAllergies(userId: Long): Flow<List<ProductEntity>>

    @Query("DELETE FROM allergy WHERE user_id = :userId AND product_id = :productId")
    suspend fun deleteAllergy(userId: Long, productId: Long)

    @Query("SELECT * FROM allergy WHERE user_id = :userId")
    suspend fun getAllergiesForUserSync(userId: Long): List<AllergyEntity>

    @Query("DELETE FROM allergy WHERE user_id = :userId")
    suspend fun deleteAllForUser(userId: Long)
}