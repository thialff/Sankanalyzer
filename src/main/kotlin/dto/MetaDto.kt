package com.github.thialff.dto

import kotlinx.serialization.Serializable

@Serializable
data class MetaDto(
    val next: String?,
    val prev: String?,
)
