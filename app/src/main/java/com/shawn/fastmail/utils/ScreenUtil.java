package com.shawn.fastmail.utils;

import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by mengyangyang on 2016-08-09.
 */
public class ScreenUtil {
    private ScreenUtil() {
    }

    /**
     * 获取宽高
     *
     * @return 宽度 高度
     */
    public static String getScreenWidthAndHeight(Context context) {
        DisplayMetrics metric = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metric);
        return metric.widthPixels + "," + metric.heightPixels;
    }

    /**
     * 获取宽度
     *
     * @return 宽度
     */
    public static int getScreenWidth(Context context) {
        DisplayMetrics metric = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metric);
        return metric.widthPixels;
    }

    /**
     * 获取高度
     *
     * @return 高度
     */
    public static int getScreenHeight(Context context) {
        DisplayMetrics metric = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metric);
        return metric.heightPixels;
    }

    /**
     * 设置 宽度 高度
     *
     * @param view   控件
     * @param width  设置的宽度
     * @param height 设置的高度
     */
    public static void setWidthHeightNumber(View view, int width, int height) {
        ViewGroup.LayoutParams paramsPic = view.getLayoutParams();
        paramsPic.height = height;
        paramsPic.width = width;
        view.setLayoutParams(paramsPic);
    }

    /**
     * 设置高度
     *
     * @param view   控件
     * @param height 设置的高度
     */
    public static void setHeightNumber(View view, int height) {
        ViewGroup.LayoutParams paramsPic = view.getLayoutParams();
        paramsPic.height = height;
        view.setLayoutParams(paramsPic);
    }


    /**
     * 设置宽度
     *
     * @param view  控件
     * @param width 设置的宽度
     */
    public static void setWidthNumber(View view, int width) {
        ViewGroup.LayoutParams paramsPic = view.getLayoutParams();
        paramsPic.width = width;
        view.setLayoutParams(paramsPic);
    }

    private static int screenW;
    private static int screenH;
    private static float screenDensity;

    public static int getScreenW(Context context) {
        if (screenW == 0) {
            initScreen(context);
        }
        return screenW;
    }

    public static int getScreenH(Context context) {
        if (screenH == 0) {
            initScreen(context);
        }
        return screenH;
    }

    public static float getScreenDensity(Context context) {
        if (screenDensity == 0) {
            initScreen(context);
        }
        return screenDensity;
    }

    private static void initScreen(Context context) {
        DisplayMetrics metric = context.getResources().getDisplayMetrics();
        screenW = metric.widthPixels;
        screenH = metric.heightPixels;
        screenDensity = metric.density;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dp2px(Context context, float dpValue) {
        return (int) (dpValue * getScreenDensity(context) + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dp(Context context, float pxValue) {
        return (int) (pxValue / getScreenDensity(context) + 0.5f);
    }

    /**
     * 计算状态栏高度
     */
    public static int getStatusBarHeight(Activity ac) {
        Rect frame = new Rect();
        ac.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        return frame.top;
    }

    public static String readAssert(Context context, String fileName) {
        String jsonString = "";
        String resultString = "";
        try {
            InputStream inputStream = context.getResources().getAssets().open(fileName);
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            resultString = new String(buffer, "utf-8");
        } catch (Exception e) {
            Log.d("ScreenUtil", e.toString());
        }
        return resultString;
    }

    public static void hideKeyBoard(Activity context) {
        if (context != null && context.getCurrentFocus() != null) {
            ((InputMethodManager) context
                    .getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(context.getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }


}
