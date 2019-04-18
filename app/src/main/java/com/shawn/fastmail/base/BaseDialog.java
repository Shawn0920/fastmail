package com.shawn.fastmail.base;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;

import butterknife.ButterKnife;

/**
 * 描述：dialog基类
 *
 * @author shawn
 * @date 2019/2/19
 */
public abstract class BaseDialog extends Dialog {

    protected View rootView;
    protected Context mContext;

    /**
     * 重写父类
     */
    public BaseDialog(Context context, int themeResId) {
        super(context, themeResId);
        init(context);
    }

    public BaseDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init(context);
    }

    public BaseDialog(Context context) {
        super(context);
        init(context);
    }

    /**
     * 初始化方法
     *
     * @param context 上下文环境
     */
    protected void init(Context context) {

        this.mContext = context;
        rootView = LayoutInflater.from(context).inflate(getLayoutId(), null);
        setContentView(rootView);
        ButterKnife.bind(this, rootView);

        onCreateDialog();
    }

    /**
     * 获取布局
     *
     * @return 资源文件R.layout.xxx
     */
    protected abstract int getLayoutId();

    /**
     * 初始化成功后调用此方法
     */
    protected abstract void onCreateDialog();

    /**
     * 通过资源id获取 文本内容
     *
     * @param resId 资源id
     * @return 内容
     */
    protected String getString(@StringRes int resId) {
        return getContext().getResources().getString(resId);
    }

    /**
     * 页面跳转方法
     *
     * @param intent 携带实体
     */
    protected void startActivity(Intent intent) {
        getContext().startActivity(intent);
    }
}
