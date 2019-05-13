package com.kernal.smartvision.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Camera;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kernal.smartvision.R;
import com.kernal.smartvision.ocr.OCRConfigParams;
import com.kernal.smartvision.ocr.OcrTypeHelper;
import com.kernal.smartvision.utils.PermissionUtils;
import com.kernal.smartvision.view.BarCodePreView;
import com.kernal.smartvision.view.BarCodeView;
import com.kernal.smartvision.view.RectFindView;
import com.kernal.smartvision.view.VinCameraPreView;
import com.kernal.smartvision.webview.FunManager;
import com.kernal.smartvision.webview.FunctionSync;
import com.kernal.smartvision.webview.JavascriptBridge;
import com.kernal.smartvision.webview.ProgressWebView;
import com.kernal.smartvisionocr.utils.SharedPreferencesHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


/**
 * Created by WenTong on 2018/12/5.
 */

public class SmartvisionCameraActivity extends AppCompatActivity implements View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    public static final String APP_CACAHE_DIRNAME = "/webcache";

    //Ocr 类型， 0：vin 和手机号都使用； 1：vin ；  2： 手机号；
    private int OcrType;
    //识别模板参数类，包括敏感区域位置等信息
    private OcrTypeHelper ocrTypeHelper;
    //当前选中类型   1:使用 vin;     2: 使用手机号码。
    private int currentType;
    private RectFindView rectFindView;
    private Animation verticalAnimation;
    private int srcWidth, srcHeight;//, screenWidth, screenHeight;
    private boolean isScreenPortrait = true;

    private static final int ScreenHorizontal = 2;
    private static final int ScreentVertical = 1;
    private int scan_line_width;
    private RelativeLayout relativeLayout;
    private ImageView scanHorizontalLineImageView, iv_camera_flash;
    //    private ImageButton imbtn_takepic;
    private FrameLayout surfaceContainer;

    private AppCompatTextView tvBack, tvTitle;


    private boolean isOpenFlash = true;
    VinCameraPreView vinCameraPreView;
    private DisplayMetrics dm;
    int statusHeight;
    boolean isResmue = false;
    private float marginTop;


    private boolean haveFinished = false;
    private BarCodePreView barCodePreView;
    private BarCodeView barCodeView;
    public boolean isInFront = true;
    private static final int PERMISSION_REQUESTCODE = 1;
    private ImageView scanHorizontalLineImageView2;
    private Display display;
    private Animation verticalAnimation2;
    private TextView tv_prompt;

    private ProgressWebView webview;
    private static JavascriptBridge jsb;

    private String url = "http://10.102.1.51:8080/#/enter";


    public static final String[] PERMISSION = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,// 写入权限
            Manifest.permission.READ_EXTERNAL_STORAGE, // 读取权限
            Manifest.permission.CAMERA,
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        url = getIntent().getStringExtra("url");

        //判断是否需要动态授权
        if (Build.VERSION.SDK_INT >= 23) {
            //先进行权限申请
            permission();
        } else {
            //不需要动态授权直接布局
            setContentView(R.layout.smartvision_camrea);
            initView();
            layoutView();
        }

    }

    private void initView() {
        relativeLayout = (RelativeLayout) findViewById(R.id.camera_re);
        scanHorizontalLineImageView = (ImageView) findViewById(R.id.camera_scanHorizontalLineImageView);
        iv_camera_flash = (ImageView) findViewById(R.id.iv_camera_flash);
        iv_camera_flash.setOnClickListener(this);
//        imbtn_takepic = (ImageButton) findViewById(R.id.imbtn_takepic);
//        imbtn_takepic.setOnClickListener(this);
        surfaceContainer = (FrameLayout) findViewById(R.id.camera_container);
        tvTitle = (AppCompatTextView) findViewById(R.id.toolbar_title);
        tvBack = (AppCompatTextView) findViewById(R.id.toolbar_back);
        tvBack.setOnClickListener(this);

//        tvTitle.setText("入库");


        webview = (ProgressWebView) findViewById(R.id.progressWebview);
        webview.setWebViewClient(new SmartvisionCameraActivity.MyWebViewClient());
        webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                Log.e("======", newProgress + "");
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
        jsb = new JavascriptBridge(webview);
        initFunction();

    }

    private void findVinView() {
        //动态创建 SurfaceView
        vinCameraPreView = new VinCameraPreView(SmartvisionCameraActivity.this);
        setFlashlightEnabled(isOpenFlash);

        surfaceContainer.addView(vinCameraPreView);
        vinCameraPreView.setOnTextChangeListener(new VinCameraPreView.OnTextChangeListener() {
            @Override
            public void onTextChange(final String result) {
                Log.e("==================", result + "xxx");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        phoneScannerResult(result);
//                        removeVinView();
//                        findQrView();
                    }
                });
            }
        });
        OcrType = OCRConfigParams.getOcrType(this);

        currentType = SharedPreferencesHelper.getInt(SmartvisionCameraActivity.this, "currentType", 2);
        //判断使用的是 vin 还是手机号
        if (OcrType != 0) {
            currentType = OcrType;
            SharedPreferencesHelper.putInt(SmartvisionCameraActivity.this, "currentType", currentType);
        } else {
            // 读取,默认 phone
            currentType = SharedPreferencesHelper.getInt(SmartvisionCameraActivity.this, "currentType", 2);
        }

        vinCameraPreView.setCurrentType(currentType);

        layoutRectAndScanLineView();
        layoutNormalView();
