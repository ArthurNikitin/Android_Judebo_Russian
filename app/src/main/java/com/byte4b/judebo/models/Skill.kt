package com.byte4b.judebo.models

import com.google.gson.annotations.SerializedName

class Skill(
    @SerializedName("UF_SKILLS_ID") val id: Int,
    @SerializedName("UF_POPULARITY") val popularity: Int?,
    @SerializedName("UF_NAME") val name: String
)