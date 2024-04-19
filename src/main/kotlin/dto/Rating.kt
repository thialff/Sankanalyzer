package com.github.thialff.dto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = RatingSerializer::class)
enum class Rating(val label: String) {
    SAFE("s"),
    QUESTIONABLE("q"),
    EXPLICIT("e"),
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = Rating::class)
object RatingSerializer : KSerializer<Rating> {
    override fun serialize(encoder: Encoder, value: Rating) {
        encoder.encodeString(value.label)
    }

    override fun deserialize(decoder: Decoder): Rating {
        return when (val value = decoder.decodeString()) {
            "s" -> Rating.SAFE
            "q" -> Rating.QUESTIONABLE
            "e" -> Rating.EXPLICIT
            else -> throw IllegalArgumentException("Unknown rating: $value")
        }
    }
}