//        vinCameraPreView.setZoom(true);
    }

    private void removeVinView() {
        if (vinCameraPreView != null) {
            vinCameraPreView.finishRecogn();
            relativeLayout.removeView(rectFindView);
            scanHorizontalLineImageView.clearAnimation();
            scanHorizontalLineImageView.setVisibility(View.GONE);
//            imbtn_takepic.setVisibility(View.GONE);
            haveFinished = false;
            isInFront = true;
            rectFindView = null;
            vinCameraPreView = null;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (vinCameraPreView != null) {
            if (Build.VERSION.SDK_INT > 18) {
                View decorView = getWindow().getDecorView();
                int option = View.SYSTEM_UI_FLAG_VISIBLE;
                decorView.setSystemUiVisibility(option);
            }

            isResmue = true;
            if (vinCameraPreView != null) {
                vinCameraPreView.cameraOnResume();
            }
        } else if (barCodePreView != null) {
            isInFront = true;
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (vinCameraPreView != null) {
            isResmue = false;
            if (vinCameraPreView != null) {
                vinCameraPreView.cameraOnPause();
            }
        } else if (barCodePreView != null) {
            isInFront = false;
        }
        isOpenFlash = false;
        iv_camera_flash.setImageResource(R.drawable.icon_flash_normal);
    }

    //界面的布局，主要包括两方面：扫描框的布局 和 界面其他元素布局
    private void layoutView() {
        setScreenSize(SmartvisionCameraActivity.this);
        display = getWindowManager().getDefaultDisplay();
        statusHeight = getStatusBarHeight();
        marginTop = (this.getResources().getDisplayMetrics().density * 2 + 0.5f);
        //重新计算横竖屏
        // 获取屏幕旋转的角度
        int screenRotation = getWindowManager().getDefaultDisplay().getRotation();
        if (screenRotation == 0 || screenRotation == 2) // 竖屏状态下
        {
            isScreenPortrait = true;
        } else { // 横屏状态下
            isScreenPortrait = false;
        }
//        layoutRectAndScanLineView();
//        layoutNormalView();

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) webview.getLayoutParams();
        layoutParams.height = (int) (srcHeight * 0.65);
        webview.setLayoutParams(layoutParams);


        /**
         * 进入开启扫描条码页面
         */
        findQrView();
//        findVinView();
    }

    /**
     * 扫描框及扫描线布局
     */
    private void layoutRectAndScanLineView() {

        //竖屏布局
        if (isScreenPortrait) {
            if (rectFindView != null) {
                RemoveView();
            }
            ocrTypeHelper = new OcrTypeHelper(currentType, ScreentVertical).getOcr();
            rectFindView = new RectFindView(this, ocrTypeHelper, srcWidth, srcHeight);
            relativeLayout.addView(rectFindView);
            scan_line_width = (int) (ocrTypeHelper.widthPercent * srcWidth);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(scan_line_width, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.leftMargin = 0;
            scanHorizontalLineImageView.setVisibility(View.VISIBLE);
            scanHorizontalLineImageView.setLayoutParams(layoutParams);
            verticalAnimation = new TranslateAnimation(ocrTypeHelper.leftPointXPercent * srcWidth, ocrTypeHelper.leftPointXPercent * srcWidth, ocrTypeHelper.leftPointYPercent * srcHeight - marginTop, (float) ((ocrTypeHelper.leftPointYPercent + ocrTypeHelper.heightPercent) * srcHeight) - marginTop);
            verticalAnimation.setDuration(1500);
            verticalAnimation.setRepeatCount(Animation.INFINITE);
            scanHorizontalLineImageView.startAnimation(verticalAnimation);

        } else {
            //横屏布局
            if (rectFindView != null) {
                RemoveView();
            }
            ocrTypeHelper = new OcrTypeHelper(currentType, ScreenHorizontal).getOcr();
            //扫描框
            rectFindView = new RectFindView(this, ocrTypeHelper, srcWidth, srcHeight);
            relativeLayout.addView(rectFindView);
            scan_line_width = (int) (ocrTypeHelper.widthPercent * srcWidth);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(scan_line_width, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.leftMargin = 0;
            scanHorizontalLineImageView.setLayoutParams(layoutParams);
            verticalAnimation = new TranslateAnimation(ocrTypeHelper.leftPointXPercent * srcWidth, ocrTypeHelper.leftPointXPercent * srcWidth, ocrTypeHelper.leftPointYPercent * srcHeight - marginTop, (float) ((ocrTypeHelper.leftPointYPercent + ocrTypeHelper.heightPercent) * srcHeight) - marginTop);
            verticalAnimation.setDuration(1500);
            verticalAnimation.setRepeatCount(Animation.INFINITE);
            scanHorizontalLineImageView.startAnimation(verticalAnimation);
        }
    }

    /**
     * 动画销毁
     */
    private void RemoveView() {
        if (rectFindView != null) {
            rectFindView.destroyDrawingCache();
            relativeLayout.removeView(rectFindView);
            scanHorizontalLineImageView.clearAnimation();
            rectFindView = null;
        }
    }

    /**
     * 界面其他元素布局，如照相按钮，返回等。
     */
    private void layoutNormalView() {
        if (isScreenPortrait) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams((int) (srcHeight * 0.05), (int) (srcHeight * 0.05));
            //照相按钮
            //  if (srcHeight == screenHeight) {
            layoutParams = new RelativeLayout.LayoutParams(
                    (int) (srcHeight * 0.05), (int) (srcHeight * 0.05));
            layoutParams.leftMargin = (int) (srcWidth * 0.86);
            int topmargin = (int) (int) (srcHeight * 0.05) + (int) (srcHeight * 0.02);
            layoutParams.topMargin = topmargin;
//            imbtn_takepic.setVisibility(View.VISIBLE);
//            imbtn_takepic.setLayoutParams(layoutParams);

        } else {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams((int) (srcWidth * 0.05), (int) (srcWidth * 0.05));
            //照相按钮

            layoutParams = new RelativeLayout.LayoutParams((int) (srcWidth * 0.05), (int) (srcWidth * 0.05));
            int topmargin = (int) (int) (srcHeight * 0.4) + (int) (srcHeight * 0.02);
            layoutParams.leftMargin = (int) (srcWidth * 0.7);
            layoutParams.topMargin = topmargin;
//            imbtn_takepic.setLayoutParams(layoutParams);

        }
    }

    /**
     * 获取屏幕信息
     *
     * @param context
     */
    public void setScreenSize(Context context) {
        int x, y;
        WindowManager wm = ((WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE));
        Display display = wm.getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point screenSize = new Point();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                display.getRealSize(screenSize);
                x = screenSize.x;
                y = screenSize.y;
            } else {
                display.getSize(screenSize);
                x = screenSize.x;
                y = screenSize.y;
            }
        } else {
            x = display.getWidth();
            y = display.getHeight();
        }
        srcWidth = x;
        srcHeight = y;
    }

    // 屏幕旋转完成后布局
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //重绘布局
        layoutView();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_VISIBLE);
        }

    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        //返回按钮
        if (i == R.id.toolbar_back) {
            if (vinCameraPreView != null) {
                vinCameraPreView.finishRecognize();
            }
            if (barCodePreView != null) {
                barCodePreView.closeCamera();
            }
            finish();
        } else if (i == R.id.iv_camera_flash) {
            //操作闪光灯
            isOpenFlash = !isOpenFlash;
            setFlashlightEnabled(isOpenFlash);
            iv_camera_flash.setImageResource(isOpenFlash ? R.drawable.icon_flash_press : R.drawable.icon_flash_normal);

        }
