package com.shawn.fastmail;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.ImageView;

import com.shawn.fastmail.activity.WebActivity;
import com.shawn.fastmail.utils.SpUtil;
import com.shawn.fastmail.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

import static com.shawn.fastmail.config.RCodeManager.REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE;

/**
 * 描述：
 *
 * @author shawn
 * @date 2019/2/19
 */
public class SplashActivity extends AppCompatActivity {

    ImageView ivIcon;

    private String versionName;

    //申请三个权限，文件读写，手机序列号，相机
    //1、首先声明一个数组permissions，将需要的权限都放在里面
    String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.CAMERA};
    //2、创建一个mPermissionList，逐个判断哪些权限未授予，未授予的权限存储到mPerrrmissionList中
    List<String> mPermissionList = new ArrayList<>();

    private final int mRequestCode = 100;//权限请求码

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ivIcon = findViewById(R.id.iv_icon);

//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) { //表示未授权时
//            //进行授权
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
//        } else {
//            rightRun();
////            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) { //表示未授权时
////                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 2);
////            }else{
////                rightRun();
////            }
//
//        }

        initPermission();

    }

    //权限判断和申请
    private void initPermission() {

        mPermissionList.clear();//清空没有通过的权限

        //逐个判断你要的权限是否已经通过
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);//添加还未授予的权限
            }
        }

        //申请权限
        if (mPermissionList.size() > 0) {//有权限没有通过，需要申请
            ActivityCompat.requestPermissions(this, permissions, mRequestCode);
        }else{
            //说明权限都已经通过，可以做你想做的事情去
            rightRun();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case mRequestCode:
                boolean hasPermissionDismiss = false;//有权限没有通过
                for (int i=0;i<grantResults.length;i++){
                    if(grantResults[i] == PackageManager.PERMISSION_DENIED){
                        hasPermissionDismiss = true;
                    }
                }
                if(hasPermissionDismiss){
                    ToastUtils.show(R.string.sd_card_permission_1);
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                    this.finish();
                }else{
                    rightRun();
                }
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { //同意权限申请
//                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) { //表示未授权时
//                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 2);
//                    } else {
//                        rightRun();
//                    }
//                } else { //拒绝权限申请
//                    if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
//                    } else {
//                        ToastUtils.show(R.string.sd_card_permission_1);
//                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                        Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
//                        intent.setData(uri);
//                        startActivity(intent);
//                        this.finish();
//                    }
//                }
                break;
            case 2:
                rightRun();
                break;
            default:
                break;
        }
    }


    /**
     * 正常运行
     */
    private void rightRun() {
        try {
            PackageManager manager = getPackageManager();
            PackageInfo packageInfo = manager.getPackageInfo(this.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        AnimationSet animationSet = new AnimationSet(true);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setDuration(2000);
        animationSet.addAnimation(alphaAnimation);
        ivIcon.startAnimation(animationSet);

        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                /**
                 * 动画开始之前，记录当前软件版本
                 */
                if (SpUtil.getInstance().getDefaultVersion() == null || SpUtil.getInstance().getDefaultVersion().equals("") || !SpUtil.getInstance().getDefaultVersion().equals(versionName)) {
                    SpUtil.getInstance().setDefaultVersion(versionName);
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                /**
                 * 动画结束后进入相对应页面
                 */
                startActivity(new Intent(SplashActivity.this, WebActivity.class));
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }
}
