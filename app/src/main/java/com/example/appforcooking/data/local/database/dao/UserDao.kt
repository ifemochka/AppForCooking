package com.example.appforcooking.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.appforcooking.data.local.database.entities.UserEntity

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(user: UserEntity)

    @Query("SELECT * FROM user WHERE user_id = :userId")
    suspend fun getUserById(userId: Long): UserEntity?

    @Query("DELETE FROM user WHERE user_id = :userId")
    suspend fun deleteUser(userId: Long)

    @Query("DELETE FROM user")
    suspend fun deleteAllUsers()
}