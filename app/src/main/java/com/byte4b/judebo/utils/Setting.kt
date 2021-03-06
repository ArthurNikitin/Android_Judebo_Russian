package com.byte4b.judebo.utils

import android.content.Context
import android.os.Build
import androidx.core.content.edit
import com.byte4b.judebo.getLangFromLocale
import com.byte4b.judebo.models.*
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson

class Setting(ctx: Context) {

    private val pref = ctx.getSharedPreferences("setting", Context.MODE_PRIVATE)

    fun logout() {
        isAuth = false
        token = null
        email = null
    }

    var isAuth
        get() = pref.getBoolean("is_auth", false)
        set(value) = pref.edit { putBoolean("is_auth", value) }

    var token
        get() = pref.getString("token", null)
        set(value) = pref.edit { putString("token", value) }

    var signUpFromGoogle
        get() = pref.getBoolean("sign_up_google", false)
        set(value) = pref.edit { putBoolean("sign_up_google", value) }

    var signUpFromService
        get() = pref.getBoolean("sign_up_service", false)
        set(value) = pref.edit { putBoolean("sign_up_service", value) }

    var email
        get() = pref.getString("email", null)
        set(value) = pref.edit { putString("email", value) }

    var lastUpdateDynamicDataFromServer
        get() = pref.getString("last_update", 0L.toString())!!
        set(value) = pref.edit { putString("last_update", value) }

    var language
        get() = pref.getString("language", "")
        set(value) = pref.edit { putString("language", value) }

    var currency
        get() = pref.getString("currency", "")
        set(value) = pref.edit { putString("currency", value) }

    var isFromRecreate
        get() = pref.getBoolean("is_from_recreate", false)
        set(value) = pref.edit { putBoolean("is_from_recreate", value) }

    var lastMapCameraPosition: LatLng
        get() {
            return LatLng(
                pref.getFloat("lastMapCameraPosition_latitude", DEFAULT_LATITUDE.toFloat())
                    .toDouble(),
                pref.getFloat("lastMapCameraPosition_longitude", DEFAULT_LONGITUDE.toFloat())
                    .toDouble()
            )
        }
        set(value) {
            pref.edit {
                putFloat("lastMapCameraPosition_latitude", value.latitude.toFloat())
                putFloat("lastMapCameraPosition_longitude", value.longitude.toFloat())
            }
        }

    var maxVocations
        get() = pref.getInt("limit", LIMIT_VACANCIES_WITHOUT_SUBSCRIPTION)
        set(value) = pref.edit { putInt("limit", value) }

    fun getCurrentLanguage(): Language {
        return if (language == "") getLangFromLocale()
        else languages.first { it.locale == language!! }
    }

    fun getCurrentCurrency(): Currency {
        return if (currency == "") {
            if (language == "")
                currencies.firstOrNull { it.name == getLangFromLocale().currency } ?: currencies.first()
            else
                currencies.firstOrNull { it.name == currency } ?: currencies.first()
        } else
            currencies.firstOrNull { it.name == currency } ?: currencies.first()
    }

    var isFilterActive
        get() = pref.getBoolean("filter_active", false)
        set(value) = pref.edit { putBoolean("filter_active", value) }

    var filterJobType
        get() = pref.getString("filter_job_type", null)
        set(value) = pref.edit { putString("filter_job_type", value) }

    var filterLanguagesIds
        get() = pref.getString("filter_languages_ids", "")!!.split(",").filterNot { it == "" }
        set(value) = pref.edit { putString("filter_languages_ids", value.joinToString(",")) }

    var filterSkillsIds
        get() = pref.getString("filter_skills_ids", "")!!.split(",").filterNot { it == "" }
        set(value) = pref.edit { putString("filter_skills_ids", value.joinToString(",")) }

    var filterSalary
        get() = pref.getString("filter_salary", DEFAULT_FILTER_RANGE_PARAMS)!!
        set(value) = pref.edit { putString("filter_salary", value) }

    var lastAdShowTimeStamp
        get() = pref.getLong("lastAdShowTimeStamp", 0L)
        set(value) = pref.edit { putLong("lastAdShowTimeStamp", value) }

    var isLastTryShowAdHaveError
        get() = pref.getBoolean("isLastTryShowAdHaveError", false)
        set(value) = pref.edit { putBoolean("isLastTryShowAdHaveError", value) }

    var subscribeInfo: SubAnswer?
        get() = Gson().fromJson(pref.getString("sub", null), SubAnswer::class.java)
        set(value) {
            pref.edit { putString("sub", Gson().toJson(value)) }
            if (subscribeInfo?.SUBSCRIPTION_ID == DEFAULT_SUBSCRIPTION_ID_HOLDEN_ACCOUNT.toInt()) {
                maxVocations = 0
            } else {
                maxVocations = value?.SUBSCRIPTION_LIMIT ?: LIMIT_VACANCIES_WITHOUT_SUBSCRIPTION
            }
        }

    var toLogin
        get() = pref.getBoolean("to_login", false)
        set(value) = pref.edit { putBoolean("to_login", value) }

