package com.byte4b.judebo

import android.app.Application
import android.os.Handler
import com.byte4b.judebo.utils.Setting
import com.byte4b.judebo.view.ServiceListener

class App : Application(), ServiceListener {

    override fun onCreate() {
        super.onCreate()

        val handler = Handler {

            true
        }

        Thread {
            Thread.sleep(Setting.)
        }.start()

        //timer with logic
        //error logic
        //show logic
    }
}