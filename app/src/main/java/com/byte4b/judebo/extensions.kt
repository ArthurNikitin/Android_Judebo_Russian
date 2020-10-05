package com.byte4b.judebo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.text.TextUtilsCompat
import androidx.core.view.ViewCompat
import com.byte4b.judebo.models.Language
import com.byte4b.judebo.models.SubscriptionRealm
import com.byte4b.judebo.models.languages
import com.byte4b.judebo.utils.Setting
import io.realm.Realm
import io.realm.kotlin.where
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.math.roundToInt

inline fun <reified A : Activity> Context.startActivity(configIntent: Intent.() -> Unit = {}) {
    startActivity(Intent(this, A::class.java).apply(configIntent))
}

@SuppressLint("MissingPermission")
fun Context.getLocation(): Location? {
    return try {
        val mLocationManager =
            applicationContext.getSystemService(LOCATION_SERVICE) as LocationManager
        //gps test data
//        mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.apply {
//            Log.e("test", "GPS_PROVIDER")
//            Log.e("test", "${latitude}=${longitude}=${accuracy}\n")
//        }
//
//        mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)?.apply {
//            Log.e("test", "NETWORK_PROVIDER")
//            Log.e("test", "${latitude}=${longitude}=${accuracy}\n")
//        }
//
        mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)?.apply {
            Log.e("test", "PASSIVE_PROVIDER")
            Log.e("test", "${latitude}=${longitude}=${accuracy}\n")
            return this
        } ?: return null

        //gps real get data
        //val providers = mLocationManager.getProviders(true)
        //var bestLocation: Location? = null
        //for (provider in providers) {
        //    val location = mLocationManager.getLastKnownLocation(provider) ?: continue
            //Log.e("test", "${location.latitude}=${location.longitude}=${location.accuracy}\n")
        //    Log.e("test 0", "${location.accuracy} ")
        //    if (bestLocation == null || location.accuracy < bestLocation.accuracy) {
        //        Log.e("test 1", "${location.accuracy} and ${bestLocation?.accuracy}")
        //        bestLocation = location
        //        //Log.e("test", "${location.accuracy} and ${bestLocation?.accuracy}")
        //        Log.e("test 2", "${location.accuracy} and ${bestLocation.accuracy}")
        //    }
        //}
        //return bestLocation
    } catch (e: Exception) {
        null
    }
}

fun TextView.setLeftDrawableMap(@DrawableRes drawable: Int, size: Int = Setting.CURRENCY_ICON_SIZE / 3) {
    val drawable = resources
        .getDrawable( drawable)
        .toBitmap(size, size)
        .toDrawable(resources)
    setCompoundDrawablesWithIntrinsicBounds(if (!isRtl) drawable else null,null, if (isRtl) drawable else null, null)
}

fun TextView.setRightDrawableMap(@DrawableRes drawable: Int, size: Int = Setting.CURRENCY_ICON_SIZE / 3) {
    val drawable = resources
        .getDrawable( drawable)
        .toBitmap(size, size)
        .toDrawable(resources)
    setCompoundDrawablesWithIntrinsicBounds(if (isRtl) drawable else null ,null, if (!isRtl) drawable else null, null)
}

fun TextView.setLeftDrawable(@DrawableRes drawable: Int, size: Int = Setting.CURRENCY_ICON_SIZE) {
    val drawable = resources
        .getDrawable( drawable)
        .toBitmap(size, size)
        .toDrawable(resources)
    setCompoundDrawablesWithIntrinsicBounds(if (!isRtl) drawable else null,null, if (isRtl) drawable else null, null)
}

val isRtl get() =
    TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) ==
            ViewCompat.LAYOUT_DIRECTION_RTL

fun isRtl(ctx: Context): Boolean {
    Setting(ctx).apply {
        val locale = if (language.isNullOrEmpty()) "en" else language!!
        return TextUtilsCompat.getLayoutDirectionFromLocale(Locale(locale)) ==
                ViewCompat.LAYOUT_DIRECTION_RTL
    }
}

fun TextView.setRightDrawable(@DrawableRes drawable: Int, size: Int = Setting.CURRENCY_ICON_SIZE) {
    val drawable = resources
        .getDrawable( drawable)
        .toBitmap(size, size)
        .toDrawable(resources)
    setCompoundDrawablesWithIntrinsicBounds(if (isRtl) drawable else null ,null, if (!isRtl) drawable else null, null)
}

fun String.round(): String {
    return try {
        ((toDouble() / 100).roundToInt() * 100).toString().getFormattedSalary()
    } catch (e: Exception) {
        this.getFormattedSalary()
    }
}

fun String.getFormattedSalary(split: String = "\u00A0"): String {
    val list = mutableListOf<String>()

    var tmp = ""
    val reverse = reversed()
    for (index in reverse.indices) {
        if (index % 3 == 0) {
            list.add(tmp)
            tmp = ""
        }
        tmp += reverse[index]
    }
    if (tmp.isNotBlank())
        list.add(tmp)

    return list.joinToString(split).reversed()
}

fun getLangFromLocale(): Language {
    for (lang in languages) {
        if (lang.locale == Locale.getDefault().language)
            return lang
    }
    return languages.first()
}

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

fun Context.hideKeyboard(view: View) {
    try {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    } catch (e: Exception) {}
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun Activity.rtlSupportActivation() {
    window.decorView.layoutDirection =
        if (isRtl(this)) View.LAYOUT_DIRECTION_RTL
        else View.LAYOUT_DIRECTION_LTR
}

val Calendar.timestamp get() = timeInMillis / 1000

fun getDate(timestampString: Long?) =
    Date((timestampString ?: 0L) * 1000L)


fun String.toServerId(realm: Realm) = realm
    .where<SubscriptionRealm>()
    .equalTo("UF_STORE_ID", this)
    .findFirst()
    ?.ID.toString()

fun String.toSubscribeName(realm: Realm) = realm
    .where<SubscriptionRealm>()
    .equalTo("UF_STORE_ID", this)
    .findFirst()
    ?.UF_NAME

fun Context.openBaseUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(url)
    startActivity(intent)
}

fun toBase64(bitmap: Bitmap): String {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        Base64.getEncoder().encodeToString(stream.toByteArray())
    else
        android.util.Base64.encodeToString(stream.toByteArray(), android.util.Base64.DEFAULT)
}