package com.shawn.fastmail.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.util.AttributeSet;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.shawn.fastmail.R;
import com.shawn.fastmail.config.Constants;


/**
 * 描述：有进度条的WebView
 *
 * @author shawn
 * @date 2019/3/8
 */
public class ProgressWebView extends WebView {

    public ProgressBar progressbar;
    private String jsSession;
    private Activity mContext;
    private WebChromeClient mWebChromeClient;

    public ProgressWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if(context instanceof Activity) {
            this.mContext = (Activity) context;
        }
        progressbar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        progressbar.setProgressDrawable(getResources().getDrawable(R.drawable.webview_progress_color));
//        progressbar.setProgressDrawable(new ColorDrawable(getResources().getColor(R.color.webviewProgressbar)));

        int roughness;
        try {
            roughness = 4;
        } catch (Resources.NotFoundException e) {
            roughness = 6;
        }
        progressbar.setLayoutParams(new LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, roughness, 0, 0));
        addView(progressbar);
//        mWebChromeClient = new WebChromeClient();
//        setWebChromeClient(mWebChromeClient);
    }

    public WebChromeClient getmWebChromeClient() {
        return mWebChromeClient;
    }

    public class WebChromeClient extends android.webkit.WebChromeClient {
        //关键代码，以下函数是没有API文档的，所以在Eclipse中会报错，如果添加了@Override关键字在这里的话。

        private ValueCallback mUploadMessage,mUploadCallbackAboveL;

        public ValueCallback getmUploadMessage() {
            return mUploadMessage;
        }

        public ValueCallback getmUploadCallbackAboveL() {
            return mUploadCallbackAboveL;
        }

        // For Android 3.0+
        public void openFileChooser(ValueCallback uploadMsg) {

            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            if(mContext != null) {
                mContext.startActivityForResult(Intent.createChooser(i, "File Chooser"),
                        Constants.FILECHOOSER_RESULTCODE);
            }

        }

        // For Android 3.0+
        public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            if(mContext != null) {
                mContext.startActivityForResult(
                        Intent.createChooser(i, "File Browser"),
                        Constants.FILECHOOSER_RESULTCODE);
            }
        }

        //For Android 4.1
        public void openFileChooser(ValueCallback uploadMsg, String acceptType, String capture) {
            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            if(mContext != null) {
                mContext.startActivityForResult(Intent.createChooser(i, "File Chooser"),
                        Constants.FILECHOOSER_RESULTCODE);
            }

        }
        // For Android 5.0+
        public boolean onShowFileChooser (WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            mUploadCallbackAboveL = filePathCallback;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            if(mContext != null) {
                mContext.startActivityForResult(
                        Intent.createChooser(i, "File Browser"),
                        Constants.FILECHOOSER_RESULTCODE);
            }
            return true;
        }
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100) {
                progressbar.setVisibility(GONE);
            } else {
                if (progressbar.getVisibility() == GONE)
                    progressbar.setVisibility(VISIBLE);
                progressbar.setProgress(newProgress);
            }
//            if (AppConfigConstants.isSetSessionstorage && !AppConfigConstants.WEIXIN_LOGIN.equals(AppConfigConstants.whoLogin)) {
//                //app登录需要传递给前端设置的参数
//                if (TextUtils.isEmpty(AppConfigConstants.KEY_WECHAT_GID_VALUE)) {
//                    jsSession = "sessionStorage.setItem('t','" + AppConfigConstants.KEY_USER_TOKEN_VALUE + "');"
//                            + "sessionStorage.setItem('u','" + AppConfigConstants.KEY_USER_ID_VALUE + "');"
//                            + "sessionStorage.setItem('userGid','" + AppConfigConstants.KEY_USER_GID_VALUE + "');"
//                            + "sessionStorage.setItem('userStatus','" + AppConfigConstants.KEY_USER_STATUS_VALUE + "');"
//                            + "sessionStorage.setItem('wechatGid','" + "app" + "');";
//                } else {
//                    jsSession = "sessionStorage.setItem('t','" + AppConfigConstants.KEY_USER_TOKEN_VALUE + "');"
//                            + "sessionStorage.setItem('u','" + AppConfigConstants.KEY_USER_ID_VALUE + "');"
//                            + "sessionStorage.setItem('userGid','" + AppConfigConstants.KEY_USER_GID_VALUE + "');"
//                            + "sessionStorage.setItem('userStatus','" + AppConfigConstants.KEY_USER_STATUS_VALUE + "');"
//                            + "sessionStorage.setItem('wechatGid','" + AppConfigConstants.KEY_WECHAT_GID_VALUE + "');";
//                }
//                LogUtil.e("jsSession:" + jsSession);
//                view.loadUrl("javascript:" + jsSession);
////                AppConfigConstants.isJsSessionLoad = 1;
//            }
            super.onProgressChanged(view, newProgress);
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        LayoutParams lp = (LayoutParams) progressbar.getLayoutParams();
        lp.x = l;
        lp.y = t;
        progressbar.setLayoutParams(lp);
        super.onScrollChanged(l, t, oldl, oldt);
    }
}
