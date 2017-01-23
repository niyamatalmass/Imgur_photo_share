package com.treehouse.android.retrofitworkshop;

import android.app.Application;

import com.treehouse.android.retrofitworkshop.api.OAuthUtil;


public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // ensure the shared pref is initialized with the Global Context
        OAuthUtil.initSharedPref(this);
    }
}
