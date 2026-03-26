package com.example.appforcooking.domain.usecases

import com.example.appforcooking.data.repositories.UserRepository
import com.example.appforcooking.domain.models.UserProfile

class GetUserProfileUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(userId: Long): UserProfile? {
        return repository.getUserProfile(userId)
    }
}