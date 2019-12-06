package com.jason.plugin.project;

import android.app.Application;
import android.content.Context;
import com.didi.virtualapk.PluginManager;

/**
 * @author Liu
 * @Date 2019-12-05
 * @mobile 18711832023
 */
public class MyApp extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        PluginManager.getInstance(base).init();
    }
}
