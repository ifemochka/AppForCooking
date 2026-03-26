package com.example.appforcooking.domain.models

data class UserProfile(
    val userId: Long,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val birthDate: String?,
    val avatarUrl: String?
)