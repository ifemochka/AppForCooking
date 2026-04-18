package com.example.appforcooking.data.server.dto

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("firstName")
    val firstName: String,

    @SerializedName("lastName")
    val lastName: String
)

data class LoginRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)

data class AuthResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("token")
    val token: String?,

    @SerializedName("userId")
    val userId: Long?,

    @SerializedName("email")
    val email: String?,

    @SerializedName("firstName")
    val firstName: String?,

    @SerializedName("lastName")
    val lastName: String?,

    @SerializedName("error")
    val error: String?
)