/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.witsi.setting.manager.luncher;

import android.app.SearchManager;
import android.content.*;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;

import java.lang.ref.WeakReference;

import com.witsi.setting1.R;



public class LauncherAppState {
    private static final String TAG = "LauncherAppState";
    private static final String SHARED_PREFERENCES_KEY = "com.android.launcher3.prefs";

    private AppFilter mAppFilter;
//    private WidgetPreviewLoader.CacheDb mWidgetPreviewCacheDb;
    private boolean mIsScreenLarge;
    private float mScreenDensity;
    private int mLongPressTimeout = 300;

    private static WeakReference<LauncherProvider> sLauncherProvider;
    private static Context sContext;

    private static LauncherAppState INSTANCE;

//    private DynamicGrid mDynamicGrid;

    public static LauncherAppState getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LauncherAppState();
        }
        return INSTANCE;
    }

    public static LauncherAppState getInstanceNoCreate() {
        return INSTANCE;
    }

    public Context getContext() {
        return sContext;
    }

    /*
     * getApplicationContext() 返回应用的上下文，生命周期是整个应用，应用摧毁它才摧�?
     * Activity.this的context 返回当前activity的上下文，属于activity ，activity 摧毁他就摧毁
     * getBaseContext()  返回由构造函数指定或setBaseContext()设置的上下文this.getApplicationContext（）
     * 取的是这个应 用程序的Context，Activity.this取的是这个Activity的Context，这两�?的生命周期是不同 的，
     * 前�?的生命周期是整个应用，后者的生命周期只是它所在的Activity�?
     * */
    
    public static void setApplicationContext(Context context) {
        if (sContext != null) {
            Log.w(TAG, "setApplicationContext called twice! old=" + sContext + " new=" + context);
        }
        sContext = context.getApplicationContext();
    }

    private LauncherAppState() {
        if (sContext == null) {
            throw new IllegalStateException("LauncherAppState inited before app context set");
        }

        Log.v(TAG, "LauncherAppState inited");

        if (sContext.getResources().getBoolean(R.bool.debug_memory_enabled)) {
//            MemoryTracker.startTrackingMe(sContext, "L");
        }

        // set sIsScreenXLarge and mScreenDensity *before* creating icon cache
        //判断是否是大屏幕(�?��边长>720px)
        mIsScreenLarge = isScreenLarge(sContext.getResources());
        //获取屏幕密度
        mScreenDensity = sContext.getResources().getDisplayMetrics().density;

//        mWidgetPreviewCacheDb = new WidgetPreviewLoader.CacheDb(sContext);
        mAppFilter = AppFilter.loadByName(sContext.getString(R.string.app_filter_class));


        // Register for changes to the favorites
        ContentResolver resolver = sContext.getContentResolver();
        resolver.registerContentObserver(LauncherSettings.Favorites.CONTENT_URI, true,
                mFavoritesObserver);
    }

    /**
     * Call from Application.onTerminate(), which is not guaranteed to ever be called.
     */
    public void onTerminate() {

        ContentResolver resolver = sContext.getContentResolver();
        resolver.unregisterContentObserver(mFavoritesObserver);
    }

    /**
     * Receives notifications whenever the user favorites have changed.
     */
    private final ContentObserver mFavoritesObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            // If the database has ever changed, then we really need to force a reload of the
            // workspace on the next load
        }
    };


    boolean shouldShowAppOrWidgetProvider(ComponentName componentName) {
        return mAppFilter == null || mAppFilter.shouldShowApp(componentName);
    }

//    WidgetPreviewLoader.CacheDb getWidgetPreviewCacheDb() {
//        return mWidgetPreviewCacheDb;
//    }

    static void setLauncherProvider(LauncherProvider provider) {
        sLauncherProvider = new WeakReference<LauncherProvider>(provider);
    }

    static LauncherProvider getLauncherProvider() {
        return sLauncherProvider.get();
    }

    public static String getSharedPreferencesKey() {
        return SHARED_PREFERENCES_KEY;
    }

//    DeviceProfile initDynamicGrid(Context context, int minWidth, int minHeight,
//                                  int width, int height,
//                                  int availableWidth, int availableHeight) {
//        if (mDynamicGrid == null) {
//            mDynamicGrid = new DynamicGrid(context,
//                    context.getResources(),
//                    minWidth, minHeight, width, height,
//                    availableWidth, availableHeight);
//        }
//
//        // Update the icon size
//        DeviceProfile grid = mDynamicGrid.getDeviceProfile();
//        Utilities.setIconSize(grid.iconSizePx);
//        grid.updateFromConfiguration(context.getResources(), width, height,
//                availableWidth, availableHeight);
//        return grid;
//    }
//    
//    DynamicGrid getDynamicGrid() {
//        return mDynamicGrid;
//    }

    public boolean isScreenLarge() {
        return mIsScreenLarge;
    }

    // Need a version that doesn't require an instance of LauncherAppState for the wallpaper picker
    public static boolean isScreenLarge(Resources res) {
        return res.getBoolean(R.bool.is_large_tablet);
    }

    public static boolean isScreenLandscape(Context context) {
        return context.getResources().getConfiguration().orientation ==
            Configuration.ORIENTATION_LANDSCAPE;
    }

    public float getScreenDensity() {
        return mScreenDensity;
    }

    public int getLongPressTimeout() {
        return mLongPressTimeout;
    }
}
