package com.byte4b.judebo.models

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.graphics.drawable.toBitmap
import com.byte4b.judebo.timestamp
import com.byte4b.judebo.utils.Setting
import com.byte4b.judebo.view.ServiceListener
import io.realm.RealmObject
import java.io.ByteArrayOutputStream
import java.util.*

data class Vocation(
    @Transient var isHided: Boolean = false,

    var UF_ACTIVE: Byte? = null,
    var COMPANY: String? = null,
    var DETAIL_TEXT: String? = null,
    var NAME: String? = null,
    var UF_APP_JOB_ID: String? = null,
    var UF_CONTACT_EMAIL: String? = null,
    var UF_CONTACT_PHONE: String? = null,
    var UF_DETAIL_IMAGE: String? = null,
    var UF_DISABLE: Long? = null,
    var UF_GOLD_PER_MONTH: Int? = null,
    var UF_GROSS_CURRENCY_ID: Int? = null,
    var UF_GROSS_PER_MONTH: Int? = null,
    var UF_JOBS_ID: Long? = null,
    var UF_LANGUAGE_ID_ALL: String? = null,
    var UF_LOGO_IMAGE: String? = null,
    var UF_MAP_POINT: String? = null,
    var UF_MODIFED: Long? = null,
    var UF_PREVIEW_IMAGE: String? = null,
    var UF_SKILLS_ID_ALL: String? = null,
    var UF_TYPE_OF_JOB_ID: Int? = null
) : ServiceListener {
    var location
        get() = UF_MAP_POINT?.split(", ")?.map { it.toDouble() } ?: listOf(.0, .0)
        set(value) { UF_MAP_POINT = value.joinToString(", ") }

    fun toRealmVersion(): VocationRealm {
        val result = VocationRealm()
        result.isHided = isHided
        result.COMPANY = COMPANY
        result.DETAIL_TEXT = DETAIL_TEXT
        result.NAME = NAME
        result.UF_ACTIVE = UF_ACTIVE
        result.UF_APP_JOB_ID = UF_APP_JOB_ID?.toLongOrNull()
        result.UF_CONTACT_EMAIL = UF_CONTACT_EMAIL
        result.UF_CONTACT_PHONE = UF_CONTACT_PHONE
        result.UF_DETAIL_IMAGE = UF_DETAIL_IMAGE
        result.UF_DISABLE = UF_DISABLE
        result.UF_GOLD_PER_MONTH = UF_GOLD_PER_MONTH
        result.UF_GROSS_CURRENCY_ID = UF_GROSS_CURRENCY_ID
        result.UF_GROSS_PER_MONTH = UF_GROSS_PER_MONTH
        result.UF_JOBS_ID = UF_JOBS_ID
        result.UF_LANGUAGE_ID_ALL = UF_LANGUAGE_ID_ALL
        result.UF_LOGO_IMAGE = UF_LOGO_IMAGE
        result.UF_MAP_POINT = UF_MAP_POINT
        result.UF_MODIFED = UF_MODIFED
        result.UF_PREVIEW_IMAGE = UF_PREVIEW_IMAGE
        result.UF_SKILLS_ID_ALL = UF_SKILLS_ID_ALL
        result.UF_TYPE_OF_JOB_ID = UF_TYPE_OF_JOB_ID
        return result
    }

    //fun createNew() {
    //    appid
    //    map
    //    lang,
    //    skill
    //    modifi,
    //    disable,

    //}

}

open class VocationRealm : RealmObject() {
    //system
    var isHided: Boolean = false

