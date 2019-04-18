package com.shawn.fastmail.utils;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.shawn.fastmail.App;

/**
 * 描述：
 *
 * @author shawn
 * @date 2019/2/20
 */
public class ToastUtils {
    private static Toast toast = null;

    /**
     * 吐司Handler对象
     */
    private static Handler mToastHandler;

    /**
     * 吐司线程对象
     */
    private static ToastRunnable mToastRunnable;

    static {
        if (mToastHandler == null) {
//            mToastHandler = new Handler(Looper.getMainLooper());
            mToastHandler = App.handler;
        }
        if (mToastRunnable == null) {
            mToastRunnable = new ToastRunnable();
        }
    }


    public static void show(final String msg) {
        try {

            if (toast == null) {
                App.handler.post(new Runnable() {
                    @Override
                    public void run() {
                        toast = Toast.makeText(App.getContext(), "", Toast.LENGTH_SHORT);
                        mToastRunnable.msg = msg;
                        mToastHandler.post(mToastRunnable);
                    }
                });

            } else {
                mToastRunnable.msg = msg;
                mToastHandler.post(mToastRunnable);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void show(final int resId) {
        try {
            if (toast == null) {
                App.handler.post(new Runnable() {
                    @Override
                    public void run() {
                        toast = Toast.makeText(App.getContext(), "", Toast.LENGTH_SHORT);
                        mToastRunnable.msg = App.getContext().getResources().getString(resId);
                        mToastHandler.post(mToastRunnable);
                    }
                });
                toast = Toast.makeText(App.getContext(), "", Toast.LENGTH_SHORT);
            } else {
                mToastRunnable.msg = App.getContext().getResources().getString(resId);
                mToastHandler.post(mToastRunnable);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 吐司线程
     */
    private static class ToastRunnable implements Runnable {

        private String msg;

        @Override
        public void run() {
            toast.setText(msg);
            toast.show();
//            Toast.makeText(App.getContext(), msg, Toast.LENGTH_SHORT).show();
        }

    }


    /**
     * 不重复显示Toast
     *
     * @param context
     * @param msg
     */

    public static void show(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 短时间显示Toast
     *
     * @param context
     * @param message
     */
    public static void showShort(Context context, CharSequence message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 短时间显示Toast
     *
     * @param context
     * @param message
     */
    public static void showShort(Context context, int message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 长时间显示Toast
     *
     * @param context
     * @param message
     */
    public static void showLong(Context context, CharSequence message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    /**
     * 长时间显示Toast
     *
     * @param context
     * @param message
     */
    public static void showLong(Context context, int message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    /**
     * 自定义显示Toast时间
     *
     * @param context
     * @param message
     * @param duration
     */
    public static void show(Context context, CharSequence message, int duration) {
        Toast.makeText(context, message, duration).show();
    }

    /**
     * 自定义显示Toast时间
     *
     * @param context
     * @param message
     * @param duration
     */
    public static void show(Context context, int message, int duration) {
        Toast.makeText(context, message, duration).show();
    }
}

