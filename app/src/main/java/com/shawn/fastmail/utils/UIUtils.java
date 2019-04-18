package com.shawn.fastmail.utils;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.shawn.fastmail.App;


/**
 * 类描述: UI工具类
 * 创建人: chenyang
 * 创建时间: 2015-3-3 下午2:44:52
 * 修改人:
 * 修改时间:
 * 修改备注:
 * 版本:
 */
public class UIUtils {

    /**
     * 主线程Handler
     */
    private static Handler mMainThreadHandler = new Handler(Looper.getMainLooper());
    private static Application sApplication;

    private static Toast toast;// 防止多次弹出相同的土司

    /**
     * 根据资源id返回相应字符串
     *
     * @param resId
     * @return
     */
    public static String getStringByResId(int resId) {
        return getResources().getString(resId);
    }

    public static void init(Application application) {
        sApplication = application;
    }


    /**
     * 短时间显示Toast
     *
     * @param info 显示的内容
     */
    public static void showToastShort(String info) {
        if (toast == null) {
            toast = Toast.makeText(getContext(), info, Toast.LENGTH_SHORT);
        } else {
            toast.setText(info);
            toast.setDuration(Toast.LENGTH_SHORT);
        }
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    /**
     * 长时间显示Toast
     *
     * @param info 显示的内容
     */
    public static void showToastLong(String info) {
        if (toast == null) {
            toast = Toast.makeText(getContext(), info, Toast.LENGTH_LONG);
        } else {
            toast.setText(info);
            toast.setDuration(Toast.LENGTH_LONG);
        }
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static Context getContext() {
        if (sApplication == null)
            sApplication = App.INSTANCE;
        return sApplication;
    }

    /**
     * dip转换px
     */
    public static int dip2px(int dip) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f);
    }

    /**
     * pxz转换dip
     */
    public static int px2dip(int px) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    /**
     * 获取主线程的handler
     */
    public static Handler getHandler() {
        return mMainThreadHandler;
    }

    /**
     * 延时在主线程执行runnable
     */
    public static boolean postDelayed(Runnable runnable, long delayMillis) {
        return getHandler().postDelayed(runnable, delayMillis);
    }

    /**
     * 在主线程执行runnable
     */
    public static boolean post(Runnable runnable) {
        return getHandler().post(runnable);
    }

    /**
     * 从主线程looper里面移除runnable
     */
    public static void removeCallbacks(Runnable runnable) {
        getHandler().removeCallbacks(runnable);
    }

    public static View inflate(int resId) {
        return LayoutInflater.from(getContext()).inflate(resId, null);
    }

    /**
     * 获取资源
     */
    public static Resources getResources() {
        return getContext().getResources();
    }


    /**
     * 获取dimen
     */
    public static int getDimens(int resId) {
        return getResources().getDimensionPixelSize(resId);
    }

    /**
     * 获取drawable
     */
    public static Drawable getDrawable(int resId) {
        return getResources().getDrawable(resId);
    }

    /**
     * 获取颜色
     */
    public static int getColor(int resId) {
        return getResources().getColor(resId);
    }

    /**
     * 获取颜色选择器
     */
    public static ColorStateList getColorStateList(int resId) {
        return getResources().getColorStateList(resId);
    }

    public static void runInMainThread(Runnable runnable) {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            runnable.run();
        } else {
            post(runnable);
        }
    }

    /**
     * 把自身从父View中移除
     */
    public static void removeSelfFromParent(View view) {
        if (view != null) {
            ViewParent parent = view.getParent();
            if (parent != null && parent instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) parent;
                group.removeView(view);
            }
        }
    }
    /**
     * 获取当前进程名
     */
    public static String getCurrentProcessName(Context context) {
        int pid = android.os.Process.myPid();
        String processName = "";
        ActivityManager manager = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        if (null != manager && manager.getRunningAppProcesses() != null)
            for (ActivityManager.RunningAppProcessInfo process : manager.getRunningAppProcesses()) {
                if (process.pid == pid) {
                    LogUtils.e("getCurrentProcessName" + processName);
                    processName = process.processName;
                }
            }
        if (TextUtils.isEmpty(processName)) {
            processName = "";
        }
        return processName;
    }

    /**
     * 包名判断是否为主进程
     *
     * @param
     * @return
     */
    public static boolean isMainProcess(Context context) {
        return context.getApplicationContext().getPackageName().equals(getCurrentProcessName(context));
    }

    public static int[] getLocationInParent(View child, ViewGroup parent){
        int[] loc = {child.getLeft(),child.getTop()};
        View tmp = (View) child.getParent();
        ViewParent p;
        do {
            loc[0] += tmp.getLeft();
            loc[1] += tmp.getTop();
            p = tmp.getParent();
            if(p==null||!(p instanceof View)) return new int[]{0,0};
            tmp = (View) p;
        }while (tmp!=parent);
        return loc;
    }

    /**
     * 使EditText自动获取焦点和自动弹出键盘
     */
    public static void setFocusForEditText(final EditText editText) {
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();

        postDelayed(new Runnable() {
            @Override
            public void run() {
                LogUtils.e("postDelay...");
                InputMethodManager inputManager = (InputMethodManager) editText.getContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                if(inputManager!=null)
                    inputManager.showSoftInput(editText, 0);
            }
        }, 500);
    }



    public static void runOnMainThread(Runnable runnable) {
        if (Thread.currentThread()==Looper.getMainLooper().getThread()) {
            runnable.run();
        } else {
            post(runnable);
        }
    }

    /**
     * 获取文字
     */
    public static String getString(int resId) {
        return getResources().getString(resId);
    }

}
