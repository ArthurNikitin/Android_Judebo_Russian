package com.byte4b.judebo;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

public class JudeboApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
