package com.kernal.smartvision.webview;

import org.json.JSONObject;

public interface Function {
    void onHandle(JSONObject params, Callback callBack);
}