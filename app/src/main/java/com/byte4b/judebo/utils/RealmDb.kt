package com.byte4b.judebo.utils

import com.byte4b.judebo.models.Currency
import com.byte4b.judebo.models.CurrencyRateRealm
import com.byte4b.judebo.models.VocationRealm
import io.realm.Realm
import io.realm.kotlin.where

object RealmDb {

    fun getVocationsCount(realm: Realm) =
        realm.where<VocationRealm>().findAll()
            .filterNot { it.isHided }
            .filter { it.UF_APP_JOB_ID != null }
            .count()

}

fun Currency.getLastRate(realm: Realm): Int {
    try {
        rate = realm.where<CurrencyRateRealm>()
            .equalTo("id", id)
            .findFirst()!!
            .rate
    } catch (e: Exception) {}
    return rate
}