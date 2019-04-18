package com.shawn.fastmail.https;


import com.shawn.fastmail.config.Constants;

/**
 * 描述：
 *
 * @author shawn
 * @date 2018/5/2
 */
public class NetConfig {

    /**
     * 网络连接超时时间
     */
    public static final int CONNECTION_TIME = 1000 * 15;

    /**
     * 网络读取超时时间
     */
    public static final int READ_TIME_OUT = 1000 * 20;


    /**
     * 环境配置
     */
    public static int HOST;
    //生产环境标识
    public static final int HOST_PRODUCT = 1;
    //测试环境标识
    public static final int HOST_TEST = 2;
    //直连测试bug标识
    public static final int HOST_DEBUG = 3;


    /**
     * 网络返回成功
     */
    public static final int NET_RESULT_SUCCESS = 0;

    /**
     * 服务端异常
     */
    public static final int NET_RESULT_SERVER_ERROR = 100000;

    /**
     * 参数校验异常
     */
    public static final int NET_RESULT_BIND_ERROR = 100001;

    /**
     * 没有登录
     */
    public static final int NET_RESULT_NOT_LOGIN = 101000;

    /**
     * session失效
     */
    public static final int NET_RESULT_SESSION_ERROR = 101001;

    /**
     * 无效的快递公司
     */
    public static final int EXPRESS_COMPANY_INVALID = 102000;

    /**
     * 不能重复入库
     */
    public static final int PACKAGE_HAS_IN = 102001;

}
