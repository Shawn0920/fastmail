package com.kernal.smartvision.webview;

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

import com.kernal.smartvision.R;


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

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        LayoutParams lp = (LayoutParams) progressbar.getLayoutParams();
        lp.x = l;
        lp.y = t;
        progressbar.setLayoutParams(lp);
        super.onScrollChanged(l, t, oldl, oldt);
    }
}
