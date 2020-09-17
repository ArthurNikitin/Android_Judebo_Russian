package com.byte4b.judebo.ui

import android.content.Context
import android.text.InputFilter
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import com.byte4b.judebo.R
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.counter_text_field.view.*


class CounterEditText(context: Context, attributeSet: AttributeSet) :
    RelativeLayout(context, attributeSet) {

    //hint + size

    var maxLength = 100
    var minLines = 1
    var maxLines = 8
    var hint = ""

    init {
        LayoutInflater.from(context).inflate(R.layout.counter_text_field, this)

        editText.doOnTextChanged { text, _, _, _ ->
            textView.text = "${text.toString().length}/${maxLength}"
        }

        val typedArray = context.theme.obtainStyledAttributes(
            attributeSet,
            R.styleable.CounterEditText, 0, 0
        )

        maxLength = typedArray.getInt(R.styleable.CounterEditText_maxLength, 1000)
        minLines = typedArray.getInt(R.styleable.CounterEditText_android_minLines, 1)
        maxLines = typedArray.getInt(R.styleable.CounterEditText_maxLines, 8)
        hint = typedArray.getString(R.styleable.CounterEditText_hint)!!

        editText.hint = hint
        editText.minLines = minLines
        editText.maxLines = maxLines
        editText.filters = arrayOf(InputFilter.LengthFilter(maxLength))

        editText.setText(typedArray.getString(R.styleable.CounterEditText_text))


    }

    fun getData() = editText.text.toString()

}

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