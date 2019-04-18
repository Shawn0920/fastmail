package com.shawn.fastmail.utils;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;

import com.shawn.fastmail.App;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author yiw
 * @ClassName: CommonUtil
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @date 2015-12-28 下午4:17:01
 */
public class CommonUtil {

    public static int dip2px(float dpValue) {
        final float scale = App.getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(float pxValue) {
        final float scale = App.getContext().getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 获取手机的密度
     */
    public static float getDensity() {
        DisplayMetrics dm = App.getContext().getResources().getDisplayMetrics();
        return dm.density;
    }

    public static int getScreenWidth() {
        final Resources resources = App.getContext().getResources();
        final DisplayMetrics dm = resources.getDisplayMetrics();
        return dm.widthPixels;
    }

    public static int getScreenHeight() {
        final Resources resources = App.getContext().getResources();
        final DisplayMetrics dm = resources.getDisplayMetrics();
        return dm.heightPixels;
    }

    public static void hideSoftInput(Activity activity) {
        View view = activity.getWindow().peekDecorView();
        if (view != null) {
            InputMethodManager inputmanger = (InputMethodManager) activity
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void copyContent(Activity activity, String content) {
        //获取剪贴板管理器：
        ClipboardManager cm = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        // 创建普通字符型ClipData
        ClipData mClipData = ClipData.newPlainText("Label", content);
        // 将ClipData内容放到系统剪贴板里。
        cm.setPrimaryClip(mClipData);
    }

    /**
     * 利用正则表达式判断字符串是否是数字和字母以及位数是否正确
     *
     * @param str
     * @return
     */

    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9a-zA-Z]{26,35}");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    public static boolean isNumericExp(String str) {
        Pattern pattern = Pattern.compile("[0-9a-zA-Z]{58,67}");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    public static boolean isLetter(String str) {
        Pattern pattern = Pattern.compile("^[A-Za-z]+$");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    public static boolean isNumAndLetter(List<String> list) {
        boolean isLetter = true;
        for (String word : list) {
            if (!isLetter(word)) {
                isLetter = false;
                break;
            }
        }
        return isLetter;
    }

//    /**
//     * 判断是否含有特殊字符
//     *
//     * @param str
//     * @return true为包含，false为不包含
//     */
//    public static boolean isSpecialChar(String str) {
//        String regEx = "[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t";
//        Pattern p = Pattern.compile(regEx);
//        Matcher m = p.matcher(str);
//        return m.find();
//    }

    /*  public static void startScan(Activity activity){
          //请求Camera权限 与 文件读写 权限
           new AlertDialog.Builder(activity)
                  .setTitle(R.string.camera_tips)
                  .setMessage(R.string.camera_sure)
                  .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialogInterface, int i) {
                          if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                              ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, 1);
                          }
                      }
                  });


      }*/


    /*
     * 将时间戳转换为时间
     */
    public static String stampToDate(String s) {
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long lt = new Long(s);
        Date date = new Date(lt);
        res = simpleDateFormat.format(date);
        return res;
    }

    /**
     * 将时间戳转换为UTC时间
     *
     * @param s
     * @return
     */
    public static String stampToDateForUTC(String s) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String ss = format.format(new Date(Long.parseLong(s))) + "  +UTC";
        return ss;
    }

    public static String getCurrentDate() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return sdf.format(date);
    }

    public static void setTranslateAnimationX(View view) {
        TranslateAnimation animation = new TranslateAnimation(0, -8, 0, 0);
        animation.setInterpolator(new OvershootInterpolator());
        animation.setDuration(100);
        animation.setRepeatCount(3);
        animation.setRepeatMode(Animation.REVERSE);
        view.startAnimation(animation);
    }



    /**
     * 根据字符串生成uuid
     *
     * @param str
     * @return
     */
    public static String fromStringWhitoutHyphens(String str) {
        StringBuilder sb = new StringBuilder();
        sb.append(str.substring(0, 8));
        sb.append("-");
        sb.append(str.substring(8, 12));
        sb.append("-");
        sb.append(str.substring(12, 16));
        sb.append("-");
        sb.append(str.substring(16, 20));
        sb.append("-");
        sb.append(str.substring(20, 32));
        return sb.toString();

    }


//    /**
//     * 获取渠道号
//     *
//     * @return
//     */
//    public static String getChannelId() {
//        if (SpUtil.getInstance().getString(Constants.CHANNEL_ID) != null) {
//            String s = SpUtil.getInstance().getString(Constants.CHANNEL_ID);
//            s = s.substring(1, s.length());
//            return s;
//        } else {
//            return "100000";
//        }
//    }

    /**
     * 获取版本版本名称
     *
     * @param context
     * @return
     */
    public static String getVersionName(Context context) {
        String verName = "";
        try {
            verName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return verName;
    }


    public static int getVersionCode(Context context){
        int verCode = 0;
        try {
            verCode = context.getPackageManager().getPackageInfo(context.getPackageName(),0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return verCode;
    }
}
