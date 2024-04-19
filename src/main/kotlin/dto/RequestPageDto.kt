package com.github.thialff.dto

import kotlinx.serialization.Serializable

@Serializable
data class RequestPageDto(
    val meta: MetaDto,
    val data: List<PostDto>,
)