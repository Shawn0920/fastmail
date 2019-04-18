package com.shawn.fastmail.https;

import android.app.Dialog;
import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ProgressBar;

import com.shawn.fastmail.App;
import com.shawn.fastmail.R;
import com.shawn.fastmail.base.BaseBean;
import com.shawn.fastmail.loader.AppLoader;
import com.shawn.fastmail.utils.LogUtils;
import com.shawn.fastmail.utils.ToastUtils;

import java.net.SocketException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.shawn.fastmail.https.NetConfig.*;

/**
 * 描述：
 *
 * @author shawn
 * @date 2018/4/28
 */
public abstract class NetCallBack<T extends BaseBean> implements Callback<T> {

    private Context mContext;

    private Dialog mLoadingDialog;

    private SwipeRefreshLayout mRefreshLayout;

    private ProgressBar pbLoading;

    public NetCallBack(Context context) {
        this.mContext = context;
    }

    /**
     * 网络加载 对话框，预留功能，对话框风格出来再行封装
     *
     * @param dialog 对话框对象
     * @return 回调对象
     */
    public NetCallBack<T> setLoadingDialog(Dialog dialog) {
        if (dialog != null) {
            this.mLoadingDialog = dialog;
            if (!mLoadingDialog.isShowing()) {
                mLoadingDialog.show();
            }

        }

        return this;
    }

//    public NetCallBack<T> setLoadingDialog(Boolean isShow) {
//        if (isShow) {
//            AppLoader.showLoading(mContext);
//        }
//        return this;
//    }

    public NetCallBack<T> setLoadingDialog(Boolean b) {
        if (b) {
            AppLoader.showLoading(mContext);
        }
//        else {
//            AppLoaderCancelable.showLoading(mContext);
//        }

        return this;
    }

    public NetCallBack<T> setLoadingDialog(SwipeRefreshLayout refreshLayout) {
        if (refreshLayout != null) {
            this.mRefreshLayout = refreshLayout;
            if (!mRefreshLayout.isRefreshing()) {
                mRefreshLayout.setRefreshing(true);
            }

        }

        return this;
    }

    public NetCallBack<T> setLoadingDialog(ProgressBar pbLoading) {
        if (pbLoading != null) {
            this.pbLoading = pbLoading;
            if (this.pbLoading.getVisibility() != View.VISIBLE) {
                this.pbLoading.setVisibility(View.VISIBLE);
            }
        }
        return this;
    }

    /**
     * 加入网络访问回调保护，处理弱网产生的所有闪退问题
     *
     * @param call
     * @param response
     */
    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (App.isDebug) {
            onNetResult(call, response);
        } else {
            try {
                onNetResult(call, response);
            } catch (Exception e) {
                onFailure(call, e);
            }

        }
//        onFinish(call);
    }


    private void onNetResult(Call<T> call, Response<T> response) {
        int code = response.code();
        if (code == 200) {
            T bean = response.body();
            int resultCode = bean.getCode();
            switch (resultCode) {
                case NET_RESULT_SUCCESS:
                    LogUtils.e("shawn=====", Thread.currentThread().getName());
                    onSuccess(call, response, bean);
                    break;
                case NET_RESULT_NOT_LOGIN:
                default:
                    ToastUtils.show(bean.getResult());
                    onSystemFailure();
                    break;

            }

        } else {
            LogUtils.e("===服务端校验错误，错误码：" + code + "  错误信息：\n" + response.message());
            onFailure(call, new FastMailException());
        }

//        onFinish(call);
    }

    /**
     * 成功回调
     *
     * @param call     Retrofit 回调
     * @param response 返回实体
     * @param bean     解析实体
     */
    protected void onSuccess(Call<T> call, Response<T> response, T bean){
        onFinish();
    }


    public void onFailure(Call<T> call, Throwable t) {
        if (t instanceof SocketException && t.toString().contains("closed")) {
            return;
        }
        //关闭显示对话框
        if (mLoadingDialog != null) {
            mLoadingDialog.dismiss();
        }
        if (mRefreshLayout != null) {
            mRefreshLayout.setRefreshing(false);
        }
        if (pbLoading != null && pbLoading.getVisibility() == View.VISIBLE) {
            pbLoading.setVisibility(View.GONE);
        }
        onFinish();
//        AppLoader.stopLoading();
        ToastUtils.show(mContext.getResources().getString(R.string.net_error_999));


//        if (t instanceof SocketTimeoutException) {
//            ToastUtils.show("网络连接超时");
//        } else if (t instanceof ConnectException) {
//            ToastUtils.show("网络连接失败");
//        }
    }

    /**
     * 结束调用
     *
     */
    protected void onFinish() {
        //关闭显示对话框
        if (mLoadingDialog != null) {
            mLoadingDialog.dismiss();
        }
        if (mRefreshLayout != null) {
            mRefreshLayout.setRefreshing(false);
        }
        AppLoader.stopLoading();
//        AppLoaderCancelable.stopLoading();
    }

    protected void onSystemFailure() {
        onFinish();
    }

}
