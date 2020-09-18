package com.byte4b.judebo.ui

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import com.byte4b.judebo.R
import com.google.android.material.textfield.TextInputLayout

class CenterErrorTextInputLayout(context: Context, attrs: AttributeSet) : TextInputLayout(context, attrs) {
    override fun setErrorTextAppearance(resId: Int) {
        super.setErrorTextAppearance(resId)
        val errorTextView = this.findViewById<TextView>(R.id.textinput_error)
        val errorFrameLayout = errorTextView.parent as FrameLayout

        errorTextView.gravity = Gravity.END
        errorFrameLayout.layoutParams =
            LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                marginStart = 670
            }
    }}