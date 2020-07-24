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
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.preview.view.*


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

fun TextView.setLeftDrawable(@DrawableRes drawable: Int) {
    val drawable = resources
        .getDrawable( drawable)
        .toBitmap(40, 40)
        .toDrawable(resources)
   setCompoundDrawablesWithIntrinsicBounds(drawable,null, null, null)
}

fun TextView.setRightDrawable(@DrawableRes drawable: Int) {
    val drawable = resources
        .getDrawable( drawable)
        .toBitmap(40, 40)
        .toDrawable(resources)
    setCompoundDrawablesWithIntrinsicBounds(null,null, drawable, null)
}
