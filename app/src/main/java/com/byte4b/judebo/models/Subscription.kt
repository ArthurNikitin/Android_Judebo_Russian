package com.byte4b.judebo.models

import io.realm.RealmObject

class Subscription(
    val ID: Int,
    val UF_STORE_ID: String,
    val UF_DURATION: Long,
    val UF_NAME: String
) {
    fun toRealmVersion(): SubscriptionRealm {
        val result = SubscriptionRealm()
        result.ID = ID
        result.UF_STORE_ID = UF_STORE_ID
        result.UF_DURATION = UF_DURATION
        result.UF_NAME = UF_NAME
        return result
    }
}

open class SubscriptionRealm : RealmObject() {
    var ID: Int = 0
    var UF_STORE_ID = ""
    var UF_DURATION = 0L
    var UF_NAME = ""

    fun toBasicVersion() = Subscription(ID, UF_STORE_ID, UF_DURATION, UF_NAME)
}