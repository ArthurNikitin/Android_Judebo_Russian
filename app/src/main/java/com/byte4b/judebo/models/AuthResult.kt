package com.byte4b.judebo.models

data class AuthResult(
    val data: String,
    val message: String,
    val status: String,
    val token: String?
)