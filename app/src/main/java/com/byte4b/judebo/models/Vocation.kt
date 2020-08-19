package com.byte4b.judebo.models

import io.realm.RealmObject

data class Vocation(
    val ALL_SKILLS_NAME: String,
    val AUTO_TRANSLATE: Int,
    val COMPANY: String,
    val DETAIL_TEXT: String,
    val ID: Int,
    val NAME: String,
    val UF_APP_JOB_ID: String,
    val UF_CONTACT_EMAIL: String,
    val UF_CONTACT_PHONE: String,
    val UF_DETAIL_IMAGE: String,
    val UF_DISABLE: String,
    val UF_GOLD_GROSS_MONTH: String,
    val UF_GOLD_PER_MONTH: String,
    val UF_GROSS_CURRENCY_ID: Int,
    val UF_GROSS_PER_MONTH: String,
    val UF_JOBS_ID: Int,
    val UF_LANGUAGE_ID: String,
    val UF_LANGUAGE_ID_ALL: String,
    val UF_LOGO_IMAGE: String,
    val UF_MAP_POINT: String,
    val UF_MAP_POINT_LATITUDE: Double,
    val UF_MAP_POINT_LONGITUDE: Double,
    val UF_MAP_RENDERED: Int,
    val UF_MODIFED: String,
    val UF_PREVIEW_IMAGE: String,
    val UF_SKILLS_ID: String,
    val UF_SKILLS_ID_ALL: String,
    val UF_TYPE_OF_JOB_ID: Int,
    val UF_TYPE_OF_JOB_NAME: String,
    val UF_USER_ID: String
) {
    fun toRealmVersion(): VocationRealm {
        val result = VocationRealm()
        result.ALL_SKILLS_NAME = ALL_SKILLS_NAME
        result.AUTO_TRANSLATE = AUTO_TRANSLATE
        result.COMPANY = COMPANY
        result.DETAIL_TEXT = DETAIL_TEXT
        result.ID = ID
        result.NAME = NAME
        result.UF_APP_JOB_ID = UF_APP_JOB_ID
        result.UF_CONTACT_EMAIL = UF_CONTACT_EMAIL
        result.UF_CONTACT_PHONE = UF_CONTACT_PHONE
        result.UF_DETAIL_IMAGE = UF_DETAIL_IMAGE
        result.UF_DISABLE = UF_DISABLE
        result.UF_GOLD_GROSS_MONTH = UF_GOLD_GROSS_MONTH
        result.UF_GOLD_PER_MONTH = UF_GOLD_PER_MONTH
        result.UF_GROSS_CURRENCY_ID = UF_GROSS_CURRENCY_ID
        result.UF_GROSS_PER_MONTH = UF_GROSS_PER_MONTH
        result.UF_JOBS_ID = UF_JOBS_ID
        result.UF_LANGUAGE_ID = UF_LANGUAGE_ID
        result.UF_LANGUAGE_ID_ALL = UF_LANGUAGE_ID_ALL
        result.UF_LOGO_IMAGE = UF_LOGO_IMAGE
        result.UF_MAP_POINT = UF_MAP_POINT
        result.UF_MAP_POINT_LATITUDE = UF_MAP_POINT_LATITUDE
        result.UF_MAP_POINT_LONGITUDE = UF_MAP_POINT_LONGITUDE
        result.UF_MAP_RENDERED = UF_MAP_RENDERED
        result.UF_MODIFED = UF_MODIFED
        result.UF_PREVIEW_IMAGE = UF_PREVIEW_IMAGE
        result.UF_SKILLS_ID = UF_SKILLS_ID
        result.UF_SKILLS_ID_ALL = UF_SKILLS_ID_ALL
        result.UF_TYPE_OF_JOB_ID = UF_TYPE_OF_JOB_ID
        result.UF_TYPE_OF_JOB_NAME = UF_TYPE_OF_JOB_NAME
        result.UF_USER_ID = UF_USER_ID
        return result
    }
}

class VocationRealm : RealmObject() {
    var ALL_SKILLS_NAME: String = ""
    var AUTO_TRANSLATE: Int = 0
    var COMPANY: String = ""
    var DETAIL_TEXT: String = ""
    var ID: Int = 0
    var NAME: String = ""
    var UF_APP_JOB_ID: String = ""
    var UF_CONTACT_EMAIL: String = ""
    var UF_CONTACT_PHONE: String = ""
    var UF_DETAIL_IMAGE: String = ""
    var UF_DISABLE: String = ""
    var UF_GOLD_GROSS_MONTH: String = ""
    var UF_GOLD_PER_MONTH: String = ""
    var UF_GROSS_CURRENCY_ID: Int = 0
    var UF_GROSS_PER_MONTH: String = ""
    var UF_JOBS_ID: Int = 0
    var UF_LANGUAGE_ID: String = ""
    var UF_LANGUAGE_ID_ALL: String = ""
    var UF_LOGO_IMAGE: String = ""
    var UF_MAP_POINT: String = ""
    var UF_MAP_POINT_LATITUDE: Double = 0.0
    var UF_MAP_POINT_LONGITUDE: Double = 0.0
    var UF_MAP_RENDERED: Int = 0
    var UF_MODIFED: String = ""
    var UF_PREVIEW_IMAGE: String = ""
    var UF_SKILLS_ID: String = ""
    var UF_SKILLS_ID_ALL: String = ""
    var UF_TYPE_OF_JOB_ID: Int = 0
    var UF_TYPE_OF_JOB_NAME: String = ""
    var UF_USER_ID: String = ""

    fun toBasicVersion() = Vocation(ALL_SKILLS_NAME, AUTO_TRANSLATE, COMPANY, DETAIL_TEXT, ID, NAME,
        UF_APP_JOB_ID, UF_CONTACT_EMAIL, UF_CONTACT_PHONE, UF_DETAIL_IMAGE, UF_DISABLE,
        UF_GOLD_GROSS_MONTH, UF_GOLD_PER_MONTH, UF_GROSS_CURRENCY_ID, UF_GROSS_PER_MONTH, UF_JOBS_ID,
        UF_LANGUAGE_ID, UF_LANGUAGE_ID_ALL, UF_LOGO_IMAGE, UF_MAP_POINT, UF_MAP_POINT_LATITUDE,
        UF_MAP_POINT_LONGITUDE, UF_MAP_RENDERED, UF_MODIFED, UF_PREVIEW_IMAGE, UF_SKILLS_ID,
        UF_SKILLS_ID_ALL, UF_TYPE_OF_JOB_ID, UF_TYPE_OF_JOB_NAME, UF_USER_ID
    )
}

