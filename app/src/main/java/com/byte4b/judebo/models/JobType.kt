package com.byte4b.judebo.models

import com.google.gson.annotations.SerializedName
import io.realm.RealmObject

data class JobType(
    @SerializedName("UF_ID") val id: Int,
    @SerializedName("UF_NAME") val name: String,
    @SerializedName("UF_SORTING") val popularity: Int?
) {
    fun toRealmVersion(): JobTypeRealm {
        val result = JobTypeRealm()
        result.id = id
        result.name = name
        result.popularity = popularity
        return result
    }
}

open class JobTypeRealm : RealmObject() {
    var id: Int = 0
    var popularity: Int? = null
    var name: String = ""

    fun toBasicVersion() = JobType(id, name, popularity)
}