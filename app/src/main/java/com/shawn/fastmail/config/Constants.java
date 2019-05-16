package com.shawn.fastmail.config;

import com.shawn.fastmail.BuildConfig;

/**
 * 描述：
 *
 * @author shawn
 * @date 2019/2/20
 */
public class Constants {

//    public static String BASE_URL = "http://zblapi.bajiedai.com.cn/tender/";
    public static String BASE_URL = BuildConfig.BASE_SERVER_URL;

    public static final String TAG = "TAG";

    public static final int FILECHOOSER_RESULTCODE = 10101;

    public static final String APP_CACAHE_DIRNAME = "/webcache";

    public static final String OCR_APP_KEY = "6R1dfMLVKdYPD9KA30M0Ub9U";// 替换您申请的合合信息授权提供的APP_KEY;20170701


    public static final class JSMethodName {
        public static final String qrResult = "qrResult";
        public static final String barResult = "barResult";
        public static final String back = "back";
    }


    public static final class NativeMethodName{
        public static final String qrcode = "qrcode";
        public static final String inbound = "inbound";
        public static final String barcode = "barcode";
        public static final String closeApp = "closeApp";
        public static final String getVersionCode = "getVersionCode";
        public static final String refresh = "refresh";
        public static final String toast = "toast";
        public static final String initBarCode = "initBarCode";
        public static final String scannerVoice = "scannerVoice";

    }
}
