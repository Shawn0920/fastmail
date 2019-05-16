/**
 * Project Name:IDCardScanCaller
 * File Name:PreviewActivity.java
 * Package Name:com.intsig.idcardscancaller
 * Date:2016年3月15日下午2:14:46
 * Copyright (c) 2016, 上海合合信息 All Rights Reserved.
 */

package com.shawn.fastmail.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kernal.smartvision.activity.SmartvisionCameraActivity;
import com.shawn.fastmail.R;
import com.shawn.fastmail.config.Constants;
import com.shawn.fastmail.utils.FunManager;
import com.shawn.fastmail.utils.FunctionSync;
import com.shawn.fastmail.utils.JavascriptBridge;
import com.shawn.fastmail.utils.LogUtils;
import com.shawn.fastmail.utils.ScreenUtil;
import com.intsig.exp.sdk.ExpScannerCardUtil;
import com.intsig.exp.sdk.IRecogStatusListener;
import com.shawn.fastmail.widget.ProgressWebView;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import static com.shawn.fastmail.config.Constants.APP_CACAHE_DIRNAME;

/**
 * ClassName:PreviewActivity <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON. <br/>
 * 功能：预览扫描一维码和机打手机号码 与PreviewActivity
 */
public class PreviewActivity extends Activity implements
        Camera.PreviewCallback, Camera.AutoFocusCallback {
    // private static final String TAG = "PreviewActivity";

    public static final String EXTRA_KEY_APP_KEY = "EXTRA_KEY_APP_KEY";
    public static final String EXTRA_KEY_RESULT_DATA = "EXTRA_KEY_RESULT_DATA";
    public static final String EXTRA_KEY_RESULT_TYPE = "EXTRA_KEY_RESULT_TYPE";

    private DetectThread mDetectThread = null;
    private Preview mPreview = null;
    private Camera mCamera = null;
    private int numberOfCameras;

    // The first rear facing camera
    private int defaultCameraId;

    private float mDensity = 2.0f;

    private ExpScannerCardUtil expScannerCardUtil = null;

    private String mImageFolder = Environment.getExternalStorageDirectory()
            + "/idcardscan/";
    private int mColorNormal = 0xff2A7DF3;
    private int mColorMatch = 0xff01d2ff;
    RelativeLayout rootView;

    private String url;

    private static JavascriptBridge jsb;

    private ProgressWebView webView;

    private String lastCurrentResult;
    private int lastCurrentType = 0;
    private ImageView mNext;


    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDensity = getResources().getDisplayMetrics().density;

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Window window = getWindow();

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        url = getIntent().getStringExtra("url");

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

//		window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // 隐藏当前Activity界面的导航栏, 隐藏后,点击屏幕又会显示出来.
//		View decorView = getWindow().getDecorView();
//		int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
//				| View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
//		;
//		decorView.setSystemUiVisibility(uiOptions);

        mImageFolder = this.getFilesDir().getPath();
        File file = new File(mImageFolder);
        if (!file.exists()) {
            file.mkdirs();
        }

        mPreview = new Preview(this);

        float dentisy = getResources().getDisplayMetrics().density;
        RelativeLayout root = new RelativeLayout(this);
        root.setBackgroundColor(0xAA666666);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        lp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        root.addView(mPreview, lp);
        setContentView(root);
        rootView = root;

        initCameraUi();// 客户可以在这个基础上覆盖一层ui
        // 初始化预览界面左边按钮组
        numberOfCameras = Camera.getNumberOfCameras();
        // Find the ID of the default camera
        CameraInfo cameraInfo = new CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
                defaultCameraId = i;
                break;
            }
        }

        mPreview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mCamera != null) {
                    mCamera.autoFocus(null);
                }
                return false;
            }
        });
        /*************************** init recog appkey ******START ***********************/
        expScannerCardUtil = new ExpScannerCardUtil();
        Intent intent = getIntent();
        final String appkey = intent.getStringExtra(EXTRA_KEY_APP_KEY);

        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                int ret = expScannerCardUtil.initRecognizer(getApplication(),
                        appkey);

                return ret;
            }

            @Override
            protected void onPostExecute(Integer result) {
                if (result != 0) {

                    /**
                     * 101 包名错误, 授权APP_KEY与绑定的APP包名不匹配； 102
                     * appKey错误，传递的APP_KEY填写错误； 103 超过时间限制，授权的APP_KEY超出使用时间限制；
                     * 104 达到设备上限，授权的APP_KEY使用设备数量达到限制； 201
                     * 签名错误，授权的APP_KEY与绑定的APP签名不匹配； 202 其他错误，其他未知错误，比如初始化有问题；
                     * 203 服务器错误，第一次联网验证时，因服务器问题，没有验证通过； 204
                     * 网络错误，第一次联网验证时，没有网络连接，导致没有验证通过； 205
                     * 包名/签名错误，授权的APP_KEY与绑定的APP包名和签名都不匹配；
                     */
                    new AlertDialog.Builder(PreviewActivity.this)
                            .setTitle("初始化失败")
                            .setMessage(
                                    "识别库初始失败,请检查 app key是否正确\n,错误码:" + result)
                            .setCancelable(false)
                            .setPositiveButton(android.R.string.ok,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(
                                                DialogInterface dialog,
                                                int which) {
                                            finish();
                                        }
                                    })

                            .create().show();
                }
            }
        }.execute();

    }

    boolean mNeedInitCameraInResume = false;

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        try {

            mCamera = Camera.open(defaultCameraId);// open the default camera
        } catch (Exception e) {
            e.printStackTrace();
            showFailedDialogAndFinish();
            return;
        }
        mPreview.setCamera(mCamera);
        setDisplayOrientation();
        try {
            mCamera.setOneShotPreviewCallback(this);
        } catch (Exception e) {
            e.printStackTrace();

        }
        if (mNeedInitCameraInResume) {
            mPreview.surfaceCreated(mPreview.mHolder);
            mPreview.surfaceChanged(mPreview.mHolder, 0,
                    mPreview.mSurfaceView.getWidth(),
                    mPreview.mSurfaceView.getHeight());
        }
        mNeedInitCameraInResume = true;
        isFlight = true;
        setFlashlightEnabled(isFlight);
        if (mNext != null) {
            mNext.setImageDrawable(PreviewActivity.this
                    .getResources().getDrawable(
                            R.mipmap.icon_flash_press));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);

        if (mCamera != null) {
            Camera camera = mCamera;
            mCamera = null;
            camera.setOneShotPreviewCallback(null);
            mPreview.setCamera(null);
            camera.release();
            camera = null;

        }
        isFlight = false;
        if (mNext != null) {
            mNext.setImageDrawable(PreviewActivity.this
                    .getResources().getDrawable(
                            R.mipmap.icon_flash_normal));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (expScannerCardUtil != null) {
            expScannerCardUtil.releaseRecognizer();
        }
        if (mDetectThread != null) {
            mDetectThread.stopRun();
        }

        mHandler.removeMessages(MSG_AUTO_FOCUS);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Size size = camera.getParameters().getPreviewSize();
        if (mDetectThread == null) {
            mDetectThread = new DetectThread();
            mDetectThread.start();
            /*********************************
             * 20170810--update--- 自动对焦的核心 启动handler 来进行循环对焦
             * ，如果使用camera参数设置连续对焦则不需要下面这句
             ***********************/
            // if (boolMiuiSystem()) {
            mHandler.sendEmptyMessageDelayed(MSG_AUTO_FOCUS, 200);
            // }
        }

        /********************************* 向预览线程队列中 加入预览的 data 分析是否ismatch ***********************/
        // Log.e("onPreviewFrame size", "width" + size.width + "h:" +
        // size.height);
        mDetectThread.addDetect(data, size.width, size.height);
    }

    private void showFailedDialogAndFinish() {
        new AlertDialog.Builder(this)
                .setMessage("无法连接到相机,请检查权限设置")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                finish();
                            }
                        }).create().show();
    }

    private void resumePreviewCallback() {
        if (mCamera != null) {
            mCamera.setOneShotPreviewCallback(this);
        }
    }

    /**
     * 功能：将显示的照片和预览的方向一致
     */
    private void setDisplayOrientation() {
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(defaultCameraId, info);
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result = (info.orientation - degrees + 360) % 360;
        // int result = (360 - (info.orientation + degrees) % 360) % 360;
        mCamera.setDisplayOrientation(result);

    }

    public static boolean boolSystem = false;

    public static boolean boolMiuiSystem() {
        if (boolSystem)
            return true;
        final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
        final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
        final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";
        final String KEY_EMUI_API_LEVEL = "ro.build.hw_emui_api_level";
        final String KEY_EMUI_VERSION = "ro.build.version.emui";
        final String KEY_EMUI_CONFIG_HW_SYS_VERSION = "ro.confg.hw_systemversion";
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream(new File(Environment
                    .getRootDirectory(), "build.prop")));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (prop.getProperty(KEY_MIUI_VERSION_CODE, null) != null
                || prop.getProperty(KEY_MIUI_VERSION_NAME, null) != null
                || prop.getProperty(KEY_MIUI_INTERNAL_STORAGE, null) != null
                || prop.getProperty(KEY_EMUI_API_LEVEL, null) != null
                || prop.getProperty(KEY_EMUI_VERSION, null) != null
                || prop.getProperty(KEY_EMUI_CONFIG_HW_SYS_VERSION, null) != null) {
            boolSystem = true;
            return true;
        }
        boolSystem = false;

        return false;
    }

    private void focusOnTouch() { // fox--update---2017.10.27

        /*
         * 焦点原理：这是多点对焦新增的一个焦点，首先确定中心点的x，y，x值应该就是屏幕的一半，y的值应该是预览框的中心点，
         * borderHeightFromTop是预览框距离屏幕顶点的距离，borderHeightVar是预览框的高度
         */

        int x = mPreview.mSurfaceView.getWidth() / 2;
        int y = (int) (ScreenUtil.dp2px(this, borderHeightFromTop) + ScreenUtil
                .dp2px(this, borderHeightVar) / 2);

        int xH = mPreview.mSurfaceView.getWidth() / 2;

        Rect rect = new Rect(x - xH, y - xH, x + xH, y + xH);
        int left = rect.left * 2000 / mPreview.mSurfaceView.getWidth() - 1000;
        int top = rect.top * 2000 / mPreview.mSurfaceView.getHeight() - 1000;
        int right = rect.right * 2000 / mPreview.mSurfaceView.getWidth() - 1000;
        int bottom = rect.bottom * 2000 / mPreview.mSurfaceView.getHeight()
                - 1000;
        // 如果超出了(-1000,1000)到(1000, 1000)的范围，则会导致相机崩溃
        left = left < -1000 ? -1000 : left;
        top = top < -1000 ? -1000 : top;
        right = right > 1000 ? 1000 : right;
        bottom = bottom > 1000 ? 1000 : bottom;
        focusOnRect(new Rect(left, top, right, bottom));
    }

    @SuppressLint("InlinedApi")
    protected void focusOnRect(Rect rect) {
        if (mCamera != null) {
            String focusMode = Parameters.FOCUS_MODE_AUTO;

            Parameters params = mCamera.getParameters(); // 先获取当前相机的参数配置对象
            final List<String> modes = params.getSupportedFocusModes();

            if (modes.contains(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                params.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            } else if (modes.contains(Parameters.FOCUS_MODE_FIXED)) {
                params.setFocusMode(Parameters.FOCUS_MODE_FIXED);
            } else if (modes.contains(Parameters.FOCUS_MODE_INFINITY)) {
                params.setFocusMode(Parameters.FOCUS_MODE_INFINITY);
            } else {
                params.setFocusMode(modes.get(0));
            }

            mCamera.cancelAutoFocus(); // 先要取消掉进程中所有的聚焦功能

            if (params.getMaxNumFocusAreas() > 0) {
                List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
                focusAreas.add(new Camera.Area(rect, 1000));
                params.setFocusAreas(focusAreas);
            }
            mCamera.setParameters(params); // 一定要记得把相应参数设置给相机
            // if (!TextUtils.equals(focusMode,
            // Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            // Log.d("FOCUS_MODE_CONTINUOUS_PICTURE", "not");
            // mHandler.sendEmptyMessageDelayed(MSG_AUTO_FOCUS, 2000);
            // }
        }
    }

    public boolean isSupported(String value, List<String> supported) {
        return supported == null ? false : supported.indexOf(value) >= 0;
    }

    private static final int MSG_AUTO_FOCUS = 100;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressLint("HandlerLeak")
        public void handleMessage(Message msg) {
            if (msg.what == MSG_AUTO_FOCUS) {
                autoFocus();
                mHandler.removeMessages(MSG_AUTO_FOCUS);
                // 两秒后进行聚焦
                mHandler.sendEmptyMessageDelayed(MSG_AUTO_FOCUS, 2000);
            }
        }

        ;
    };

    private void autoFocus() {
        if (mCamera != null) {
            try {
                mCamera.autoFocus(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    boolean isFocus = false;

    boolean isVertical = true;

    private Set<String> setResultSet = new HashSet<String>();

    // thread to detect and recognize.

    /**
     * 功能：将每一次预览的data 存入ArrayBlockingQueue 队列中，然后依次进行ismatch的验证，如果匹配就会就会进行进一步的识别
     * 注意点： 1.其中 控制预览框的位置大小，需要
     */

    public void showView(final String result, final String time, final int type) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                StringBuffer sb = new StringBuffer();
                for (String s : setResultSet) {
                    sb.append(s + "  ");
                }
//                mResultValueAll.setText("当前识别结果集：" + sb.toString() + "\n");

//                mResultValue.setText("当前识别结果：" + result + "耗时：" + time + "类型：" + type);
                LogUtils.e("=================", "当前识别结果：" + result + "耗时：" + time + "类型：" + type);
            }

        });

    }

    String lastRecgResultString = null;
    int countRecg = 0;
    ToneGenerator tone;

    private class DetectThread extends Thread {
        private ArrayBlockingQueue<byte[]> mPreviewQueue = new ArrayBlockingQueue<byte[]>(
                1);
        private int width;
        private int height;

        public void stopRun() {
            addDetect(new byte[]{0}, -1, -1);
        }

        @Override
        public void run() {
            try {
                while (true) {
                    byte[] data = mPreviewQueue.take();// block here, if no data
                    // in the queue.
                    if (data.length <= 1) {// quit the thread, if we got special
                        // byte array put by stopRun().
                        return;
                    }

                    int left = borderLeftAndRight[1];
                    int right = borderLeftAndRight[3];
                    int top = borderLeftAndRight[0];
                    int bottom = borderLeftAndRight[2];

                    int roiWidthScreen = (int) (right - left);
                    int roiHeightScreen = (int) (bottom - top);

                    //recognizeScreenExp方法传入的坐标都是竖屏对应的坐标
                    final long starttime = System.currentTimeMillis();
                    expScannerCardUtil.recognizeScreenExp(data, height, width,
                            screenwidth, screenheight, roiWidthScreen,
                            roiHeightScreen, left, top,
                            new IRecogStatusListener() {

                                @Override
                                public void onRecognizeExp(String result,
                                                           int type) {

                                    /**
                                     * 连续两帧数据一样才返回结果
                                     */
                                    if (lastRecgResultString == null) {
                                        //
                                        showView("lastRecgResultString:null,"
                                                + "result:" + result, "", type);

                                        lastRecgResultString = result;
                                        resumePreviewCallback();
                                        countRecg = 0;
                                    } else {

                                        if (result.equals(lastRecgResultString)) {

                                            long endtime = System
                                                    .currentTimeMillis();

//                                            if (tone == null) {
//                                                // 发出提示用户的声音
//                                                tone = new ToneGenerator(
//                                                        AudioManager.STREAM_MUSIC,
//                                                        ToneGenerator.MAX_VOLUME);
//                                            }
//                                            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//                                            v.vibrate(200);
//                                            tone.startTone(ToneGenerator.TONE_PROP_BEEP);
                                            setResultSet.add(result);
                                            showView(result,
                                                    (endtime - starttime)
                                                            + "ms", type);
                                            lastRecgResultString = result;
                                            scannerResult(result, type);
//                                            lastCurrentResult = result;
//                                            lastCurrentType = type;
//											resumePreviewCallback();

                                        } else {
                                            countRecg = 0;
                                            lastRecgResultString = result;
                                            resumePreviewCallback();
                                        }

                                    }

                                }

                                @Override
                                public void onRecognizeError(int arg0) {
                                    Log.e("onRecognizeError", "false");

                                    // TODO Auto-generated method stub
                                    resumePreviewCallback();
                                }
                            });

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void addDetect(byte[] data, int width, int height) {
            if (mPreviewQueue.size() == 1) {
                mPreviewQueue.clear();
            }
            mPreviewQueue.add(data);
            this.width = width;
            this.height = height;
        }
    }

    /**
     * A simple wrapper around a Camera and a SurfaceView that renders a
     * centered preview of the Camera to the surface. We need to center the
     * SurfaceView because not all devices have cameras that support preview
     * sizes at the same aspect ratio as the device's display.
     */
    private TextView mResultValue = null;

    private TextView mResultValueAll = null;


    private class Preview extends ViewGroup implements SurfaceHolder.Callback {
        private final String TAG = "Preview";
        private SurfaceView mSurfaceView = null;
        private SurfaceHolder mHolder = null;
        private Size mPreviewSize = null;
        private List<Size> mSupportedPreviewSizes = null;
        private Camera mCamera = null;
        private DetectView mDetectView = null;
        private TextView mInfoView = null;
//		private TextView mCopyRight = null;;

        public Preview(Context context) {
            super(context);
            mSurfaceView = new SurfaceView(context);
            addView(mSurfaceView);

            mInfoView = new TextView(context);
//			addView(mInfoView);

            mDetectView = new DetectView(context);
            addView(mDetectView);

//			mCopyRight = new TextView(PreviewActivity.this);
//			mCopyRight.setGravity(Gravity.CENTER);
//			mCopyRight.setText(R.string.intsig_copyright);
//			addView(mCopyRight);

            mResultValue = new TextView(PreviewActivity.this);
            mResultValue.setGravity(Gravity.CENTER);
            mResultValue.setText("");
            mResultValue.setTextSize(12);
            mResultValue.setTextColor(Color.YELLOW);
            addView(mResultValue);

            mResultValueAll = new TextView(PreviewActivity.this);
            mResultValueAll.setGravity(Gravity.CENTER);
            mResultValueAll.setText("");
            mResultValueAll.setTextColor(Color.RED);
            mResultValue.setTextSize(18);
            addView(mResultValueAll);

            mHolder = mSurfaceView.getHolder();
            mHolder.addCallback(this);
        }

        public void setCamera(Camera camera) {
            mCamera = camera;
            if (mCamera != null) {
                mSupportedPreviewSizes = mCamera.getParameters()
                        .getSupportedPreviewSizes();
                requestLayout();
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            // We purposely disregard child measurements because act as a
            // wrapper to a SurfaceView that centers the camera preview instead
            // of stretching it.
            final int width = resolveSize(getSuggestedMinimumWidth(),
                    widthMeasureSpec);
            final int height = resolveSize(getSuggestedMinimumHeight(),
                    heightMeasureSpec);
            setMeasuredDimension(width, height);

            if (mSupportedPreviewSizes != null) {
                int targetHeight = 720;
                if (width > targetHeight && width <= 1080)
                    targetHeight = width;
                mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes,
                        height, width, targetHeight);// 竖屏模式，寬高颠倒
            }
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            if (changed && getChildCount() > 0) {
                final View child = getChildAt(0);

                final int width = r - l;
                final int height = b - t;

                int previewWidth = width;
                int previewHeight = height;
                // if (mPreviewSize != null) {
                // previewWidth = mPreviewSize.height;
                // previewHeight = mPreviewSize.width;
                // }

                // Center the child SurfaceView within the parent.
                if (width * previewHeight > height * previewWidth) {
                    final int scaledChildWidth = previewWidth * height
                            / previewHeight;
                    child.layout((width - scaledChildWidth) / 2, 0,
                            (width + scaledChildWidth) / 2, height);
                    mDetectView.layout((width - scaledChildWidth) / 2, 0,
                            (width + scaledChildWidth) / 2, height);
                } else {
                    final int scaledChildHeight = previewHeight * width
                            / previewWidth;
                    child.layout(0, (height - scaledChildHeight) / 2, width,
                            (height + scaledChildHeight) / 2);
                    mDetectView.layout(0, (height - scaledChildHeight) / 2,
                            width, (height + scaledChildHeight) / 2);
                }
                getChildAt(1).layout(l, t, r, b);

                mResultValue
                        .layout(l, (int) (b - 48 * 4 * mDensity),
                                (int) (r - 8 * mDensity),
                                (int) (b - 48 * 2 * mDensity));

                mResultValueAll.layout(l, (int) (b - 48 * 2 * mDensity),
                        (int) (r - 8 * mDensity), (int) (b - 48 * mDensity));
//				mCopyRight.layout(l, (int) (b - 48 * mDensity),
//						(int) (r - 8 * mDensity), b);
            }
        }

        public void surfaceCreated(SurfaceHolder holder) {
            // The Surface has been created, acquire the camera and tell it
            // where to draw.
            try {
                if (mCamera != null) {
                    mCamera.setPreviewDisplay(holder);
                }
            } catch (IOException exception) {
                Log.e(TAG, "IOException caused by setPreviewDisplay()",
                        exception);
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // Surface will be destroyed when we return, so stop the preview.
            if (mCamera != null) {
                mCamera.stopPreview();
            }
        }

        private Size getOptimalPreviewSize(List<Size> sizes, int w, int h,
                                           int targetHeight) {
            final double ASPECT_TOLERANCE = 0.2;
            double targetRatio = (double) w / h;
            if (sizes == null)
                return null;
            Size optimalSize = null;
            double minDiff = Double.MAX_VALUE;

            // Try to find an size match aspect ratio and size
            for (Size size : sizes) {

                double ratio = (double) size.width / size.height;
                if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                    continue;
                if (Math.abs(size.height - targetHeight) < minDiff
                        && Math.abs(ratio - 1.77f) < 0.02) {

                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }

            // requirement
            if (optimalSize == null) {
                minDiff = Double.MAX_VALUE;
                for (Size size : sizes) {
                    if (Math.abs(size.height - targetHeight) < minDiff) {
                        optimalSize = size;
                        minDiff = Math.abs(size.height - targetHeight);
                    }
                }
            }
            return optimalSize;
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w,
                                   int h) {
            if (mCamera != null) {
                // Now that the size is known, set up the camera parameters and
                // begin the preview.
                Parameters parameters = mCamera.getParameters();
                parameters.setRotation(0);
                parameters.setPreviewSize(mPreviewSize.width,
                        mPreviewSize.height);
                parameters.setPreviewFormat(ImageFormat.NV21);
                requestLayout();
                mDetectView.setPreviewSize(mPreviewSize.width,
                        mPreviewSize.height);
                mInfoView.setText("preview：" + mPreviewSize.width + ","
                        + mPreviewSize.height);
                // int maxZoom = parameters.getMaxZoom();
                // if (parameters.isZoomSupported()) {
                // int zoom = (maxZoom * 3) / 10;
                // if (zoom < maxZoom && zoom > 0) {
                // parameters.setZoom(zoom);
                // }
                // }
                // parameters.setExposureCompensation(parameters.getMaxExposureCompensation());

                mCamera.setParameters(parameters);
                // focusOnTouch();// fox--update---2017.10.27

                mCamera.startPreview();
            }
        }
    }

    // 自定义相机View中定义方法
    // 放大
    // public void zoomOut() {
    // Camera.Parameters parameters = mCamera.getParameters();
    // if (!parameters.isZoomSupported()) return;
    //
    // int zoom = parameters.getZoom() + 1;
    // if (zoom < parameters.getMaxZoom()) {
    // parameters.setZoom(zoom);
    // mCamera.setParameters(parameters);
    // }
    // }
    //
    // //缩小
    // public void zoomIn() {
    // Camera.Parameters parameters = mCamera.getParameters();
    // if (!parameters.isZoomSupported()) return;
    //
    // int zoom = parameters.getZoom() - 1;
    // if (zoom >= 0) {
    // parameters.setZoom(zoom);
    // mCamera.setParameters(parameters);
    // }
    // }

    int screenwidth = 0;
    int screenheight = 0;

    /**
     * the view show bank card border.
     */
    @SuppressLint("InlinedApi")
    private class DetectView extends View {
        private Paint paint = null;
        private int[] border = null;
        private boolean match = false;
        private int previewWidth;
        private int previewHeight;
        private Context context;

        // 蒙层位置路径
        Path mClipPath = new Path();
        RectF mClipRect = new RectF();
        float mRadius = 12;
        float cornerSize = 40;// 4个角的大小
        float cornerStrokeWidth = 8;

        public DetectView(Context context) {
            super(context);
            paint = new Paint();
            this.context = context;
            paint.setColor(0x66000000);

        }

        public void setPreviewSize(int width, int height) {
            this.previewWidth = width;
            this.previewHeight = height;

        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            // upateClipRegion();
        }

        // 计算蒙层位置
        public void upateClipRegion(float scale, float scaleH) {
            float left, top, right, bottom;
            float density = getResources().getDisplayMetrics().density;
            mRadius = 0;

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
                setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }

            cornerStrokeWidth = 4 * density;
            // float scale = getWidth() / (float) previewHeight;

            Map<String, Float> map = getPositionWithArea(getWidth(),
                    getHeight());
            left = map.get("left");
            right = map.get("right");
            top = map.get("top");
            bottom = map.get("bottom");

            mClipPath.reset();
            mClipRect.set(left, top, right, bottom);
            mClipPath.addRoundRect(mClipRect, mRadius, mRadius, Direction.CW);
        }

        @Override
        public void onDraw(Canvas c) {

            screenwidth = getWidth();
            screenheight = getHeight();

            float scale = getWidth() / (float) previewHeight;
            float scaleH = getHeight() / (float) previewWidth;

            upateClipRegion(scale, scaleH);
            c.save();

            // 绘制 灰色蒙层
            c.clipPath(mClipPath, Region.Op.DIFFERENCE);
            c.drawColor(0xaa000000);
            c.drawRoundRect(mClipRect, mRadius, mRadius, paint);

            c.restore();

            if (match) {// 设置颜色
                paint.setColor(mColorMatch);
            } else {
                paint.setColor(mColorNormal);
            }
            float len = cornerSize;
            float strokeWidth = cornerStrokeWidth;
            paint.setStrokeWidth(strokeWidth);
            c.drawLine(mClipRect.left, mClipRect.top + strokeWidth / 2,
                    mClipRect.left + len + strokeWidth / 2, mClipRect.top
                            + strokeWidth / 2, paint);
            c.drawLine(mClipRect.left + strokeWidth / 2, mClipRect.top
                            + strokeWidth / 2, mClipRect.left + strokeWidth / 2,
                    mClipRect.top + len + strokeWidth / 2, paint);
            // 右上
            c.drawLine(mClipRect.right - len - strokeWidth / 2, mClipRect.top
                    + strokeWidth / 2, mClipRect.right, mClipRect.top
                    + strokeWidth / 2, paint);
            c.drawLine(mClipRect.right - strokeWidth / 2, mClipRect.top
                            + strokeWidth / 2, mClipRect.right - strokeWidth / 2,
                    mClipRect.top + len + strokeWidth / 2, paint);
            // 右下
            c.drawLine(mClipRect.right - len - strokeWidth / 2,
                    mClipRect.bottom - strokeWidth / 2, mClipRect.right,
                    mClipRect.bottom - strokeWidth / 2, paint);
            c.drawLine(mClipRect.right - strokeWidth / 2, mClipRect.bottom
                            - len - strokeWidth / 2, mClipRect.right - strokeWidth / 2,
                    mClipRect.bottom - strokeWidth / 2, paint);
            // 左下
            c.drawLine(mClipRect.left, mClipRect.bottom - strokeWidth / 2,
                    mClipRect.left + len + strokeWidth / 2, mClipRect.bottom
                            - strokeWidth / 2, paint);
            c.drawLine(mClipRect.left + strokeWidth / 2, mClipRect.bottom - len
                            - strokeWidth / 2, mClipRect.left + strokeWidth / 2,
                    mClipRect.bottom - strokeWidth / 2, paint);

            if (border != null) {
                paint.setStrokeWidth(3);
                c.drawLine(border[0] * scale, border[1] * scale, border[2]
                        * scale, border[3] * scale, paint);
                c.drawLine(border[2] * scale, border[3] * scale, border[4]
                        * scale, border[5] * scale, paint);
                c.drawLine(border[4] * scale, border[5] * scale, border[6]
                        * scale, border[7] * scale, paint);
                c.drawLine(border[6] * scale, border[7] * scale, border[0]
                        * scale, border[1] * scale, paint);

            }

            float left, top, right, bottom;

            Map<String, Float> map = getPositionWithArea(getWidth(),
                    getHeight());
            left = map.get("left");
            right = map.get("right");
            top = map.get("top");
            bottom = map.get("bottom");

            // 画动态的中心线
            paint.setColor(context.getResources().getColor(R.color.back_line_3));
            paint.setStrokeWidth(1);

            if (isVertical) {
                c.drawLine(left, top + (bottom - top) / 2, right, top
                        + (bottom - top) / 2, paint);
            } else {
                c.drawLine(left + (right - left) / 2, top, left
                        + (right - left) / 2, bottom, paint);

            }

        }
    }

    @Override
    public void onAutoFocus(boolean arg0, Camera arg1) {
        // TODO Auto-generated method stub

    }

    int[] borderLeftAndRight = new int[4];// 预览框的左右坐标---竖屏的时候

    float borderHeightVar = 50;// 预览框的高度值，如果需要改变为屏幕高度的比例值，需要初始化的时候重新赋值
    float borderHeightFromTop = 90;// 预览框离顶点的距离，也可以变为屏幕高度和预览宽高度的差值，需要初始化的时候重新赋值

    public Map<String, Float> getPositionWithArea(int newWidth, int newHeight) {

        float left = 0, top = 0, right = 0, bottom = 0;

        float borderHeight = (int) ScreenUtil.dp2px(this, borderHeightVar);

        // 注意：机打号的预览框高度设置建议是 屏幕高度的1/10,宽度 尽量与屏幕同宽
        Map<String, Float> map = new HashMap<String, Float>();
        if (isVertical) {// vertical
            int padding_leftright = 50;
            int padding_top = (int) ScreenUtil.dp2px(this, borderHeightFromTop);
            left = padding_leftright;
            right = newWidth - left;
            top = padding_top;
            bottom = borderHeight + top;

        } else {
            borderHeight = (int) ScreenUtil.dp2px(this, borderHeightVar);
            left = (newWidth - borderHeight) / 2;
            right = newWidth - left;
            float borderWidth = (int) 1000;
            top = (newHeight - borderWidth) / 2;
            bottom = newHeight - top;
        }
        borderLeftAndRight[0] = (int) top;
        borderLeftAndRight[1] = (int) left;
        borderLeftAndRight[2] = (int) bottom;
        borderLeftAndRight[3] = (int) right;
        map.put("left", left);
        map.put("right", right);

        map.put("top", top);

        map.put("bottom", bottom);
        Log.d("Preview",
                "getPositionWithArea," + "newWidth:" + newWidth + ",newHeight:"
                        + newHeight + Arrays.toString(borderLeftAndRight));

        return map;

    }

    /**
     * 初始化预览界面左边按钮组，可以选择正反面识别 正面识别 反面识别 注：如果客户想要自定义预览界面，可以参考
     * initButtonGroup中的添加方式
     */
    private void initCameraUi() {
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        // **********************************添加动态的布局
        LayoutInflater inflater = getLayoutInflater();
        View toolbar = inflater.inflate(R.layout.toolbar_scanner, null);
        rootView.addView(toolbar);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) toolbar.getLayoutParams();
        params.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        toolbar.setLayoutParams(params);
        TextView tvBack = toolbar.findViewById(R.id.toolbar_back);
        TextView mTitle = toolbar.findViewById(R.id.toolbar_title);
        mNext = toolbar.findViewById(R.id.iv_camera_flash);
        tvBack.setOnClickListener(view -> {
            finish();
        });
        mTitle.setText("入库");
        View view = inflater.inflate(R.layout.activity_scanner_web, null);
        lp.setMargins(0, (int) (ScreenUtil.getScreenHeight(this) * 0.3), 0, 0);
        rootView.addView(view, lp);
        webView = view.findViewById(R.id.progressWebview);
        initWebView(webView);
        WebView.setWebContentsDebuggingEnabled(true);

        webView.loadUrl(url);
        jsb = new JavascriptBridge(webView);
        initFunction();

        mNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (isFlight) {// 当前是闪光打开 然后关闭闪光

                    mNext.setImageDrawable(PreviewActivity.this
                            .getResources().getDrawable(
                                    R.mipmap.icon_flash_normal));

                    setFlashlightEnabled(false);
                    isFlight = false;
                } else {// 当前是闪光关闭 然后打开
                    mNext.setImageDrawable(PreviewActivity.this
                            .getResources().getDrawable(
                                    R.mipmap.icon_flash_press));

                    setFlashlightEnabled(true);
                    isFlight = true;
                }
            }
        });
    }

    boolean isFlight = false;

    private void setFlashlightEnabled(boolean isEnable) {
        try {
            if (isEnable) {
                if (mCamera != null) {
                    Camera m_Camera = mCamera;
                    Camera.Parameters mParameters;
                    mParameters = m_Camera.getParameters();
                    mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    m_Camera.setParameters(mParameters);
                }
            } else {
                if (mCamera != null) {
                    Camera m_Camera = mCamera;
                    Camera.Parameters mParameters;
                    mParameters = m_Camera.getParameters();
                    mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    m_Camera.setParameters(mParameters);
                }
            }
        } catch (Exception ex) {
        }

    }

    private void initWebView(ProgressWebView webview) {

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

        webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

    }

    /**
     * JS调用本地方法
     */
    private void initFunction() {
        FunManager.registerFunctionSync(Constants.NativeMethodName.initBarCode, new FunctionSync() {
            @Override
            public JSONObject onHandle(JSONObject param) {
                runOnUiThread(() -> resumePreviewCallback());
                return null;
            }
        });
        FunManager.registerFunctionSync(Constants.NativeMethodName.scannerVoice, new FunctionSync() {
            @Override
            public JSONObject onHandle(JSONObject params) {
                LogUtils.e("=====================", "4567890");
                if (tone == null) {
                    // 发出提示用户的声音
                    tone = new ToneGenerator(
                            AudioManager.STREAM_MUSIC,
                            ToneGenerator.MAX_VOLUME);
                }
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(200);
                tone.startTone(ToneGenerator.TONE_PROP_BEEP);
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
                    PreviewActivity.this.finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return null;
            }
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void scannerResult(String outString, int type) {
        String result = outString + "," + type;
        LogUtils.e("===============", result);
        runOnUiThread(() -> webView.loadUrl("javascript:scannerResult('" + result + "')"));
//        webView.loadUrl("javascript:scannerResult('" + result + "')");
    }
}
