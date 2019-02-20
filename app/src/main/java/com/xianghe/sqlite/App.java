package com.xianghe.sqlite;

import android.app.Application;

import com.xianghe.sqlite.manager.DbManager;

public class App extends Application {

    private static App mApp;

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;
        DbManager.getInstance().init();
    }

    public static App getInstance(){
        return mApp;
    }
}
