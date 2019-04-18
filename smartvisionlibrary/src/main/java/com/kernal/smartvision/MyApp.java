package com.kernal.smartvision;

import android.app.Application;
import android.content.Context;

/**
 * 描述：
 *
 * @author shawn
 * @date 2019/3/26
 */
public class MyApp extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        com.jiagu.sdk.BarCode_sdkProtected.install(this);
    }
}
