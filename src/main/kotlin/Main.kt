package com.github.thialff

import okhttp3.OkHttpClient

fun main() {
    val client = OkHttpClient.Builder()
        .build()

    val sankakuService = SankakuService(client)

    val authorizedClient = sankakuService
        .buildAuthorizedClient(CredentialsProvider.username, CredentialsProvider.password)

    val favoritesCapped = sankakuService.getFavorites(authorizedClient, pageSize = 10, maxNumberOfPosts = 23)
    check(favoritesCapped.size == 23) { "expected: 23, actual: ${favoritesCapped.size}" }

    val favoritesCapLargerThanActualFavoritesCount =
        sankakuService.getFavorites(authorizedClient, pageSize = 40, maxNumberOfPosts = 90)
    check(favoritesCapLargerThanActualFavoritesCount.size == 43) { "expected: 23, actual: ${favoritesCapped.size}" }
}