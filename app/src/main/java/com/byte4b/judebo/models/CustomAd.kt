package com.byte4b.judebo.models

data class CustomAd(
    val format: String?,
    val time: Int?,
    val url_link: String?,
    val url_source: String?
) {
    val isPhoto get() = format?.contains("png", ignoreCase = true) ?: false
    val isVideo get() = format?.contains("mp4", ignoreCase = true) ?: false
    val isEmpty = url_source == null || url_link == null
}