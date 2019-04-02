package com.sun.mediacontroller;

import android.app.Application;
import android.content.Context;

/**
 * @author Sun
 * @date 2019/1/29 10:21
 * @desc
 */
public class App extends Application {
    protected static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

        init();
    }

    public static Context getContext() {
        return context;
    }

    public void init() {
    }
}
