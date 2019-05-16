package com.shawn.fastmail;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

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

        UMConfigure.init(this, UMConfigure.DEVICE_TYPE_PHONE, "");

        /**
         * 注意: 即使您已经在AndroidManifest.xml中配置过appkey和channel值，也需要在App代码中调用初始化接口（如需要使用AndroidManifest.xml中配置好的appkey和channel值，UMConfigure.init调用中appkey和channel参数请置为null）。
         */
//        UMConfigure.init(this, "5cdbd80e4ca357942c000e47", "ydy1000000", UMConfigure.DEVICE_TYPE_PHONE, "");
//        MobclickAgent.setScenarioType(getContext(), MobclickAgent.EScenarioType.E_UM_NORMAL);
//        MobclickAgent.setDebugMode(true);

    }

    public static Context getContext() {
        return mContext;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
//        com.jiagu.sdk.BarCode_sdkProtected.install(this);
    }
}
