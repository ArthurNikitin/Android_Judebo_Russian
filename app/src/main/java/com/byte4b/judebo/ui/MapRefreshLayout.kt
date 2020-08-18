package com.byte4b.judebo.ui

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout


class MapRefreshLayout : SwipeRefreshLayout {
    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val action = ev.action
        when (action) {
            MotionEvent.ACTION_DOWN -> super.onTouchEvent(ev)
            MotionEvent.ACTION_MOVE -> return false
            MotionEvent.ACTION_CANCEL -> super.onTouchEvent(ev)
            MotionEvent.ACTION_UP -> return false
            else -> {
            }
        }
        return false
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        super.onTouchEvent(ev)
        return true
    }
}