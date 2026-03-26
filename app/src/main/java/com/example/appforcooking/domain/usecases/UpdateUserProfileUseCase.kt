package com.example.appforcooking.domain.usecases

import com.example.appforcooking.data.repositories.UserRepository

class UpdateUserProfileUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(userId: Long, firstName: String?, lastName: String?): Boolean {
        return repository.createOrUpdateUserProfile(userId, firstName, lastName)
    }
}