    var isLocaleSettingRtl
    get() = pref.getBoolean("isLocaleSettingRtl", false)
    set(value) = pref.edit { putBoolean("isLocaleSettingRtl", value) }

    companion object {
        //Cron in app
        //and
        // resend request after null/error answer AND checking time interval
        const val APP_CRON_FREQUENCY_IN_SECONDS = 60

        const val LIMIT_VACANCIES_WITHOUT_SUBSCRIPTION = 5

        const val BASIC_ZOOM = 11.0f
        const val MIN_ZOOM = 2.0f
        const val MAX_ZOOM = 21.0f

        const val DEFAULT_EDIT_PAGE_ZOOM = 15f
        const val DETAILS_DEFAULT_ZOOM = 14.0f
        const val DETAILS_MIN_ZOOM = MIN_ZOOM + 5
        const val DETAILS_MAX_ZOOM = MAX_ZOOM

        const val SHADOW_WIDTH = 5f

        //London
        const val DEFAULT_LATITUDE = 51.506767
        const val DEFAULT_LONGITUDE = 0.000004

        const val MAX_SEARCH_LATITUDE_SIZE = 0.3
        const val MAX_SEARCH_LONGITUDE_SIZE = 0.3

        const val SEARCH_REQUEST_PAUSE_SECONDS = 1
        const val SEARCH_REQUEST_MIN_MOVE_DELTA = 0.18
        //degrees

        // skills, languages, currencies rates
        const val PERIOD_UPDATE_DYNAMIC_DATA_FROM_SERVER_IN_MINUTE = 60 * 24

        // how offen update job list from server in TAB vacancies
        const val PERIOD_UPDATE_JOB_LIST_FOR_USER_IN_SECONDS = 1 * 15

        //Timeout of json request in Seconds
        const val JSON_REQUEST_TIMEOUT_IN_SECONDS = 15L

        const val TAGS_SLEEP_AFTER_SAVE_IN_SECONDS = 10L

        // 2020-08-10 b.2.5
        // const val CLUSTER_RADIUS = 165.0
        const val CLUSTER_RADIUS = 100.0
        const val VALUE_INFINITESIMAL = 0.00001
        //API LEVEL -> (increase const * size)
        val CURRENCY_ICON_SIZE =
            when (Build.VERSION.SDK_INT) {
                26 -> (1.2 * 24).toInt() //8.0 android
                27 -> (1.2 * 24).toInt() //8.1
                28 -> (24.0 * 0.6).toInt() //9

                else -> 24//other versions
            }

        val CLUSTER_SIZES =
            listOf(2..3, 4..5, 6..7, 8..9, 10..11, 12..15, 16..20, 21..30, 31..50, 51..99)

        const val JOB_LIFETIME_IN_DAYS = 90

        const val MAX_IMG_CROP_HEIGHT_LOGO = 32
        const val MAX_IMG_CROP_HEIGHT_PREVIEW = 100
        const val MAX_IMG_CROP_HEIGHT = 400
        const val DEFAULT_SKILL_ID_ALWAYS_HIDDEN = "284"
        const val DEFAULT_SUBSCRIPTION_ID_HOLDEN_ACCOUNT = "26"
        const val DEFAULT_JOB_ID_SERVICE_USED = 1L

        //Search filter settings
        const val SEARCH_GROSS_GOLD_MAX = 70_000
        const val SEARCH_GROSS_STEPS = 1_000

        const val DEFAULT_FILTER_RANGE_PARAMS = "0-${SEARCH_GROSS_STEPS}"
        const val DEFAULT_MAX = (SEARCH_GROSS_GOLD_MAX / SEARCH_GROSS_STEPS).toString()

        //How long text title/company/detail
        const val TEXT_LENGTH_MAX_SYMBOLS_JOB_TITLE = 70
        const val TEXT_LENGTH_MAX_SYMBOLS_JOB_EMAIL_PHONE_GROSS = 70
        const val TEXT_LENGTH_MAX_SYMBOLS_JOB_COMPANY = 40
        const val TEXT_LENGTH_MAX_SYMBOLS_JOB_DETAIL = 7000

        const val TAGS_POPULARITY_MINIMUM = 2

        // disable adv after show
        const val JSON_REQUEST_ADV_PERIOD_IN_SECONDS = 10_800
        const val ADV_DEFAULT_SHOW_ADV_IN_SECONDS = 7
        //setting for Google advertising type
        // = "banner"
        // = "fullscreen"
        // = "disable"
        const val ADV_GOOGLE_ADV_ADMOB_TYPE = "fullscreen"


        //Important!!! save format subsription name
        //playmarket_month_, playmarket_halfyear_, playmarket_year_
        val subs10PeriodVariantsIds = listOf(
            "playmarket_month_limit_00010",
            "playmarket_halfyear_limit_00010",
            "playmarket_year_limit_00010"
        )
        val subs50PeriodVariantsIds = listOf(
            "playmarket_month_limit_00050",
            "playmarket_halfyear_limit_00050",
            "playmarket_year_limit_00050"
        )
        val subs200PeriodVariantsIds = listOf(
            "playmarket_month_limit_00200",
            "playmarket_halfyear_limit_00200",
            "playmarket_year_limit_00200"
        )

    }

}