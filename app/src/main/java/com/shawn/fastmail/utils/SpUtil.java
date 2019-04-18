package com.shawn.fastmail.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.shawn.fastmail.App;

/**
 * 描述：
 *
 * @author shawn
 * @date 2019/2/19
 */
public class SpUtil {

    private static SharedPreferences mSharedPreferences;
    private static SpUtil mInstance;
    private final static String COMMON_SP = "COMMON_SP_ZBL";

    private static final String DEFAULT_VERSION = "default_version";
    private static final String LAST_EXPRESS_NAME = "last_express_name";
    private static final String LAST_EXPRESS_ID = "last_express_id";
    private static final String LAST_PACKET_TYPE = "last_packet_type";
    private static final String TOKEN = "token";
    private static final String USERNAME = "username";
    private static final String P = "p";

    public static SpUtil getInstance() {
        if (mSharedPreferences == null) {
            mSharedPreferences = App.getContext().getSharedPreferences(COMMON_SP, Context.MODE_PRIVATE);
        }
        if (mInstance == null) mInstance = new SpUtil();
        return mInstance;
    }

    public String getDefaultVersion() {
        return mSharedPreferences.getString(DEFAULT_VERSION, "1.0.0");
    }

    public void setDefaultVersion(String s) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(DEFAULT_VERSION, s);
        editor.apply();
    }

    public String getExpressName() {
        return mSharedPreferences.getString(LAST_EXPRESS_NAME, "");
    }

    public void setExpressName(String s) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(LAST_EXPRESS_NAME, s);
        editor.apply();
    }

    public String getExpressId() {
        return mSharedPreferences.getString(LAST_EXPRESS_ID, "");
    }

    public void setExpressId(String s) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(LAST_EXPRESS_ID, s);
        editor.apply();
    }

    public String getPacketType() {
        return mSharedPreferences.getString(LAST_PACKET_TYPE, "");
    }

    public void setPacketType(String s) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(LAST_PACKET_TYPE, s);
        editor.apply();
    }

    public String getToken() {
        return mSharedPreferences.getString(TOKEN, "");
    }

    public void setToken(String s) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(TOKEN, s);
        editor.apply();
    }

    public String getUsername() {
        return mSharedPreferences.getString(USERNAME, "");
    }

    public void setUsername(String s) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(USERNAME, s);
        editor.apply();
    }

    public String getP() {
        return mSharedPreferences.getString(P, "");
    }

    public void setP(String s) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(P, s);
        editor.apply();
    }
}
