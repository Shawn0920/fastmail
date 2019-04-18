package com.shawn.fastmail.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.shawn.fastmail.App;

/**
 * 描述：
 *
 * @author shawn
 * @date 2019/2/19
 */
public class DimensionUtils {

    private static DisplayMetrics metrics;

    /**
     * 初始化
     */
    static {
        if (metrics == null) {
            metrics = new DisplayMetrics();
            WindowManager windowManager = (WindowManager) App.getContext().getSystemService(Context.WINDOW_SERVICE);
            windowManager.getDefaultDisplay().getMetrics(metrics);
        }
    }

    /**
     * 获取屏幕宽度
     * @return 屏幕宽度
     */
    public static int getWidth() {
        return metrics.widthPixels;
    }

    /**
     * 获取屏幕高度
     * @return 屏幕高度
     */
    public static int getHeight() {
        return metrics.heightPixels;
    }


    /**
     * 根据手机分辨率从DP转成PX
     * @param context
     * @param dpValue
     * @return
     */
    public static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     * @param spValue
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 根据手机的分辨率PX(像素)转成DP
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2dip(Context context, float pxValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     * @param pxValue
     * @return
     */

    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

}
