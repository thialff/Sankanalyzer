package com.github.thialff

import com.github.thialff.dto.PostDto
import com.github.thialff.dto.RequestPageDto
import com.github.thialff.dto.UserResponseDto
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.lang.Thread.sleep
import java.net.HttpURLConnection


class SankakuService(val defaultClient: OkHttpClient) {

    companion object {
        const val DELAY_BETWEEN_REQUESTS_MS = 1000L
    }

    private val json = Json { ignoreUnknownKeys = true }

    private fun buildAuthJsonBody(username: String, password: String) =
        """{"login":"$username","password":"$password","mfaParams":{"login":"$username"}}"""
            .toRequestBody("application/json".toMediaType())

    /**
     * Will return a client that is authorized using the provided username and password.
     * Authorization works through the Authorization header with a bearer token.
     */
    @Throws(IOException::class)
    fun buildAuthorizedClient(username: String, password: String): OkHttpClient {
        defaultClient.newCall(
            Request.Builder()
                .url("https://capi-v2.sankakucomplex.com/auth/token")
                .post(buildAuthJsonBody(username, password))
                .build()
        ).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            if (response.body == null) throw IOException("Response body is null")

            val responseJson = Json.parseToJsonElement(response.body!!.string())

            val tokenType = responseJson.jsonObject["token_type"]?.jsonPrimitive?.content
            check(tokenType != null) { "missing field 'token_type'" }
            val accessToken = responseJson.jsonObject["access_token"]?.jsonPrimitive?.content
            check(accessToken != null) { "missing field 'access_token'" }

            val token = SankakuAuthToken(tokenType, accessToken)
            return OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .addHeader("Authorization", "${token.type} ${token.token}")
                        .build()
                    chain.proceed(request)
                }
                .build()
        }
    }

    /**
     * Will return the profile of the user that is authorized with the provided client.
     *
     * @throws IOException if the request was not successful
     */
    @Throws(IOException::class)
    fun getProfile(authorizedClient: OkHttpClient): UserResponseDto {
        if (authorizedClient.interceptors.isEmpty()) throw IllegalArgumentException("authorized client does not have any interceptors that could add auth headers")
        val request = Request.Builder()
            .url("https://capi-v2.sankakucomplex.com/users/me?lang=en")
            .build()

        authorizedClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            val responseBody = response.body?.string() ?: throw IOException("Response body is null")

            val parsedResponse = json.parseToJsonElement(responseBody)
            parsedResponse.jsonObject["success"]?.jsonPrimitive?.booleanOrNull?.let { success ->
                if (!success) throw IOException("Response was not successful")
            } ?: throw IOException("missing field 'success'")

            return parsedResponse.jsonObject["user"]?.let { data ->
                json.decodeFromJsonElement(UserResponseDto.serializer(), data)
            } ?: throw IOException("missing field 'user'")
        }
    }

    @Throws(IOException::class)
    fun getPosts(
        client: OkHttpClient,
        defaultThreshold: Int = 2,
        hidePostsInBooks: Boolean = true,
        pageSize: Int = 40,
        maxNumberOfPosts: Int? = pageSize,
        tags: List<String> = emptyList()
    ): List<PostDto> {
        require(defaultThreshold in 1..5) { "defaultThreshold must be between 1 and 5" }
        require(pageSize > 0) { "pageSize must be positive" }

        val urlBuilder = "https://capi-v2.sankakucomplex.com/posts/keyset"
            .toHttpUrl()
            .newBuilder()
            .addQueryParameter("lang", "en")
            .addQueryParameter("default_threshold", defaultThreshold.toString())
            .addQueryParameter("limit", pageSize.toString())

        if (hidePostsInBooks) {
            urlBuilder.addQueryParameter("hide_posts_in_books", "in-larger-tags")
        }
        if (tags.isNotEmpty()) {
            urlBuilder.addEncodedQueryParameter("tags", tags.joinToString("+"))
        }

        val posts = mutableListOf<PostDto>()
        var fetchNextPage = true
        var nextToFetchPostId: String? = null

        while (fetchNextPage) {
            if (nextToFetchPostId != null) {
                urlBuilder.setQueryParameter("next", nextToFetchPostId)
            }

            val request = Request.Builder()
                .url(urlBuilder.build())
                .build()

            client.newCall(request).execute().use { response ->
                when (response.code) {
                    HttpURLConnection.HTTP_OK -> {
                        val body = response.body?.string() ?: throw IllegalStateException("Response body is null")
                        val page = json.decodeFromString<RequestPageDto>(body)

                        if (maxNumberOfPosts != null && posts.size + page.data.size > maxNumberOfPosts) {
                            val numberOfRemainingPosts = maxNumberOfPosts - posts.size
                            posts.addAll(page.data.take(numberOfRemainingPosts))
                            fetchNextPage = false
                        } else {
                            posts.addAll(page.data)
                            nextToFetchPostId = page.meta.next
                            fetchNextPage = page.data.size == pageSize && page.meta.next != null
                        }
                    }
                    // Too many requests
                    429 -> {
                        throw IOException("Too many requests")
                    }
                    else -> {
                        throw IOException("request unsuccessful\nhttp_status_code: ${response.code}\nbody: ${response.body?.string()}")
                    }
                }
            }
            sleep(DELAY_BETWEEN_REQUESTS_MS)
        }

        return posts

    }

    fun getFavorites(
        authorizedClient: OkHttpClient,
        defaultThreshold: Int = 2,
        hidePostsInBooks: Boolean = true,
        pageSize: Int = 40,
        maxNumberOfPosts: Int? = pageSize,
        tags: List<String> = emptyList()
    ): List<PostDto> {
        if (authorizedClient.interceptors.isEmpty()) throw IllegalArgumentException("authorized client does not have any interceptors that could add auth headers")

        val userFavoritesTag = "fav:${getProfile(authorizedClient).name}"
        return getPosts(
            authorizedClient,
            defaultThreshold,
            hidePostsInBooks,
            pageSize,
            maxNumberOfPosts,
            tags + userFavoritesTag
        )
    }
}