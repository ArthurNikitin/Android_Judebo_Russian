package com.byte4b.judebo.models

data class CustomAd(
    val format: String?,
    val time: Int?,
    val url_link: String?,
    val url_source: String?
) {
    val isPhoto = format == "png"
    val isVideo = format == "mp4"
    val isEmpty = url_source == null || url_link == null
}