    var UF_ACTIVE: Byte? = null
    var COMPANY: String? = null
    var DETAIL_TEXT: String? = null
    var NAME: String? = null
    var UF_APP_JOB_ID: Long? = null
    var UF_CONTACT_EMAIL: String? = null
    var UF_CONTACT_PHONE: String? = null
    var UF_DETAIL_IMAGE: String? = null
    var UF_DISABLE: Long? = null
    var UF_GOLD_PER_MONTH: Int? = null
    var UF_GROSS_CURRENCY_ID: Int? = null
    var UF_GROSS_PER_MONTH: Int? = null
    var UF_JOBS_ID: Long? = null
    var UF_LANGUAGE_ID_ALL: String? = null
    var UF_LOGO_IMAGE: String? = null
    var UF_MAP_POINT: String? = null
    var UF_MODIFED: Long? = null
    var UF_PREVIEW_IMAGE: String? = null
    var UF_SKILLS_ID_ALL: String? = null
    var UF_TYPE_OF_JOB_ID: Int? = null

    fun setDateModifiedAndDisable() {
        val time = Calendar.getInstance()
        UF_MODIFED = time.timestamp
        time.add(Calendar.DATE, Setting.JOB_LIFETIME_IN_DAYS)
        UF_DISABLE = time.timestamp
    }

    fun setSalary(currencyId: Int, salary: String?) {
        UF_GROSS_CURRENCY_ID = currencyId
        UF_GROSS_PER_MONTH = try {
            salary?.trim()?.replace(" ", "")?.toInt()
        } catch (e: Exception) {
            null
        }

        if (UF_GROSS_PER_MONTH == 0)
            UF_GROSS_PER_MONTH = null
        if (UF_GROSS_PER_MONTH != null) {
            val currency = currencies.first { it.id == UF_GROSS_CURRENCY_ID }
            UF_GOLD_PER_MONTH = ((1000000L * UF_GROSS_PER_MONTH!!) / currency.rate).toInt()
        }
    }

    fun setIcons(drawable: Drawable) {
        UF_DETAIL_IMAGE = toBase64(
            drawable.toBitmap(
                Setting.MAX_IMG_CROP_HEIGHT,
                Setting.MAX_IMG_CROP_HEIGHT
            )
        )
        UF_LOGO_IMAGE = toBase64(
            drawable.toBitmap(
                Setting.MAX_IMG_CROP_HEIGHT_LOGO,
                Setting.MAX_IMG_CROP_HEIGHT_LOGO
            )
        )
        UF_PREVIEW_IMAGE = toBase64(
            drawable.toBitmap(
                Setting.MAX_IMG_CROP_HEIGHT_PREVIEW,
                Setting.MAX_IMG_CROP_HEIGHT_PREVIEW
            )
        )
    }

    private fun toBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            Base64.getEncoder().encodeToString(stream.toByteArray())
        else
            android.util.Base64.encodeToString(stream.toByteArray(), android.util.Base64.DEFAULT)
    }

    fun toBasicVersion() = Vocation(
        isHided = isHided,
        COMPANY = COMPANY,
        DETAIL_TEXT = DETAIL_TEXT,
        NAME = NAME,
        UF_APP_JOB_ID = UF_APP_JOB_ID?.toString(),
        UF_CONTACT_EMAIL = UF_CONTACT_EMAIL,
        UF_CONTACT_PHONE = UF_CONTACT_PHONE,
        UF_DETAIL_IMAGE = UF_DETAIL_IMAGE,
        UF_DISABLE = UF_DISABLE,
        UF_GOLD_PER_MONTH = UF_GOLD_PER_MONTH,
        UF_GROSS_CURRENCY_ID = UF_GROSS_CURRENCY_ID,
        UF_GROSS_PER_MONTH = UF_GROSS_PER_MONTH,
        UF_JOBS_ID = UF_JOBS_ID,
        UF_ACTIVE = UF_ACTIVE,
        UF_LANGUAGE_ID_ALL = UF_LANGUAGE_ID_ALL,
        UF_LOGO_IMAGE = UF_LOGO_IMAGE,
        UF_MAP_POINT = UF_MAP_POINT,
        UF_MODIFED = UF_MODIFED,
        UF_PREVIEW_IMAGE = UF_PREVIEW_IMAGE,
        UF_SKILLS_ID_ALL = UF_SKILLS_ID_ALL,
        UF_TYPE_OF_JOB_ID = UF_TYPE_OF_JOB_ID
    )
}

