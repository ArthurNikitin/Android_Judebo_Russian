package com.byte4b.judebo.ui

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout


class MapRefreshLayout : SwipeRefreshLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    init {
        setColorSchemeColors(Color.parseColor("#027E3C"))
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> super.onTouchEvent(ev)
            MotionEvent.ACTION_MOVE -> return false
            MotionEvent.ACTION_CANCEL -> super.onTouchEvent(ev)
            MotionEvent.ACTION_UP -> return false
            else -> {}
        }
        return false
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        super.onTouchEvent(ev)
        return true
    }
}