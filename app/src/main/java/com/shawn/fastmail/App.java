package com.shawn.fastmail;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

/**
 * 描述：
 *
 * @author shawn
 * @date 2019/2/19
 */
public class App extends Application {

    public static boolean isDebug = true;

    private static Context mContext;

    public static App INSTANCE = null;

    public static Handler handler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();

        INSTANCE = this;

    }

    public static Context getContext() {
        return mContext;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        com.jiagu.sdk.BarCode_sdkProtected.install(this);
    }
}
