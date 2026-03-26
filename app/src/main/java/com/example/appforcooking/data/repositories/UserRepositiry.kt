package com.example.appforcooking.data.repositories

import com.example.appforcooking.data.local.database.dao.UserDao
import com.example.appforcooking.data.local.database.dao.UserProfileDao
import com.example.appforcooking.data.local.database.entities.UserProfileEntity
import com.example.appforcooking.domain.models.UserProfile

class UserRepository(
    private val userDao: UserDao,
    private val userProfileDao: UserProfileDao
) {
    suspend fun getUserProfile(userId: Long): UserProfile? {
        val user = userDao.getUserById(userId) ?: return null
        val profile = userProfileDao.getUserProfileByUserId(userId)

        return UserProfile(
            userId = user.userId,
            email = user.email,
            firstName = profile?.firstName,
            lastName = profile?.lastName,
            birthDate = profile?.birthDate,
            avatarUrl = profile?.avatarUrl
        )
    }

    suspend fun createOrUpdateUserProfile(
        userId: Long,
        firstName: String?,
        lastName: String?
    ): Boolean {
        return try {
            val existingProfile = userProfileDao.getUserProfileByUserId(userId)

            if (existingProfile == null) {
                val newProfile = UserProfileEntity(
                    userId = userId,
                    firstName = firstName,
                    lastName = lastName,
                    birthDate = null,
                    avatarUrl = null
                )
                userProfileDao.insertUserProfile(newProfile)
            } else {
                val updatedProfile = existingProfile.copy(
                    firstName = firstName,
                    lastName = lastName
                )
                userProfileDao.updateUserProfile(updatedProfile)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}