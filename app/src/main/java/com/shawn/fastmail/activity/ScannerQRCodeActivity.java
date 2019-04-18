package com.shawn.fastmail.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.Log;
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
public class ScannerQRCodeActivity extends BaseActivity implements QRCodeView.Delegate {

    private ZXingView mZXingView;
    private TextView mToolbarTitle, mToolbarBack;

    @Override
    protected int onLayout() {
        return R.layout.activity_scanner_qr_code;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mZXingView = findViewById(R.id.zxingview);
        mToolbarTitle = findViewById(R.id.toolbar_title);
        mToolbarBack = findViewById(R.id.toolbar_back);

        mToolbarBack.setOnClickListener(view -> {
            ScannerQRCodeActivity.this.finish();
        });

        mToolbarTitle.setText("扫码取件");

        mZXingView.setDelegate(this);

        // 仅识别扫描框中的码
        mZXingView.getScanBoxView().setOnlyDecodeScanBoxArea(true);

        mZXingView.setType(BarcodeType.TWO_DIMENSION, null); // 只识别二维条码
        mZXingView.startSpotAndShowRect(); // 显示扫描框，并开始识别
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
        Log.e("==========", result);
        setResult(RESULT_OK, intent);
        vibrate();
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
