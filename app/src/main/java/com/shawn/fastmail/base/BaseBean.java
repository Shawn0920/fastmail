package com.shawn.fastmail.base;

import java.io.Serializable;

/**
 * 描述：
 *
 * @author shawn
 * @date 2019/2/20
 */
public class BaseBean<T> implements Serializable {
    /**
     * 返回信息
     */
    private String msg;

    /**
     * 返回值码 前后台协商统一
     */
    private int code;

    /**
     * 返回实体
     */
    private T data;

    public String getResult() {
        return msg;
    }

    public void setResult(String message) {
        this.msg = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "BaseBean{" +
                "result='" + msg + '\'' +
                ", code=" + code +
                ", data=" + data +
                '}';
    }
    private static final long serialVersionUID = 1L;
}
