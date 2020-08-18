package com.byte4b.judebo.models

import com.google.gson.annotations.SerializedName
import io.realm.RealmObject

data class Skill(
    @SerializedName("UF_SKILLS_ID") val id: Int = 0,
    @SerializedName("UF_POPULARITY") val popularity: Int? = null,
    @SerializedName("UF_NAME") val name: String = ""
) {
    fun toRealmVersion(): SkillRealm {
        val result = SkillRealm()
        result.id = id
        result.popularity = popularity
        result.name = name
        return result
    }
}

open class SkillRealm : RealmObject() {
    var id: Int = 0
    var popularity: Int? = null
    var name: String = ""

    fun toBasicVersion() = Skill(id, popularity, name)
}