//        else if (i == R.id.imbtn_takepic) {
//            //拍照按钮
//            vinCameraPreView.setTakePicture();
//        }
    }

    // 监听返回键事件
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (vinCameraPreView != null) {
                vinCameraPreView.finishRecognize();
            }
            if (barCodePreView != null) {
                barCodePreView.closeCamera();
            }
            finish();
            return true;
        }
        return true;
    }

    /**
     * 权限申请操作
     */
    private void permission() {
        boolean isgranted = true;
        for (int i = 0; i < PERMISSION.length; i++) {
            if (ContextCompat.checkSelfPermission(this, PERMISSION[i]) != PackageManager.PERMISSION_GRANTED) {
                isgranted = false;
                break;
            }
        }

//        PermissionUtils.requestMultiPermissions(this, mPermissionGrant);
        if (!isgranted) {
            //没有授权
            PermissionUtils.requestMultiPermissions(this, mPermissionGrant);
        } else {
            //已经授权
            setContentView(R.layout.smartvision_camrea);
            initView();
            layoutView();
        }
    }

    private PermissionUtils.PermissionGrant mPermissionGrant = new PermissionUtils.PermissionGrant() {
        @Override
        public void onPermissionGranted(int requestCode) {
            switch (requestCode) {
                case PermissionUtils.CODE_MULTI_PERMISSION:

                    setContentView(R.layout.smartvision_camrea);
                    initView();
                    layoutView();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionUtils.requestPermissionsResult(this, requestCode, permissions, grantResults, mPermissionGrant);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scanHorizontalLineImageView != null) {
            scanHorizontalLineImageView.clearAnimation();
        }
        if (scanHorizontalLineImageView2 != null) {
            scanHorizontalLineImageView2.clearAnimation();
        }
        if (webview != null) {
            webview.clearCache(true);
            webview.destroy();
        }
    }

    private int getStatusBarHeight() {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, sbar = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            sbar = SmartvisionCameraActivity.this.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return sbar;
    }


    private boolean isPhone = false;

    public void qiehuan(View view) {
//        isPhone = !isPhone;
//        if (isPhone) {
//            removeQrScaner();
//            findVinView();
//        } else {
//            removeVinView();
//            findQrView();
//
//        }

//        haveFinished = false;
//        isInFront = true;

    }


    private void removeQrScaner() {
        if (barCodePreView != null) {
            barCodePreView.closeCamera();
            scanHorizontalLineImageView2.clearAnimation();
            relativeLayout.removeView(barCodeView);
            relativeLayout.removeView(scanHorizontalLineImageView2);
            relativeLayout.removeView(tv_prompt);
            surfaceContainer.removeAllViews();
            barCodePreView = null;
            barCodeView = null;
            scanHorizontalLineImageView2 = null;
            tv_prompt = null;
        }
    }


    public boolean isHaveFinished() {
        return haveFinished;
    }

    /**
     * 获取识别结果并且跳转
     *
     * @param value
     * @param type
     */
    public void scanComplete(final String value, String type) {
        if (haveFinished) {
            return;
        }
        haveFinished = true;
        isInFront = false;
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(200);
        Log.e("==========", value);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                barScannerResult(value);
//                removeQrScaner();
//                findVinView();
            }
        });

