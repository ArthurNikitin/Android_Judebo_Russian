package com.byte4b.judebo.models

import com.google.gson.annotations.SerializedName

data class JobType(
    @SerializedName("UF_ID") val id: Int,
    @SerializedName("UF_NAME") val name: String,
    @SerializedName("UF_SORTING") val popularity: Int?
)