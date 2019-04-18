package com.shawn.fastmail.utils;

import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Set;

/**
 * Created by caiyoulin on 2018/5/21.
 */

public class JsonUtil {

    // Constant values
    public static final int ERROR_INTEGER = 0;
    public static final double ERROR_DOUBLE = 0f;
    public static final boolean ERROR_BOOLEAN = false;
    /**
     * Get integer from JSON object
     *
     * @param jsonObject
     * @param aName
     * @return
     */
    public static int getInt(org.json.JSONObject jsonObject, String aName) {
        try {
            if (jsonObject != null && jsonObject.has(aName)) {
                return jsonObject.getInt(aName);
            }
            return 0;
        } catch (Throwable e) {
            LogUtils.e("get int from json object failed! jsonObject:" + jsonObject + "\tname:" + aName, e.toString());
        }
        return ERROR_INTEGER;
    }

    /**
     * Load
     *
     * @param jsonString JSON string who contains JSON text
     * @return
     */
    public static JSONObject loadJSON(String jsonString) {
        try {
            if (jsonString != null && jsonString.length() != 0) {
                return new JSONObject(jsonString);
            }
        } catch (Throwable e) {
            LogUtils.e("parse json string to object failed! jsonString:" + jsonString, e.toString());
        }
        return null;
    }

/*    *//**
     * 把json string 转化成类对象
     *
     * @param str
     * @param t
     * @return
     *//*
    public static <T> List<T> parseArray(String str, Class<T> t) {
        try {
            if (str != null && !"".equals(str.trim())) {
                List<T> res = JSONArray.parseArray(str.trim(), t);
                return res;
            }
        } catch (Exception e) {
            LogUtil.e( "exception:" + e.getMessage());
        }
        return null;
    }*/

