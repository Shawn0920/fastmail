package com.shawn.fastmail.dialog;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.shawn.fastmail.R;
import com.shawn.fastmail.base.BaseDialog;

import butterknife.BindView;

/**
 * 描述：
 *
 * @author shawn
 * @date 2018/10/17
 */
public class AppLoading extends BaseDialog {

    @BindView(R.id.iv)
    ImageView iv;

    private Context mContext;

    public AppLoading(Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
        init();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        iv.clearAnimation();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_app_loading;
    }

    @Override
    protected void onCreateDialog() {



    }

    private void init(){
        Animation rotate = AnimationUtils.loadAnimation(mContext, R.anim.rotate_anim);
        LinearInterpolator lin = new LinearInterpolator();
        rotate.setInterpolator(lin);
        if (rotate != null) {
            iv.startAnimation(rotate);
        } else {
            iv.setAnimation(rotate);
            iv.startAnimation(rotate);
        }
    }


}
