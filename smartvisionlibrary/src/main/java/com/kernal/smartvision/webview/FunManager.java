package com.kernal.smartvision.webview;

import java.util.HashMap;
import java.util.Map;

public class FunManager {

    private static final FunManager ourInstance = new FunManager();

    Map<String, FunctionSync> syncFunctions = new HashMap<>();					    //同步方法集合
    Map<String, Function> asyncFunctions = new HashMap<>();					//异步方法集合

    public static void registerFunction(String functionName, Function function){
        ourInstance.asyncFunctions.put(functionName,function);
    }

    public static void registerFunctionSync(String functionName, FunctionSync functionSync){
        ourInstance.syncFunctions.put(functionName,functionSync);
    }

    public static FunctionSync getFunctionSync(String functionSync){
        return ourInstance.syncFunctions.get(functionSync);
    }

    public static Function getFunction(String function){
        return ourInstance.asyncFunctions.get(function);
    }

    public static boolean isFunctionAvailable(String function){
        return ourInstance.asyncFunctions.containsKey(function) || ourInstance.syncFunctions.containsKey(function);
    }


}
