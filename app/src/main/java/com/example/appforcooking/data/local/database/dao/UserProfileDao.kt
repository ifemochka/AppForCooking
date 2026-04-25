package com.example.appforcooking.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.appforcooking.data.local.database.entities.UserProfileEntity

@Dao
interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfileEntity): Long

    @Update
    suspend fun updateUserProfile(profile: UserProfileEntity)

    @Query("SELECT * FROM user_profile WHERE user_id = :userId")
    suspend fun getUserProfileByUserId(userId: Long): UserProfileEntity?

    @Query("SELECT * FROM user_profile WHERE profile_id = :profileId")
    suspend fun getUserProfileById(profileId: Long): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: UserProfileEntity)

    @Query("SELECT * FROM user_profile WHERE user_id = :userId")
    suspend fun getByUserId(userId: Long): UserProfileEntity?

    @Query("DELETE FROM user_profile WHERE user_id = :userId")
    suspend fun deleteByUserId(userId: Long)
}