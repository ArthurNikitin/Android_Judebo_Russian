package com.byte4b.judebo.models

import com.google.gson.annotations.SerializedName
import io.realm.RealmObject

data class CurrencyRate(
    @SerializedName("UF_ID") val id: Int,
    @SerializedName("UF_NAME") val name: String,
    @SerializedName("UF_RATE") val rate: Int
) {
    fun toRealmVersion(): CurrencyRateRealm {
        val result = CurrencyRateRealm()
        result.id = id
        result.name = name
        result.rate = rate
        return result
    }
}

open class CurrencyRateRealm : RealmObject() {
    var id: Int = 0
    var name: String = ""
    var rate: Int = 1

    fun toBasicVersion() = CurrencyRate(id, name, rate)
}