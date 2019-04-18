package com.shawn.fastmail.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.google.gson.Gson;
import com.kernal.smartvision.activity.SmartvisionCameraActivity;
import com.shawn.fastmail.BuildConfig;
import com.shawn.fastmail.R;
import com.shawn.fastmail.base.BaseActivity;
import com.shawn.fastmail.config.Constants;
import com.shawn.fastmail.entity.JsResponse;
import com.shawn.fastmail.utils.Callback;
import com.shawn.fastmail.utils.FunManager;
import com.shawn.fastmail.utils.FunctionSync;
import com.shawn.fastmail.utils.JavascriptBridge;
import com.shawn.fastmail.utils.JsonUtil;
import com.shawn.fastmail.utils.LogUtils;
import com.shawn.fastmail.utils.ToastUtils;
import com.shawn.fastmail.widget.ProgressWebView;

import org.json.JSONObject;

import static com.shawn.fastmail.config.Constants.APP_CACAHE_DIRNAME;

/**
 * 描述：
 *
 * @author shawn
 * @date 2019/3/27
 */
public class WebActivity extends BaseActivity {

    private static final int REQUEST_CODE_RECORDER_VIDEO = 0x501;
    private static final int REQUEST_CODE_RECORDER_IMAGE = 0x502;
    private static final int REQUEST_CODE_QR = 0x503;
    private static final int REQUEST_CODE_BAR = 0x504;
    private static final int REQUEST_CODE_INBOUND = 0x505;

    private ValueCallback filePathCallback;
    private ValueCallback uploadFile;
    private Uri imageUri;

    private ProgressWebView webview;
    private static JavascriptBridge jsb;
    private ProgressWebView.WebChromeClient mWebChromeClient;

    private ValueCallback<Uri> uploadMessage;
    private ValueCallback<Uri[]> uploadMessageAboveL;

    private final static int FILE_CHOOSER_RESULT_CODE = 10000;

    //    private String url = "http://10.102.1.51:8080";
    private String url = BuildConfig.BASE_H5_URL;

    @Override
    protected int onLayout() {
        return R.layout.activity_web;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webview = findViewById(R.id.progressWebview);

        webview.setWebViewClient(new WebActivity.MyWebViewClient());
        webview.setWebChromeClient(new WebChromeClient() {
            // For Android < 3.0
            public void openFileChooser(ValueCallback<Uri> valueCallback) {
                uploadMessage = valueCallback;
                openImageChooserActivity();
            }

            // For Android >= 3.0
            public void openFileChooser(ValueCallback valueCallback, String acceptType) {
                uploadMessage = valueCallback;
                openImageChooserActivity();
            }

            //For Android >= 4.1
            public void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType, String capture) {
                uploadMessage = valueCallback;
                openImageChooserActivity();
            }

            // For Android >= 5.0
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                uploadMessageAboveL = filePathCallback;
                openImageChooserActivity();
                return true;
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                LogUtils.e("======", newProgress + "");
                if (newProgress == 100) {
                    webview.progressbar.setVisibility(View.GONE);
                } else {
                    if (webview.progressbar.getVisibility() == View.GONE)
                        webview.progressbar.setVisibility(View.VISIBLE);
                    webview.progressbar.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }


        });

        initWebView();


