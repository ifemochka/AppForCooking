package com.example.appforcooking.data.server.dto

import com.google.gson.annotations.SerializedName

data class ProfileDto(
    @SerializedName("email")
    val email: String,

    @SerializedName("firstName")
    val firstName: String,

    @SerializedName("lastName")
    val lastName: String,

    @SerializedName("birthDate")
    val birthDate: String?,

    @SerializedName("avatarUrl")
    val avatarUrl: String?
) {
}

data class UpdateProfileRequest(
    @SerializedName("firstName")
    val firstName: String,

    @SerializedName("lastName")
    val lastName: String
)

data class UpdateProfileResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String?,

    @SerializedName("firstName")
    val firstName: String?,

    @SerializedName("lastName")
    val lastName: String?
)