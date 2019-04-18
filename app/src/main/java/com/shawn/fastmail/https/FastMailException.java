package com.shawn.fastmail.https;

/**
 * 描述：
 *
 * @author shawn
 * @date 2019/2/28
 */
public class FastMailException extends Exception {
    private static final long serialVersionUID = 1L;
    public FastMailException() {
        super("众便利服务器异常");
    }

    public FastMailException(String message) {
        super("众便利服务器异常："+message);
    }
}
