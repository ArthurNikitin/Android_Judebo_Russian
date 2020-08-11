package com.byte4b.judebo.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import com.byte4b.judebo.utils.Setting

class StrokeTextView(context: Context, attrs: AttributeSet) :
    androidx.appcompat.widget.AppCompatTextView(context, attrs) {

    override fun onDraw(canvas: Canvas?) {
        val textColor = textColors
        val paint = paint

        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeMiter = 10f
        setTextColor(Color.WHITE)
        paint.strokeWidth = Setting.SHADOW_WIDTH

        super.onDraw(canvas)
        paint.style = Paint.Style.FILL

        setTextColor(textColor)
        super.onDraw(canvas)
    }

}