//        Intent intent=new Intent(BarCodeCamera.this,MainActivity.class);
//        intent.putExtra("BarString",value);
//        intent.putExtra("BarStringType",type);
//        setResult(RESULT_OK,intent);
//        finish();
    }

    /**
     * UI布局
     */
    public void findQrView() {
        barCodeView = new BarCodeView(this);
        barCodePreView = new BarCodePreView(this, this, barCodeView);
        setFlashlightEnabled(isOpenFlash);
//        relativeLayout = new RelativeLayout(this);
        /**
         * 扫描框UI布局
         */
        scanHorizontalLineImageView2 = new ImageView(this);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams((int) (display.getWidth() * 0.8), 5);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        scanHorizontalLineImageView2.setLayoutParams(layoutParams);
        scanHorizontalLineImageView2.setBackgroundResource(R.drawable.horizontal_line);
        relativeLayout.addView(scanHorizontalLineImageView2);
        /**
         * 提示UI布局
         */
        tv_prompt = new TextView(this);
        layoutParams = new RelativeLayout.LayoutParams((int) (display.getWidth()),
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutParams.topMargin = (int) (display.getHeight() * 0.2);
        tv_prompt.setLayoutParams(layoutParams);
        tv_prompt.setTextColor(Color.rgb(255, 255, 255));
        tv_prompt.setTextSize(25);
        tv_prompt.setGravity(Gravity.CENTER);
        relativeLayout.addView(tv_prompt);
//        relativeLayout.addView(barCodePreView);
//        relativeLayout.addView(barCodeView);
//        setContentView(barCodePreView);
        relativeLayout.addView(barCodeView);
        surfaceContainer.removeAllViews();
        surfaceContainer.addView(barCodePreView);
//        addContentView(barCodeView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//        addContentView(relativeLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)(display.getHeight()*0.3)));
        int top = (barCodeView.mScanRect.top * (display.getHeight() - 20)) / 100;
        int bottom = (barCodeView.mScanRect.bottom * (display.getHeight() - 200)) / 100;
        // 从上到下的平移动画
        verticalAnimation2 = new TranslateAnimation(0, 0, top, bottom);
        verticalAnimation2.setDuration(2000);
        verticalAnimation2.setRepeatCount(Animation.INFINITE); // 无限循环
        scanHorizontalLineImageView2.startAnimation(verticalAnimation2);

    }

    public Handler mAutoFocusHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 100) {
                if (barCodePreView != null) {
                    barCodePreView.autoFocus();
                }
            }
        }
    };


    /**
     * ============================WEebView部分=======================================
     */


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

    }

    // Web视图
    private class MyWebViewClient extends WebViewClient {
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

    }


    private void initFunction() {
        FunManager.registerFunctionSync("scannerBarCode", new FunctionSync() {
            @Override
            public JSONObject onHandle(JSONObject param) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        removeVinView();
                        findQrView();
                    }
                });

                return null;
            }
        });

        FunManager.registerFunctionSync("scannerPhone", new FunctionSync() {
            @Override
            public JSONObject onHandle(JSONObject param) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        removeQrScaner();
                        findVinView();
                    }
                });

                return null;
            }
        });
        FunManager.registerFunctionSync("inboundSuccess", new FunctionSync() {
            @Override
            public JSONObject onHandle(JSONObject param) {
                try {
                    String url = param.getString("url");
                    Log.e("==========", url);
                    Intent intent = new Intent();
                    intent.putExtra("url", url);
                    setResult(RESULT_OK, intent);
                    SmartvisionCameraActivity.this.finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return null;
            }
        });
        FunManager.registerFunctionSync("initBarCode", new FunctionSync() {
            @Override
            public JSONObject onHandle(JSONObject params) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        haveFinished = false;
                        isInFront = true;
                    }
                });

                return null;
            }
        });

        FunManager.registerFunctionSync("gaoyan484sha", new FunctionSync() {
            @Override
            public JSONObject onHandle(JSONObject params) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (vinCameraPreView != null) {
                            removeVinView();
                            findQrView();
                        } else if (barCodePreView != null) {
                            haveFinished = false;
                            isInFront = true;
                        }
                    }
                });

                return null;
            }
        });

        FunManager.registerFunctionSync("setTitle", new FunctionSync() {
            @Override
            public JSONObject onHandle(final JSONObject params) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String title = params.getString("title");
                            Log.e("==========", title);
                            tvTitle.setText(title);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });

                return null;
            }
        });

        FunManager.registerFunctionSync("barCodeSuccess", new FunctionSync() {
            @Override
            public JSONObject onHandle(final JSONObject params) {
                try {
                    Thread.sleep(1500);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            haveFinished = false;
                            isInFront = true;
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                return null;
            }
        });

    }

    @SuppressLint("SetJavaScriptEnabled")
    public void barScannerResult(String result) {
        webview.loadUrl("javascript:barScannerResult('" + result + "')");
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void phoneScannerResult(String result) {
        webview.loadUrl("javascript:phoneScannerResult('" + result + "')");
    }


    /**
     * 设置闪光灯的开启和关闭
     *
     * @param isEnable
     * @author shawn
     * @date 2019-4-28
     */
    private void setFlashlightEnabled(boolean isEnable) {
        try {
            if (isEnable) {
                if (vinCameraPreView != null) {
                    Camera m_Camera = vinCameraPreView.getCamera();
                    Camera.Parameters mParameters;
                    mParameters = m_Camera.getParameters();
                    mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    m_Camera.setParameters(mParameters);
                } else if (barCodePreView != null) {
                    Camera m_Camera = barCodePreView.getCamera();
                    Camera.Parameters mParameters;
                    mParameters = m_Camera.getParameters();
                    mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    m_Camera.setParameters(mParameters);
                }
            } else {
                if (vinCameraPreView != null) {
                    Camera.Parameters mParameters;
                    Camera m_Camera = vinCameraPreView.getCamera();
                    mParameters = m_Camera.getParameters();
                    mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    m_Camera.setParameters(mParameters);
                } else if (barCodePreView != null) {
                    Camera.Parameters mParameters;
                    Camera m_Camera = barCodePreView.getCamera();
                    mParameters = m_Camera.getParameters();
                    mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    m_Camera.setParameters(mParameters);
                }
            }
        } catch (Exception ex) {
        }

    }

}
