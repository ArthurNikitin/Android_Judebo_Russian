package com.byte4b.judebo.models

data class CustomAd(
    val format: String? = null,
    val time: Int? = null,
    val url_link: String? = null,
    val url_source: String? = null
) {
    val isPhoto get() = format?.contains("png", ignoreCase = true) ?: false
    val isVideo get() = format?.contains("mp4", ignoreCase = true) ?: false
    val isEmpty get() = url_source == null || url_link == null
}