package com.byte4b.judebo.models

data class SubAnswer(
    val MESSAGE: String,
    val STATUS: String,
    val SUBSCRIPTION_ID: Int? = null,
    val SUBSCRIPTION_BILL_TOKEN: String?,
    val SUBSCRIPTION_END: Long?,
    val SUBSCRIPTION_STORE_ID: String?,
    val SUBSCRIPTION_LIMIT: Int?,
    val SUBSCRIPTION_NAME: String?
)