package com.shawn.fastmail.config;

/**
 * 描述：
 *
 * @author shawn
 * @date 2019/2/20
 */
public class Constants {

    public static String BASE_URL = "http://zblapi.bajiedai.com.cn/tender/";

    public static final String TAG = "TAG";

    public static final int FILECHOOSER_RESULTCODE = 10101;

    public static final String APP_CACAHE_DIRNAME = "/webcache";


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
    }
}
