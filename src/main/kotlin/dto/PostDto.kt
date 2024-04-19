package com.github.thialff.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PostDto(
    val id: Int,
    val rating: Rating,
    val author: AuthorDto,
    @SerialName("sample_url") val sampleUrl: String,
    @SerialName("sample_width") val sampleWidth: Int,
    @SerialName("sample_height") val sampleHeight: Int,
    @SerialName("preview_url") val previewUrl: String,
    @SerialName("preview_width") val previewWidth: Int,
    @SerialName("preview_height") val previewHeight: Int,
    @SerialName("file_url") val fileUrl: String,
    val width: Int,
    val height: Int,
    @SerialName("file_size") val fileSize: Int,
    @SerialName("file_type") val fileType: String,
    @SerialName("has_children") val hasChildren: Boolean,
    @SerialName("has_comments") val hasComments: Boolean,
    @SerialName("has_notes") val hasNotes: Boolean,
    @SerialName("is_favorited") val isFavorited: Boolean,
    @SerialName("user_vote") val userVote: Int?,
    @SerialName("parent_id") val parentId: Int?,
    @SerialName("fav_count") val favCount: Int,
    @SerialName("vote_count") val voteCount: Int,
    @SerialName("total_score") val totalScore: Int,
    val tags: List<TagsDto>,
    @SerialName("video_duration") val videoDuration: Float?,
)
