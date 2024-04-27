package com.github.thialff

/**
 * Represents an authentication token used on Sankaku.
 *
 * Can be used in the Authorization header to authenticate the client.
 */
data class SankakuAuthToken (
    val type: String = "Bearer",
    val token: String
)