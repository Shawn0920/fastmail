package com.kernal.smartvision.view;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.kernal.barcode.sdk.InitBarCodeParams;
import com.kernal.barcode.sdk.KernalBarCode;
import com.kernal.barcode.sdk.OcrScanParams;
import com.kernal.barcode.sdk.RecogResultMessage;
import com.kernal.barcode.sdk.RecogThreadManager;
import com.kernal.barcode.sdk.ScanROIParams;
import com.kernal.barcode.sdk.ThreadManager;
import com.kernal.barcode.utils.BarCodeUtils;
import com.kernal.smartvision.R;
import com.kernal.smartvision.activity.SmartvisionCameraActivity;
import com.kernal.smartvision.utils.CameraSetting;

import java.io.IOException;

/**
 * Created by user on 2018/8/28.
 */

public class BarCodePreView extends SurfaceView implements SurfaceHolder.Callback {
    SurfaceHolder mHolder;
    Camera mCamera;
    SmartvisionCameraActivity mScanner;
    boolean mFinished;
    BarCodeView barCodeView;
    KernalBarCode mBarcode = null;
    OcrScanParams ocrScanParams = new OcrScanParams();
    public Camera.Parameters params;
    private Context context;
    public static boolean isOpenFlash = false;
    public Rect frameRect = new Rect();//自动放大二维码倍数的扫描框
    public Point screenResolution, cameraResolution;
    public boolean isStartWeakLightDetection = false;
    RecogResultMessage recogResultMessage;

