package com.byte4b.judebo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.text.TextUtilsCompat
import androidx.core.view.ViewCompat
import java.util.*
import kotlin.math.roundToInt


fun Context.toast(text: String, duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, text, duration).show()

fun Context.toast(message: Int, duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, message, duration).show()

inline fun <reified A : Activity> Context.startActivity(configIntent: Intent.() -> Unit = {}) {
    startActivity(Intent(this, A::class.java).apply(configIntent))
}

fun Context.isHavePermission(permission: String): Boolean {
    return ActivityCompat.checkSelfPermission(this, permission) ==
            PackageManager.PERMISSION_GRANTED
}

@SuppressLint("MissingPermission")
fun Context.getLocation(): Location? {
    return try {
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
    } catch (e: Exception) {
        null
    }
}


private val size = 14
fun TextView.setLeftDrawable(@DrawableRes drawable: Int) {
    val isRtl = TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_RTL

    val drawable = resources
        .getDrawable( drawable)
        .toBitmap(size, size)
        .toDrawable(resources)
   setCompoundDrawablesWithIntrinsicBounds(if (!isRtl) drawable else null,null, if (isRtl) drawable else null, null)
}

fun TextView.setRightDrawable(@DrawableRes drawable: Int) {
    val isRtl = TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_RTL

    val drawable = resources
        .getDrawable( drawable)
        .toBitmap(size, size)
        .toDrawable(resources)
    setCompoundDrawablesWithIntrinsicBounds(if (isRtl) drawable else null ,null, if (!isRtl) drawable else null, null)
}

fun String.round(): String {
    return try {
        ((toDouble() / 100).roundToInt() * 100).toString()
    } catch (e: Exception) {
        this
    }
}