package com.byte4b.judebo.models

import io.realm.RealmObject

data class Vocation(
    var isHided: Boolean = false,

    var AUTO_TRANSLATE: Int? = null,
    var COMPANY: String? = null,
    var DETAIL_TEXT: String? = null,
    var NAME: String? = null,
    var UF_APP_JOB_ID: String? = null,
    var UF_CONTACT_EMAIL: String? = null,
    var UF_CONTACT_PHONE: String? = null,
    var UF_DETAIL_IMAGE: String? = null,
    var UF_DISABLE: String? = null,
    var UF_GOLD_GROSS_MONTH: String? = null,
    var UF_GOLD_PER_MONTH: String? = null,
    var UF_GROSS_CURRENCY_ID: Int? = null,
    var UF_GROSS_PER_MONTH: String? = null,
    var UF_JOBS_ID: Int? = null,
    var UF_LANGUAGE_ID_ALL: String? = null,
    var UF_LOGO_IMAGE: String? = null,
    var UF_MAP_POINT: String? = null,
    var UF_MAP_RENDERED: Int? = null,
    var UF_MODIFED: String? = null,
    var UF_PREVIEW_IMAGE: String? = null,
    var UF_SKILLS_ID_ALL: String? = null,
    var UF_TYPE_OF_JOB_ID: Int? = null,
    var UF_USER_ID: String? = null
) {
    var location
        get() = UF_MAP_POINT?.split(", ")?.map { it.toDouble() } ?: listOf(.0, .0)
        set(value) { UF_MAP_POINT = value.joinToString(", ") }

    fun toRealmVersion(): VocationRealm {
        val result = VocationRealm()
        result.isHided = isHided
        result.AUTO_TRANSLATE = AUTO_TRANSLATE
        result.COMPANY = COMPANY
        result.DETAIL_TEXT = DETAIL_TEXT
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
        result.UF_LANGUAGE_ID_ALL = UF_LANGUAGE_ID_ALL
        result.UF_LOGO_IMAGE = UF_LOGO_IMAGE
        result.UF_MAP_POINT = UF_MAP_POINT
        result.UF_MAP_RENDERED = UF_MAP_RENDERED
        result.UF_MODIFED = UF_MODIFED
        result.UF_PREVIEW_IMAGE = UF_PREVIEW_IMAGE
        result.UF_SKILLS_ID_ALL = UF_SKILLS_ID_ALL
        result.UF_TYPE_OF_JOB_ID = UF_TYPE_OF_JOB_ID
        result.UF_USER_ID = UF_USER_ID
        return result
    }
}

open class VocationRealm : RealmObject() {
    //system
    var isHided: Boolean = false

    var AUTO_TRANSLATE: Int? = null
    var COMPANY: String? = null
    var DETAIL_TEXT: String? = null
    var NAME: String? = null
    var UF_APP_JOB_ID: String? = null
    var UF_CONTACT_EMAIL: String? = null
    var UF_CONTACT_PHONE: String? = null
    var UF_DETAIL_IMAGE: String? = null
    var UF_DISABLE: String? = null
    var UF_GOLD_GROSS_MONTH: String? = null
    var UF_GOLD_PER_MONTH: String? = null
    var UF_GROSS_CURRENCY_ID: Int? = null
    var UF_GROSS_PER_MONTH: String? = null
    var UF_JOBS_ID: Int? = null
    var UF_LANGUAGE_ID_ALL: String? = null
    var UF_LOGO_IMAGE: String? = null
    var UF_MAP_POINT: String? = null
    var UF_MAP_RENDERED: Int? = null
    var UF_MODIFED: String? = null
    var UF_PREVIEW_IMAGE: String? = null
    var UF_SKILLS_ID_ALL: String? = null
    var UF_TYPE_OF_JOB_ID: Int? = null
    var UF_USER_ID: String? = null

    var location
        get() = UF_MAP_POINT?.split(", ")?.map { it.toDouble() } ?: listOf(.0, .0)
        set(value) { UF_MAP_POINT = value.joinToString(", ") }

    fun toBasicVersion() = Vocation(
        isHided = isHided,
        AUTO_TRANSLATE = AUTO_TRANSLATE,
        COMPANY = COMPANY,
        DETAIL_TEXT = DETAIL_TEXT,
        NAME = NAME,
        UF_APP_JOB_ID = UF_APP_JOB_ID,
        UF_CONTACT_EMAIL = UF_CONTACT_EMAIL,
        UF_CONTACT_PHONE = UF_CONTACT_PHONE,
        UF_DETAIL_IMAGE = UF_DETAIL_IMAGE,
        UF_DISABLE = UF_DISABLE,
        UF_GOLD_GROSS_MONTH = UF_GOLD_GROSS_MONTH,
        UF_GOLD_PER_MONTH = UF_GOLD_PER_MONTH,
        UF_GROSS_CURRENCY_ID = UF_GROSS_CURRENCY_ID,
        UF_GROSS_PER_MONTH = UF_GROSS_PER_MONTH,
        UF_JOBS_ID = UF_JOBS_ID,
        UF_LANGUAGE_ID_ALL = UF_LANGUAGE_ID_ALL,
        UF_LOGO_IMAGE = UF_LOGO_IMAGE,
        UF_MAP_POINT = UF_MAP_POINT,
        UF_MAP_RENDERED = UF_MAP_RENDERED,
        UF_MODIFED = UF_MODIFED,
        UF_PREVIEW_IMAGE = UF_PREVIEW_IMAGE,
        UF_SKILLS_ID_ALL = UF_SKILLS_ID_ALL,
        UF_TYPE_OF_JOB_ID = UF_TYPE_OF_JOB_ID,
        UF_USER_ID = UF_USER_ID
    )
}

