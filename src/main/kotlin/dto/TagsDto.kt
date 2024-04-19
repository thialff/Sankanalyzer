package com.github.thialff.dto

import kotlinx.serialization.Serializable

@Serializable
data class TagsDto(
    val id: Int,
    val name: String,
    val type: Int,
    val count: Int,
    val rating: Rating?,
)
