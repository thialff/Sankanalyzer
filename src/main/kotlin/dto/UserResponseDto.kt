package com.github.thialff.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Used to deserialize a user profile from Sankaku API.
 * This does not map to all values by the API, but just the relevant ones.
 */
@Serializable
data class UserResponseDto(
    val email: String,
    @SerialName("favorite_count")
    val favoriteCount: Int,
    val id: Int,
    val name: String,
)