package com.shawn.fastmail.utils;

import org.json.JSONObject;

public interface Function {
    void onHandle(JSONObject params, Callback callBack);
}