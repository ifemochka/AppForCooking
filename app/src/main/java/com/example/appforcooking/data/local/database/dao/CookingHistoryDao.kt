package com.example.appforcooking.data.local.database.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.appforcooking.data.local.database.entities.CookingHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CookingHistoryDao {

    @Insert
    suspend fun insert(history: CookingHistoryEntity): Long

    @Query("""
        SELECT ch.*, r.title, r.difficulty, r.cooking_time_minutes, r.image_url
        FROM cooking_history ch
        INNER JOIN recipe r ON ch.recipe_id = r.recipe_id
        WHERE ch.user_id = :userId
        ORDER BY ch.cooked_at DESC
    """)
    fun getHistory(userId: Long): Flow<List<CookingHistoryWithRecipe>>

    @Query("DELETE FROM cooking_history WHERE user_id = :userId AND history_id = :historyId")
    suspend fun deleteHistoryItem(userId: Long, historyId: Long)

    @Query("DELETE FROM cooking_history WHERE user_id = :userId")
    suspend fun clearHistory(userId: Long)
}

data class CookingHistoryWithRecipe(
    @ColumnInfo(name = "history_id") val historyId: Long,
    @ColumnInfo(name = "user_id") val userId: Long,
    @ColumnInfo(name = "recipe_id") val recipeId: Long,
    @ColumnInfo(name = "cooked_at") val cookedAt: Long,
    @ColumnInfo(name = "rating") val rating: Int?,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "difficulty") val difficulty: String,
    @ColumnInfo(name = "cooking_time_minutes") val cookingTimeMinutes: Int,
    @ColumnInfo(name = "image_url") val imageUrl: String?
)