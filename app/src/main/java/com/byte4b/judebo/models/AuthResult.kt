package com.byte4b.judebo.models

data class AuthResult(
    val data: String,
    val message: String,
    val status: String,
    val token: String?,
    val SUBSCRIPTION_ID: String?,
    val SUBSCRIPTION_LIMIT: Int?,
    val SUBSCRIPTION_END: Long?,
    val SUBSCRIPTION_BILL_TOKEN: String?,
    val SUBSCRIPTION_NAME: String?
)