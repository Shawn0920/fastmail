package com.shawn.fastmail.base;

import android.content.Context;

import com.shawn.fastmail.R;
import com.shawn.fastmail.utils.DimensionUtils;

/**
 * 描述：提示框基类
 *
 * @author shawn
 * @date 2019/2/19
 */
public abstract class BaseAlertDialog extends BaseDialog {
    /**
     * 调用此构造需要自己传入Style ID
     *
     * @param context    上下文环境
     * @param themeResId 资源id
     */
    public BaseAlertDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    /**
     * default styles id
     *
     * @param context 上下文环境
     */
    public BaseAlertDialog(Context context) {
        super(context, R.style.baseAlertDialog);
    }

    /**
     * 重写此方法 设置 对话框宽度
     *
     * @param context 上下文环境
     */
    @Override
    protected void init(Context context) {
        super.init(context);

        //设置最小宽度
        rootView.setMinimumWidth(DimensionUtils.getWidth() * 4 / 5);
    }
}
