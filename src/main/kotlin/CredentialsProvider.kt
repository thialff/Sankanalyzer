package com.github.thialff

class CredentialsProvider {
    companion object {
        private val _username: String? = null
        private val _password: String? = null

        val username: String
            get() = _username ?: error("Username not set")

        val password: String
            get() = _password ?: error("Password not set")
    }
}