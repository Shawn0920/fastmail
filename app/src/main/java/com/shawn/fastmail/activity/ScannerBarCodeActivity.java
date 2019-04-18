package com.shawn.fastmail.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.shawn.fastmail.R;
import com.shawn.fastmail.base.BaseActivity;

import cn.bingoogolapple.qrcode.core.BarcodeType;
import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zxing.ZXingView;

/**
 * 描述：
 *
 * @author shawn
 * @date 2019/3/27
 */
public class ScannerBarCodeActivity extends BaseActivity implements QRCodeView.Delegate {

    private ZXingView mZXingView;

    private ImageView ivLight;

    private TextView mToolbarTitle, mToolbarBack;

    private boolean isLight;

    @Override
    public int onLayout() {
        return R.layout.activity_scanner_bar_code;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mToolbarTitle = findViewById(R.id.toolbar_title);
        mToolbarBack = findViewById(R.id.toolbar_back);
        mZXingView = findViewById(R.id.zxingview);
        ivLight = findViewById(R.id.iv_light);

        mToolbarTitle.setText("扫码查件");

        mZXingView.setDelegate(this);

        // 仅识别扫描框中的码
        mZXingView.getScanBoxView().setOnlyDecodeScanBoxArea(true);

        mZXingView.changeToScanBarcodeStyle(); // 切换成扫描条码样式
        mZXingView.setType(BarcodeType.ONE_DIMENSION, null); // 只识别一维条码
        mZXingView.startSpotAndShowRect(); // 显示扫描框，并开始识别

        mToolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ivLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isLight) {
                    ivLight.setImageResource(R.mipmap.icon_light_press);
                    mZXingView.openFlashlight();
                } else {
                    ivLight.setImageResource(R.mipmap.icon_light_normal);
                    mZXingView.closeFlashlight();
                }
                isLight = !isLight;
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();

        // 打开后置摄像头开始预览，但是并未开始识别
        mZXingView.startCamera();

        // 显示扫描框，并开始识别
        mZXingView.startSpotAndShowRect();
    }

    @Override
    public void onStop() {
        super.onStop();

        // 关闭摄像头预览，并且隐藏扫描框
        mZXingView.stopCamera();
    }

    @Override
    protected void onDestroy() {
        // 销毁二维码扫描控件
        mZXingView.onDestroy();
        super.onDestroy();
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        Intent intent = new Intent();
        intent.putExtra("code", result);
        vibrate();
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onCameraAmbientBrightnessChanged(boolean isDark) {
        if (isDark) {

        }
    }

    @Override
    public void onScanQRCodeOpenCameraError() {

    }

}
