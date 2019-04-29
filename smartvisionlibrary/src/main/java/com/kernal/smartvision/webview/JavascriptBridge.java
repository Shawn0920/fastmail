package com.kernal.smartvision.webview;

import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.WebView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 描述：AndroidJavascriptBridge
 * 连接 Java 和 Javascript 的桥梁
 *
 * @author shawn
 * @date 2019/3/8
 */
public class JavascriptBridge {


    public static final String API_NAMESPACE = "__JavascriptBridge__";

    private WebView webView;
    private boolean isJsbReady;
    Map<String, Callback> jsCallbacks;
    Map<String, JSONObject> callJsFunctionQ;
    private static long seed = 0;

    private static long getSerial() {
        return ++seed;
    }

    /**
     * java对js的调用命令封装
     *
     * @author azrael
     */
    class Command {
        long serial;
        String cmd;
        JSONObject params;
        Callback callback;

        public Command() {
            this.serial = getSerial();
        }


        /**
         * @param cmd
         * @param params
         * @param callback
         */
        public Command(String cmd, JSONObject params, Callback callback) {
            this();
            this.cmd = cmd;
            this.params = params;
            this.callback = callback;
        }

        /**
         * 把命令的内容序列化成json字符串
         */
        @Override
        public String toString() {
            JSONObject json = new JSONObject();
            try {
                json.put("cmd", this.cmd);
                json.put("serial", this.serial);
                json.put("params", this.params);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json.toString();
        }

        /**
         * 释放该命令保存的内容, 防止被再次触发
         */
        public void release() {
            this.serial = 0;
            this.cmd = null;
            this.params = null;
            this.callback = null;
        }

    }


    public void clearComman() {
        commandMap.clear();
        commandQueue.clear();
    }

    private HashMap<Long, Command> commandMap;

    private ArrayList<Command> commandQueue;

    public JavascriptBridge(WebView webView) {
        this.webView = webView;
        jsCallbacks = new HashMap<>();
        callJsFunctionQ = new HashMap<>();
        commandQueue = new ArrayList<>();
        commandMap = new HashMap<>();
        webView.addJavascriptInterface(new JavaScriptInterface(), API_NAMESPACE);
    }

    class JavaScriptInterface {
        @android.webkit.JavascriptInterface
        public void scannerBarCode() {
            FunctionSync functionSync = FunManager.getFunctionSync("scannerBarCode");
            if (functionSync != null) {
                functionSync.onHandle(null);
            }
        }

        @android.webkit.JavascriptInterface
        public void scannerPhone() {
            FunctionSync functionSync = FunManager.getFunctionSync("scannerPhone");
            if (functionSync != null) {
                functionSync.onHandle(null);
            }
        }

        @android.webkit.JavascriptInterface
        public void inboundSuccess(String url) {
            FunctionSync functionSync = FunManager.getFunctionSync("inboundSuccess");
            if (functionSync != null) {
                JSONObject object = new JSONObject();
                try {
                    object.put("url", url);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                functionSync.onHandle(object);
            }

        }

        @android.webkit.JavascriptInterface
        public void initBarCode(){
            FunctionSync functionSync = FunManager.getFunctionSync("initBarCode");
            if (functionSync != null) {
                functionSync.onHandle(null);
            }
        }

        @android.webkit.JavascriptInterface
        public void gaoyan484sha(){
            FunctionSync functionSync = FunManager.getFunctionSync("gaoyan484sha");
            if (functionSync != null) {
                functionSync.onHandle(null);
            }
        }

        @android.webkit.JavascriptInterface
        public void setTitle(String title) {
            FunctionSync functionSync = FunManager.getFunctionSync("setTitle");
            if (functionSync != null) {
                JSONObject object = new JSONObject();
                try {
                    object.put("title", title);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                functionSync.onHandle(object);
            }

        }

        @android.webkit.JavascriptInterface
        public void barCodeSuccess(){
            FunctionSync functionSync = FunManager.getFunctionSync("barCodeSuccess");
            if (functionSync != null) {
                functionSync.onHandle(null);
            }
        }
    }

}
