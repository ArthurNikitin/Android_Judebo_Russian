package com.byte4b.judebo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast


fun Context.toast(text: String, duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, text, duration).show()

fun Context.toast(message: Int, duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, message, duration).show()

inline fun <reified A : Activity> Context.startActivity(configIntent: Intent.() -> Unit = {}) {
    startActivity(Intent(this, A::class.java).apply(configIntent))
}