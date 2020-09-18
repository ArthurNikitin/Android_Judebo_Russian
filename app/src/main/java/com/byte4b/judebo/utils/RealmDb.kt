package com.byte4b.judebo.utils

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