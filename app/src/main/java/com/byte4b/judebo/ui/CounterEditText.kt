package com.byte4b.judebo.ui

import android.content.Context
import android.text.InputFilter
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.core.widget.doOnTextChanged
import com.byte4b.judebo.R
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

        val typedArray = context.theme.obtainStyledAttributes(attributeSet,
            R.styleable.CounterEditText, 0, 0)

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