package com.lj.com;

import android.app.Application;

import com.udisk.lib.UsbSdk;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        UsbSdk.init(this);
    }
}