        webview.loadUrl(url);
        // 设置web视图客户端
//        webview.setDownloadListener(new WebActivity.MyWebViewDownLoadListener(WalletApp.getContext()));
        //TODO:JS交互
        jsb = new JavascriptBridge(webview);
        initFunction();

    }


    private void initWebView() {

        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setDomStorageEnabled(true);
        webview.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
//        boolean available = NetUtil.isNetworkAvailable(App.getContext());
//        if (!available) {
//            //无网络情况
//            webview.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);  //设置 缓存模式
//        } else {
//            webview.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);  //设置 缓存模式
//        }
        // 开启 DOM storage API 功能
        webview.getSettings().setDomStorageEnabled(true);
        //开启 database storage API 功能
        webview.getSettings().setDatabaseEnabled(true);
        String cacheDirPath = getFilesDir().getAbsolutePath() + APP_CACAHE_DIRNAME;
//      String cacheDirPath = getCacheDir().getAbsolutePath()+Constant.APP_DB_DIRNAME;
        //设置数据库缓存路径
        webview.getSettings().setDatabasePath(cacheDirPath);
        //设置  Application Caches 缓存目录
        webview.getSettings().setAppCachePath(cacheDirPath);
        //开启 Application Caches 功能
        webview.getSettings().setAppCacheEnabled(true);

        webview.getSettings().setDefaultTextEncodingName("utf-8");
        //自适应屏幕
        webview.getSettings().setUseWideViewPort(true);
        webview.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webview.getSettings().setLoadWithOverviewMode(true);
        //缩放
        webview.getSettings().setBuiltInZoomControls(true);
        webview.getSettings().setSupportZoom(true);
        webview.getSettings().setDisplayZoomControls(false);//设定缩放控件隐藏

        webview.getSettings().setTextZoom(100); // 设定webView的字体大小不随系统字体大小的改变而改变

        webview.getSettings().setGeolocationEnabled(true);

        webview.getSettings().setBlockNetworkImage(false);

        webview.setDownloadListener(new MyWebViewDownLoadListenerImpl(this));
    }

    private void openImageChooserActivity() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        startActivityForResult(Intent.createChooser(i, "Image Chooser"), FILE_CHOOSER_RESULT_CODE);
    }

    public class MyWebViewDownLoadListenerImpl extends MyWebViewDownLoadListener {
        public MyWebViewDownLoadListenerImpl(Context context) {
            super(context);
        }

        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
            super.onDownloadStart(url, userAgent, contentDisposition, mimetype, contentLength);
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }

    public class MyWebViewDownLoadListener implements DownloadListener {
        private Context context;

        public MyWebViewDownLoadListener(Context context) {
            this.context = context;
        }

        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            try {
                context.startActivity(intent);
            } catch (Throwable tr) {
                tr.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (null == uploadMessage && null == uploadMessageAboveL) return;
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if (uploadMessageAboveL != null) {
                onActivityResultAboveL(requestCode, resultCode, data);
            } else if (uploadMessage != null) {
                uploadMessage.onReceiveValue(result);
                uploadMessage = null;
            }
        }
        Uri uri = null;
        if (requestCode == REQUEST_CODE_RECORDER_VIDEO && resultCode == RESULT_OK && data != null) {
            uri = data.getData();
        }
        if (requestCode == REQUEST_CODE_RECORDER_IMAGE && resultCode == RESULT_OK) {
            uri = imageUri;//拍照片data不会返回uri，用之前暂存下的
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//android 5.0及以上
            if (filePathCallback != null) {//将拍摄的照片或者视频回调给H5
                if (uri != null) {
                    filePathCallback.onReceiveValue(new Uri[]{uri});
                } else {
                    filePathCallback.onReceiveValue(null);
                }
                filePathCallback = null;
            }
        } else {//android 5.0以下
            if (uploadFile != null) {//将拍摄的照片或者视频回调给H5
                if (uri != null) {
                    uploadFile.onReceiveValue(uri);
                } else {
                    uploadFile.onReceiveValue(null);
                }
                uploadFile = null;
            }
        }
        if (requestCode == REQUEST_CODE_QR && resultCode == RESULT_OK) {
            if (data != null) {
                LogUtils.e("==========", data.getStringExtra("code"));
                funJS(Constants.JSMethodName.qrResult, data.getStringExtra("code"));
            }
        } else if (requestCode == REQUEST_CODE_BAR && resultCode == RESULT_OK) {
            if (data != null) {
                LogUtils.e("==========", data.getStringExtra("code"));
                funJS(Constants.JSMethodName.barResult, data.getStringExtra("code"));
            }
        } else if (requestCode == REQUEST_CODE_INBOUND && resultCode == RESULT_OK) {
            if (data != null) {
                webview.loadUrl(data.getStringExtra("url"));
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onActivityResultAboveL(int requestCode, int resultCode, Intent intent) {
        if (requestCode != FILE_CHOOSER_RESULT_CODE || uploadMessageAboveL == null) return;
        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                String dataString = intent.getDataString();
                ClipData clipData = intent.getClipData();
                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }
                if (dataString != null) results = new Uri[]{Uri.parse(dataString)};
            }
        }
        uploadMessageAboveL.onReceiveValue(results);
        uploadMessageAboveL = null;
    }

    // Web视图
    private class MyWebViewClient extends WebViewClient {

        //判断断网和链接超时
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            LogUtils.e("shawn======", errorCode + "");
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            if (url.startsWith("http") || url.startsWith("https")) { //http和https协议开头的执行正常的流程
                return false;
            } else {  //其他的URL则会开启一个Acitity然后去调用原生APP
                Intent in = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                if (in.resolveActivity(getPackageManager()) == null) {
                    //说明系统中不存在这个activity
                } else {
                    in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    startActivity(in);
                }
                return true;
            }

        }


        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);


        }

        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

        }

        // 添加下面2行代码来忽略SSL验证
        @Override
        public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
            sslErrorHandler.proceed();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest webResourceRequest) {
            return super.shouldOverrideUrlLoading(webView, webResourceRequest);
        }
    }

    /**
     * native调用JavaScript提供的statisticsCallback方法
     */
    private void getJsMethed(String action) {
        //调用JavaScript方法
        JsResponse jsResponse = new JsResponse();
        String json = new Gson().toJson(jsResponse);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            funJS(Constants.JSMethodName.back, "");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void mGoback() {
        if (null != webview && webview.canGoBack()) {
            webview.goBack();
        } else {
            this.onBackPressed();
        }
    }

    private void jsCallBackFun(String jsType, Bundle params) {
        jsb.require(jsType, params, new Callback() {
            @Override
            public void onComplete(JSONObject resultData) {
                LogUtils.e("native调用JavaScript返回值::" + resultData.toString());
            }
        });

        jsb.callJsFunction(jsType, params);
    }

    public void fangfa(String ss) {
        //调用JavaScript方法
        Bundle params = new Bundle();
        params.putString("ss", ss);

        jsCallBackFun("back", params);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webview != null) {
            webview.clearCache(true);
            webview.destroy();
        }
    }

    /**
     * JS调用本地方法
     */
    private void initFunction() {
        FunManager.registerFunctionSync(Constants.NativeMethodName.qrcode, new FunctionSync() {
            @Override
            public JSONObject onHandle(JSONObject param) {
                if (ContextCompat.checkSelfPermission(WebActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(WebActivity.this, ScannerQRCodeActivity.class);
                    intent.putExtra("index", 1);
                    WebActivity.this.startActivityForResult(intent, REQUEST_CODE_QR);

                } else {
                    ToastUtils.show(R.string.sd_card_permission_1);
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }
                return null;
            }
        });

        FunManager.registerFunctionSync(Constants.NativeMethodName.inbound, new FunctionSync() {
            @Override
            public JSONObject onHandle(JSONObject param) {
                String url = JsonUtil.getString(param, "url");
                Intent intent = new Intent(WebActivity.this, SmartvisionCameraActivity.class);
                intent.putExtra("url", url);
                WebActivity.this.startActivityForResult(intent, REQUEST_CODE_INBOUND);
                return null;
            }
        });

        FunManager.registerFunctionSync(Constants.NativeMethodName.barcode, new FunctionSync() {
            @Override
            public JSONObject onHandle(JSONObject param) {
                if (ContextCompat.checkSelfPermission(WebActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(WebActivity.this, ScannerBarCodeActivity.class);
                    intent.putExtra("index", 1);
                    WebActivity.this.startActivityForResult(intent, REQUEST_CODE_BAR);

                } else {
                    ToastUtils.show(R.string.sd_card_permission_1);
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }
                return null;
            }
        });
        FunManager.registerFunctionSync(Constants.NativeMethodName.closeApp, new FunctionSync() {
            @Override
            public JSONObject onHandle(JSONObject param) {
                WebActivity.this.finish();
                return null;
            }
        });

    }

    /**
     * 调用JS方法
     *
     * @param methodName
     * @param value
     */
    @SuppressLint("SetJavaScriptEnabled")
    public void funJS(String methodName, String value) {
        webview.loadUrl("javascript:" + methodName + "('" + value + "')");
    }

}