    public BarCodePreView(SmartvisionCameraActivity s, Context context, BarCodeView barCodeView) {
        super(context);
        this.context = context;
        mBarcode = new KernalBarCode(context);
        this.barCodeView = barCodeView;
        mScanner = s;
        /**
         * 初始化识别引擎
         */
        String setting = BarCodeUtils.getInstance(context).loadDefaultSettings();
        InitBarCodeParams initBarCodeParams = new InitBarCodeParams();
        initBarCodeParams.setSettings(setting);//设置支持编码格式和其他设置
        int initBarCodeEngine = mBarcode.initBarCodeEngine(initBarCodeParams);
        if (initBarCodeEngine != 0) {
            Toast.makeText(context, context.getString(R.string.initException) + initBarCodeEngine, Toast.LENGTH_SHORT).show();
        }
        // Initialize the draw-on-top companion
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) { // 竖屏
            barCodeView.mScanRect = new Rect(10, 5, 90, 18);
        } else {
            barCodeView.mScanRect = new Rect(30, 20, 80, 80);
        }
        ScanROIParams scanROIParams = new ScanROIParams();
        scanROIParams.setTopLeftX(barCodeView.mScanRect.left);
        scanROIParams.setTopLeftY(barCodeView.mScanRect.top);
        scanROIParams.setBottomRightX(barCodeView.mScanRect.right);
        scanROIParams.setBottomRightY(barCodeView.mScanRect.bottom);
        scanROIParams.setMappingMode(1);
        /**
         *设置扫描框敏感区域
         */
        mBarcode.SetScanROI(scanROIParams);


        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        /**
         * 自动放大二维码倍数
         */
        screenResolution = BarCodeUtils.getInstance(context).getDisplaySize();
        frameRect.left = (barCodeView.mScanRect.left * screenResolution.x) / 100;
        frameRect.right = (barCodeView.mScanRect.right * screenResolution.x) / 100;
        frameRect.top = (barCodeView.mScanRect.top * screenResolution.y) / 100;
        frameRect.bottom = (barCodeView.mScanRect.bottom * screenResolution.y) / 100;

    }

    private byte[] tempData;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {

            /**
             * 防止返回卡顿
             * 识别函数
             */
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {  //竖屏
                ocrScanParams.setPreWidth(params.getPreviewSize().height);
                ocrScanParams.setPreHeight(params.getPreviewSize().width);
                /**
                 * 将数据源旋转正了，有利于加快识别速度
                 */
                ocrScanParams.setData(BarCodeUtils.getInstance(context).rotateYUV420Degree90(tempData, params.getPreviewSize().width, params.getPreviewSize().height));

            } else {
                ocrScanParams.setPreWidth(params.getPreviewSize().width);
                ocrScanParams.setPreHeight(params.getPreviewSize().height);
                ocrScanParams.setData(tempData);
            }
            if (!mScanner.isHaveFinished()) {
                recogResultMessage = mBarcode.startOCRScan(ocrScanParams);
            }
            if (recogResultMessage.getErrorException() == 0 && recogResultMessage.getBarString() != null && !recogResultMessage.getBarString().equals("")) {
                mScanner.scanComplete(recogResultMessage.getBarString(), recogResultMessage.getBarStringType());
                return;
            }

        }
    };
    Runnable amplifyQRCodeRunnable1 = new Runnable() {
        @Override
        public void run() {
            /**
             * 放大二维码大小接口
             */
            if (mCamera != null) {
                BarCodeUtils.getInstance(context).decode(tempData, params.getPreviewSize().width, params.getPreviewSize().height, mCamera, frameRect, screenResolution, cameraResolution);
            }

            if (isStartWeakLightDetection) {
                /**
                 * 弱光检测
                 */
//                boolean isWeakLight = BarCodeUtils.getInstance(context).analysisColor(tempData, params.getPreviewSize().width, params.getPreviewSize().height);
//                if (isWeakLight) {
//                    mScanner.mAutoFocusHandler.sendEmptyMessage(101);
//                } else {
//                    if (!isOpenFlash) {
//                        mScanner.mAutoFocusHandler.sendEmptyMessage(102);
//                    }
//                }
            }
            /**
             * 防止第一次进入相机，取得的数据是黑图，从第二帧图像开始分析
             */
            isStartWeakLightDetection = true;
        }
    };

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        try {
            mCamera = Camera.open();
            mCamera.setPreviewDisplay(holder);
            // Preview callback used whenever new viewfinder frame is available
            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    if (mScanner.isInFront == false) {
                        return;
                    }
                    tempData = data;
                    if (tempData != null) {
                        params = camera.getParameters();
                        RecogThreadManager.getInstance().execute(runnable);
                        ThreadManager.getInstance().execute(amplifyQRCodeRunnable1);
                    }
                }
            });
            mScanner.mAutoFocusHandler.sendEmptyMessage(100);
        } catch (IOException exception) {
            mCamera.release();
            mCamera = null;
        }
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        // Because the CameraDevice object is not a shared resource, it's very
        // important to release it when the activity is paused.
        closeCamera();
    }

    public void closeCamera() {
        mFinished = true;
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        if (isOpenFlash) {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        }
        Point prewResolution;
        if ((float) layoutWidth1 / layoutHeight1 == 0.75 || (float) layoutHeight1 / layoutWidth1 == 0.75) {
            prewResolution = BarCodeUtils.getInstance(context).findBestPreviewSizeValue(parameters, 1280, 960, layoutWidth1, layoutHeight1);
        } else {
            prewResolution = BarCodeUtils.getInstance(context).findBestPreviewSizeValue(parameters, 1920, 1080, layoutWidth1, layoutHeight1);
        }
        parameters.setPreviewSize(prewResolution.x, prewResolution.y);
        mCamera.setParameters(parameters);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) { // 竖屏
            mCamera.setDisplayOrientation(90);
        }
        mCamera.startPreview();
        cameraResolution = new Point();
        cameraResolution.x = mCamera.getParameters().getPreviewSize().width;
        cameraResolution.y = mCamera.getParameters().getPreviewSize().height;

    }

    public void autoFocus() {

        if (mCamera != null) {
            try {
                if (mCamera.getParameters().getSupportedFocusModes() != null
                        && mCamera
                        .getParameters()
                        .getSupportedFocusModes()
                        .contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    try {
                        mCamera.autoFocus(null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mScanner.mAutoFocusHandler.sendEmptyMessageDelayed(
                            100, 2000);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public static int layoutWidth1;
    public static int layoutHeight1;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        short desiredWidth;
        short desiredHeight;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            if ((float) widthSize / heightSize == 0.75) {
                desiredWidth = 960;
                desiredHeight = 1280;
            } else {
                desiredWidth = 1080;
                desiredHeight = 1920;
            }

        } else {
            if ((float) heightSize / widthSize == 0.75) {
                desiredWidth = 1280;
                desiredHeight = 960;
            } else {
                desiredWidth = 1920;
                desiredHeight = 1080;
            }

        }
        float radio = (float) desiredWidth / (float) desiredHeight;
        boolean layoutWidth = false;
        boolean layoutHeight = false;
        if (widthMode == 1073741824) {
            layoutWidth1 = widthSize;
        } else if (widthMode == -2147483648) {
            layoutWidth1 = Math.min(desiredWidth, widthSize);
        } else {
            layoutWidth1 = desiredWidth;
        }
        if (heightMode == 1073741824) {
            layoutHeight1 = heightSize;
        } else if (heightMode == -2147483648) {
            layoutHeight1 = Math.min(desiredHeight, heightSize);
        } else {
            layoutHeight1 = desiredHeight;
        }
        float layoutRadio = (float) layoutWidth1 / (float) layoutHeight1;
        if (layoutRadio > radio) {
            layoutHeight1 = (int) ((float) layoutWidth1 / radio);
        } else {
            layoutWidth1 = (int) ((float) layoutHeight1 * radio);
        }

        this.setMeasuredDimension(layoutWidth1, layoutHeight1);
        //System.out.println("布局宽高:"+layoutWidth1+"*"+layoutHeight1);
    }

    public void openOrClosedFlash() {
        // 闪光灯点击事件
        try {
            if (mCamera == null) {
                mCamera = Camera.open();
            }
            params = mCamera.getParameters();
            // 魅族4设备
            if ("MX4".equals(Build.MODEL)) {
                mCamera.stopPreview();
            }
            if (params.getSupportedFlashModes() != null
                    && params.getSupportedFlashModes().contains(
                    Camera.Parameters.FLASH_MODE_TORCH)
                    && params.getSupportedFlashModes().contains(
                    Camera.Parameters.FLASH_MODE_OFF)) {
                if (!isOpenFlash) {
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                    }
                    mCamera.setParameters(params);

                } else {
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                    }
                    mCamera.setParameters(params);

                }
            } else {
                Toast.makeText(context,
                        context.getString(R.string.unsupportflash),
                        Toast.LENGTH_SHORT).show();
            }
            // 魅族4设备
            if ("MX4".equals(Build.MODEL)) {
                mCamera.startPreview();
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /**
     * 操作闪光灯
     *
     * @param open
     */
    public void operateFlash(boolean open) {
        if (open) {
            CameraSetting.getInstance(mScanner.getApplicationContext()).openCameraFlash(mCamera);
        } else {
            CameraSetting.getInstance(mScanner.getApplicationContext()).closedCameraFlash(mCamera);
        }
    }
}
