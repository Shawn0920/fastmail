package com.shawn.fastmail.utils;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;

import com.shawn.fastmail.config.Constants;

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

    public static final String ERROR_CODE_METHOD_NO_EXIT = "10000";
    public static final String ERROR_MSG_METHOD_NO_EXIT = "method no exit";
    public static final String ERROR_MSG_UNKNOW_EXCEPTION = "unknow exception";
    public static final String ERROR_CODE_UNKNOW_EXCEPTION = "20000";
    public static final String RES_CODE_SUCCESS = "0000";
    public static final String RES_MSG_SUCCESS = "success";

    public static final String API_NAMESPACE = "__JavascriptBridge__";
    public static final String SYNC_RESPONSE_DATA = "{\"methodName\": \"%s\",\"resCode\": \"%s\",\"resMsg\": \"%s\"}";
    public static final String JAVASCRIPT_PREFIX = "javascript:%s";
    public static final String CALLBACK_TEMPLATE = "javascript:__JavascriptBridge__.javaCallback(\"%s\", %s)";
    public static final String NOEXIT_TEMPLATE = "javascript:__JavascriptBridge__.javaCallback(\"%s\", {\"methodName\":\"%s\",\"resCode\":\"10000\",\"resMsg\":\"function no exit\"})";
    public static final String CALLJS_TEMPLATE = "javascript:__JavascriptBridge__.jsHandler(\"%s\",\"%s\",%s)";

    private WebView webView;
    private boolean isJsbReady;
    Map<String, Callback> jsCallbacks;
    Map<String, JSONObject> callJsFunctionQ;
    private static long seed = 0;

    private static long getSerial(){
        return ++seed;
    }

    /**
     * java对js的调用命令封装
     * @author azrael
     *
     */
    class Command{
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
        public String toString(){
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
        public void release(){
            this.serial = 0;
            this.cmd = null;
            this.params = null;
            this.callback = null;
        }

    }


    public void clearComman(){
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
        webView.addJavascriptInterface(new JavaScriptInterface(),API_NAMESPACE);
    }

    public synchronized void callJsFunction(String functionName, Bundle params, Callback callBackFunction){
        callJsFunction(functionName,JsonUtil.bundleToJSON(params),callBackFunction);
    }

    /**
     * 调用js方法
     * @param functionName
     * @param params
     * @param callBackFunction
     */
    public synchronized void callJsFunction(String functionName, JSONObject params, Callback callBackFunction){
        try{
            LogUtils.e("==========================jsb callJsFunction functionName :" + functionName );
            LogUtils.e("==========================jsb callJsFunction params :" +params);
            if (TextUtils.isEmpty(functionName)){
                return;
            }
            String id = "";
            if (callBackFunction != null) {
                id = String.valueOf(System.currentTimeMillis());
                jsCallbacks.put(id, callBackFunction);
            }
            if (!isJsbReady){
                LogUtils.e("==========================jsb callJsFunctionQ functionName :" + functionName);
                LogUtils.e("==========================jsb callJsFunctionQ params :" + params);
                callJsFunctionQ.put(functionName,params);
                return;
            }
            LogUtils.e("==========================jsb callJsFunction id :" + id);
            loadUrl(String.format(CALLJS_TEMPLATE,functionName,id,params));
        }catch (Exception e){
            LogUtils.e("==========================jsb callJsFunction error :" + e.getMessage());
        }
    }

    private void loadUrl(final String url){
        UIUtils.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                LogUtils.d("==========================jsb loadUrl :" + url);
                webView.loadUrl(url);
            }
        });
    }

    /**
     * 调用js方法
     * @param functionName
     */
    public synchronized void callJsFunction(String functionName){
        callJsFunction(functionName,new JSONObject());
    }

    /**
     * 调用js方法
     * @param functionName
     * @param params
     */
    public synchronized void callJsFunction(String functionName, JSONObject params){
        callJsFunction(functionName,params,null);
    }

    /**
     * 调用js方法
     * @param functionName
     * @param params
     */
    public synchronized void callJsFunction(String functionName, Bundle params){
        callJsFunction(functionName,JsonUtil.bundleToJSON(params),null);
    }

    /**
     * 请求调用js方法(老jsb的实现方式)
     * @param cmd
     * @param params
     * @param callback
     */
    public void require(String cmd, Bundle params, Callback callback){
        Command command = new Command(cmd, JsonUtil.bundleToJSON(params), callback);
        commandMap.put(command.serial, command);
        commandQueue.add(command);
    }

    class JavaScriptInterface {

        /**
         * js 调用 java 方法 （同步）
         * @param functionName
         * @param params
         * @return
         */
        @android.webkit.JavascriptInterface
        public String requireSync(String functionName, String params){
            isJsbReady = true;
            LogUtils.d("==========================jsb requireSync functionName :" + functionName );
            LogUtils.d("==========================jsb requireSync params :" +params);
            synchronized (JavascriptBridge.this){
                JSONObject r = new JSONObject();
                try {
                    r.put("methodName",functionName);
                    if (TextUtils.isEmpty(functionName)){
                        r.put("resCode",ERROR_CODE_METHOD_NO_EXIT);
                        r.put("resMsg",ERROR_MSG_METHOD_NO_EXIT);
                        return r.toString();
                    }
                    FunctionSync javaSyncFunction = FunManager.getFunctionSync(functionName);
                    if (javaSyncFunction == null){
                        r.put("resCode",ERROR_CODE_METHOD_NO_EXIT);
                        r.put("resMsg",ERROR_MSG_METHOD_NO_EXIT);
                        return r.toString();
                    }
                    JSONObject p  = JsonUtil.loadJSON(params);
                    if (p == null){
                        r.put("resCode",ERROR_CODE_UNKNOW_EXCEPTION);
                        r.put("resMsg",ERROR_MSG_UNKNOW_EXCEPTION);
                        return r.toString();
                    }
                    JSONObject result = javaSyncFunction.onHandle(p);
                    if (result != null) r = result;
                    r.put("resCode",RES_CODE_SUCCESS);
                    r.put("resMsg",RES_MSG_SUCCESS);
                } catch (JSONException j){

                }
                LogUtils.d("==========================jsb requireSync return data :" +r.toString());
                return r.toString();
            }
        }

        /**
         * Js 调用 Java 方法（异步）
         * @param functionName
         * @param reqId
         * @param params
         */
        @android.webkit.JavascriptInterface
        public void requireAsync(final String functionName, final String reqId, final String params){
            isJsbReady = true;
            LogUtils.d("==========================jsb requireAsync functionName :" + functionName );
            LogUtils.d("==========================jsb requireAsync reqId :" + reqId );
            LogUtils.d("==========================jsb requireAsync params :" + params );
            BackgroundFunctionTask.runBackground(new Runnable() {
                @Override
                public void run() {
                    try{
                        String noExit =  String.format(NOEXIT_TEMPLATE,reqId,functionName);
                        if (TextUtils.isEmpty(functionName)){
                            loadUrl(noExit);
                            return;
                        }
                        Function javaAsyncFunction = FunManager.getFunction(functionName);
                        if (null == javaAsyncFunction){
                            loadUrl(noExit);
                            return;
                        }
                        JSONObject p = JsonUtil.loadJSON(params);
                        javaAsyncFunction.onHandle(p, new Callback() {
                            @Override
                            public void onComplete(JSONObject data) {
                                data = JsonUtil.putObject(data,"resId",reqId);
                                LogUtils.d("==========================jsb requireAsync onComplete :" + functionName );
                                LogUtils.d("==========================jsb requireAsync data :" + data );
                                String returnUrl = String.format(CALLBACK_TEMPLATE,reqId, data == null ? "" : data.toString());
                                LogUtils.d("==========================jsb requireAsync returnUrl :" + returnUrl );
                                loadUrl(returnUrl);
                            }
                        });}catch (Exception e){
                        LogUtils.e("==========================jsb requireAsync Error :" + e.getMessage());
                    }
                }
            });
        }



        /**
         * js 回调 java 方法
         * @param requestcode
         * @param jsonResult
         */
        @android.webkit.JavascriptInterface
        public void jsCallback(String requestcode, String jsonResult){
            isJsbReady = true;
            LogUtils.d("==========================jsb jsCallback callbackId :" + requestcode );
            LogUtils.d("==========================jsb jsCallback params :" + jsonResult );
            Callback callBack = jsCallbacks.remove(requestcode);
            if (callBack == null){
                LogUtils.d("==========================jsb jsCallback callBack == null error , callbackId :" + requestcode);
                return;
            }
            callBack.onComplete(JsonUtil.loadJSON(jsonResult));
        }

        /**
         * 校验方法是否存在
         * @param methodName
         */
        @android.webkit.JavascriptInterface
        public boolean isFunctionAvailable(String methodName){
            isJsbReady = true;
            LogUtils.d("==========================jsb isFunctionAvailable :" + methodName );
            return FunManager.isFunctionAvailable(methodName);
        }

        /**
         * 校验方法是否存在
         */
        @android.webkit.JavascriptInterface
        public void onJsbReady(String information){
            isJsbReady = true;
            LogUtils.d("==========================jsb onJsbReady!!!");
            Set<String> methods = callJsFunctionQ.keySet();
            for (String name : methods) {
                LogUtils.d("==========================jsb onJsbReady call method :" + name);
                callJsFunction(name,callJsFunctionQ.remove(name));
            }
        }

        /**
         * 打印日志
         */
        @android.webkit.JavascriptInterface
        public void log(int level,String msg){
            isJsbReady = true;
            LogUtils.print(msg);
        }

        //---------------------- 以下部分为兼容老JsBridge方案----------------------//
        //---------------------- 以下部分为兼容老JsBridge方案----------------------//
        //---------------------- 以下部分为兼容老JsBridge方案----------------------//

        /**
         * 获取需要处理的命令
         * @return 命令数组
         */
        @android.webkit.JavascriptInterface
        public String getCommands(){
            isJsbReady = true;
            synchronized (JavascriptBridge.this){
                boolean empty = commandQueue.isEmpty();
                String cmds = commandQueue.toString();
                commandQueue.clear();
                if(!empty)LogUtils.d("==========================old jsb require getCommands command : " + cmds);
                return cmds;
            }
        }


        /**
         * js执行java cmd之后返回的接口通过这个接口设置
         * @param serial
         * @param jsonResult
         */
        @android.webkit.JavascriptInterface
        public void setResult(long serial, String jsonResult){
            isJsbReady = true;
            LogUtils.d("==========================old jsb require setResult serial : " + serial);
            LogUtils.d("==========================old jsb require setResult jsonResult : " + jsonResult);
            Command command = commandMap.remove(serial);
            if(command == null){
                return;
            }
            command.release();
        }
        /**
         * js使用该方法请求java接口
         * @param cmd
         * @param params
         * @return java方法执行的返回值
         */
        @android.webkit.JavascriptInterface
        public String require(final String cmd, String params){
            isJsbReady = true;
            LogUtils.d("==========================old jsb require cmd : " + cmd + " params : " + params);
            if (!"messagebox".equals(cmd)){
                LogUtils.d("==========================old jsb require cmd : " + cmd);
                return null;
            }
            JSONObject p = JsonUtil.loadJSON(params);
            String methodName = JsonUtil.getString(p,"type");
            LogUtils.d("==========================old jsb require methodName : " + methodName);

            if (TextUtils.isEmpty(methodName)) return null;
            Function function = FunManager.getFunction(methodName);
            if (function != null){
                function.onHandle(p, new Callback() {
                    @Override
                    public void onComplete(JSONObject resultData) {
                        Command command = new Command();
                        command.cmd = JsonUtil.getString(resultData,"cmd");
                        command.params = resultData;
                        commandQueue.add(command);
                        commandMap.put(command.serial,command);
                        LogUtils.d("==========================old jsb require onComplete command : " + command);
                    }
                });
            }else {
                FunctionSync functionSync = FunManager.getFunctionSync(methodName);
                if (functionSync != null){
                    JSONObject jsonObject = functionSync.onHandle(p);
                    if (jsonObject != null)
                    {
                        return jsonObject.toString();
                    }
                }
            }
            return null;
        }


        //---------------------- 以下部分为老JsBridge存在方法----------------------//
        //---------------------- 以下部分为老JsBridge存在方法----------------------//

        @android.webkit.JavascriptInterface
        public String getUserInfoCallback(){
            isJsbReady = true;
            FunctionSync functionSync = FunManager.getFunctionSync("getUserInfoCallback");
            JSONObject o = null;
            if (functionSync != null){
                o = functionSync.onHandle(null);
            }
            return  o == null ? null : o.toString();
        }

        @android.webkit.JavascriptInterface
        public String getZuid(){
            isJsbReady = true;
            FunctionSync functionSync = FunManager.getFunctionSync("getZuid");
            JSONObject o = null;
            if (functionSync != null){
                o = functionSync.onHandle(null);
            }
            return  o == null ? null:o.toString();
        }

        @android.webkit.JavascriptInterface
        public void setJpushAlias(String userGid, String uid){
            isJsbReady = true;
            FunctionSync functionSync = FunManager.getFunctionSync("setJpushAlias");
            if (functionSync != null){
                JSONObject object = new JSONObject();
                JsonUtil.putObject(object,"userGid",userGid);
                JsonUtil.putObject(object,"uid",uid);
                functionSync.onHandle(object);
            }
        }

        @android.webkit.JavascriptInterface
        public void qrcode() {
            FunctionSync functionSync = FunManager.getFunctionSync(Constants.NativeMethodName.qrcode);
            if (functionSync != null) {
                functionSync.onHandle(null);
            }
        }

        @android.webkit.JavascriptInterface
        public void inbound(String url) {
            FunctionSync functionSync = FunManager.getFunctionSync(Constants.NativeMethodName.inbound);
            if (functionSync != null) {
                JSONObject object = new JSONObject();
                JsonUtil.putObject(object,"url",url);
                functionSync.onHandle(object);
            }
        }

        @android.webkit.JavascriptInterface
        public void barcode() {
            FunctionSync functionSync = FunManager.getFunctionSync(Constants.NativeMethodName.barcode);
            if (functionSync != null) {
                functionSync.onHandle(null);
            }
        }
        @android.webkit.JavascriptInterface
        public void closeApp() {
            FunctionSync functionSync = FunManager.getFunctionSync(Constants.NativeMethodName.closeApp);
            if (functionSync != null) {
                functionSync.onHandle(null);
            }
        }
    }
}