    /**
     * json解析成class
     *
     * @param json
     * @param clazz
     * @return
     */
    public static <T> T Gson2Class(String json, Class<T> clazz) {
        try {
            Gson gson = new GsonBuilder().enableComplexMapKeySerialization().disableHtmlEscaping().create();
            return gson.fromJson(json, clazz);
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * Object生成Json
     *
     * @param object
     * @return
     */
    public static String Gson2String(Object object) {
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization()//支持Map的key为复杂对象的形式
                .create();
        String gsonString = gson.toJson(object);
        return gsonString;
    }

    /**
     * 把 bundle 转换成 json 对象, 只取用 String, Boolean, Integer, Long, Double
     *
     * @param bundle
     * @return
     * @throws JSONException
     */
    public static  org.json.JSONObject bundleToJSON(Bundle bundle){
        org.json.JSONObject json = new org.json.JSONObject();
        try {


        if (bundle == null || bundle.isEmpty()) {
            return json;
        }
        Set<String> keySet = bundle.keySet();
        for (String key : keySet) {
            Object object = bundle.get(key);
            if (object instanceof String || object instanceof Boolean || object instanceof Integer
                    || object instanceof Long || object instanceof Double) {
                json.put(key, object);
            }
        }        }catch (JSONException e){
            return null;
        }

        return json;
    }

    /**
     * 把 bundle 转换成 json 字符串, 只取用 String, Boolean, Integer, Long, Double
     *
     * @param bundle
     * @return
     * @throws JSONException
     */
    public static String bundleToJSONString(Bundle bundle) throws JSONException {
       JSONObject json = bundleToJSON(bundle);
        return json.toString();
    }


    /**
     * Load
     *
     * @param jsonArrayString JSON string who contains JSON text
     * @return
     */
    public static JSONArray loadJsonArray(String jsonArrayString) {
        try {
            if (jsonArrayString != null && jsonArrayString.length() != 0) {
//            	CLog.e(TAG,  "data---->" + jsonArrayString);
                return new JSONArray(jsonArrayString);
            }
        } catch (Throwable e) {
            LogUtils.e("parse json array to object failed! jsonString:" + jsonArrayString, e.toString());
        }
        return null;
    }



    /**
     * Get long from JSON object
     *
     * @param aJoObj
     * @param aName
     * @return
     */
    public static long getLong(org.json.JSONObject aJoObj, String aName) {
        try {
            if (aJoObj != null && aJoObj.has(aName)) {
                return aJoObj.getLong(aName);
            }
        } catch (Throwable e) {
            LogUtils.e("get long from json object failed! jsonObject:" + aJoObj + "\tname:" + aName, e.toString());
        }
        return ERROR_INTEGER;
    }

    /**
     * Get string from JSON object
     *
     * @param aJoObj
     * @param aName
     * @return
     */
    public static String getString(org.json.JSONObject aJoObj, String aName) {
        try {
            if (aJoObj != null && aJoObj.has(aName)) {
                String value = aJoObj.getString(aName);
                if (!value.equals(org.json.JSONObject.NULL)) {
                    return value;
                }
            }
        } catch (Throwable e) {
            LogUtils.e("get string from json object failed! jsonObject:" + aJoObj + "\tname:" + aName, e.toString());
        }
        return null;
    }


    /**Updated upstream
     * json解析成任意的类型,List<Bean>等所有类型
     *
     * @param json
     * @param type
     * @return
     */
    public static <T> T Gson2Type(String json, Type type) {
        try {
            Gson gson = new GsonBuilder().enableComplexMapKeySerialization().disableHtmlEscaping().create();
            return gson.fromJson(json, type);
        }catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }
    /**
     * json解析成任意的类型,List<Bean>等所有类型
     *
     * @param json
     * @param type
     * @return
     */
    public static <T> T Gson2TypeNoEscaping(String json, Type type) {
        try {
            Gson gson = new GsonBuilder().enableComplexMapKeySerialization().disableHtmlEscaping().create();
            return gson.fromJson(json, type);
        }catch (Throwable tr){
            tr.printStackTrace();
            return null;
        }
    }
    /**Updated upstream
     * put string to JSON object
     *
     * @param aJoObj
     * @param aName
     * @return
     */
    public static JSONObject putObject(JSONObject aJoObj, String aName, Object aValue) {
        try {
            if (aJoObj == null) {
                aJoObj = new JSONObject();
            }
            aJoObj.put(aName,aValue);
        } catch (Throwable e) {
            LogUtils.e("get string from json object failed! jsonObject:" + aJoObj + "\tname:" + aName, e.toString());

        }
        return aJoObj;
    }




    /**
     * Get boolean from JSON object
     *
     * @param aJoObj
     * @param aName
     * @return
     */
    public static boolean getBoolean(org.json.JSONObject aJoObj, String aName) {
        try {
            if (aJoObj != null && aJoObj.has(aName)) {
                return aJoObj.getBoolean(aName);
            }
        } catch (Throwable e) {
            LogUtils.e("get boolean from json object failed! jsonObject:" + aJoObj + "\tname:" + aName, e.toString());
        }
        return ERROR_BOOLEAN;
    }


    /**
     * Get double from JSON object
     *
     * @param aJoObj
     * @param aName
     * @return
     */
    public static double getDouble(org.json.JSONObject aJoObj, String aName) {
        try {
            if (aJoObj != null && aJoObj.has(aName)) {
                return aJoObj.getDouble(aName);
            }
        } catch (Throwable e) {
            LogUtils.e("get double from json object failed! jsonObject:" + aJoObj + "\tname:" + aName, e.toString());
        }
        return ERROR_DOUBLE;
    }

    /**
     * Get sub object from JSON object
     *
     * @param aJoObj
     * @param aName
     * @return
     */
    public static org.json.JSONObject getJSONObject(org.json.JSONObject aJoObj, String aName) {
        try {
            if (aJoObj != null && aJoObj.has(aName)) {
                return aJoObj.getJSONObject(aName);
            }
        } catch (Throwable e) {
            LogUtils.e("get jsonObject from json object failed! jsonObject:" + aJoObj + "\tname:" + aName, e.toString());
        }
        return null;
    }

    /**
     * Get sub object from JSON object
     *
     * @param aJoObj
     * @param aName
     * @return
     */
    public static JSONArray getJSONArray(JSONObject aJoObj, String aName) {
        try {
            if (aJoObj != null && aJoObj.has(aName)) {
                return aJoObj.getJSONArray(aName);
            }
        } catch (Throwable e) {
            LogUtils.e("get jsonObject from json object failed! jsonObject:" + aJoObj + "\tname:" + aName, e.toString());
        }
        return null;
    }
}
