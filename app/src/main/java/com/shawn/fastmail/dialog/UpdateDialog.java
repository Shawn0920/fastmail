package com.shawn.fastmail.dialog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.shawn.fastmail.App;
import com.shawn.fastmail.R;
import com.shawn.fastmail.base.BaseDialog;
import com.shawn.fastmail.https.HttpsUtils;
import com.shawn.fastmail.https.ProgressResponseBody;
import com.shawn.fastmail.utils.LogUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UpdateDialog extends BaseDialog {

    @BindView(R.id.tv_title)
    TextView tvTitle;

    @BindView(R.id.tv_desc)
    TextView tvContent;

    @BindView(R.id.btn_update)
    Button btnConfirm;

    @BindView(R.id.btn_cancel)
    Button btnCancel;

    @BindView(R.id.v_line)
    View vLine;

    @BindView(R.id.pb_update)
    ProgressBar mProgressBar;

    @BindView(R.id.tv_icon)
    TextView tvProgress;

    @BindView(R.id.root_view)
    RelativeLayout root_view;

    private String url;

    private int pbUpdateWidth;

    public UpdateDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    public UpdateDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public UpdateDialog(Context context) {
        super(context, R.style.baseAlertDialog);

    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_update;
    }

    @Override
    protected void onCreateDialog() {
        root_view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            private boolean flag = true;

            @Override
            public boolean onPreDraw() {
                if (flag) {
                    pbUpdateWidth = mProgressBar.getMeasuredWidth();
                    flag = false;
                }
                return true;
            }
        });

        this.setCanceledOnTouchOutside(false);
        this.setCancelable(false);
    }

    @OnClick({R.id.btn_cancel, R.id.btn_update})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancel:
                dismiss();
                break;
            case R.id.btn_update:
//                rlProgress.setVisibility(View.VISIBLE);
//                btnConfirm.setVisibility(View.GONE);
                try {
                    downloadProgress();
                    btnConfirm.setEnabled(false);
                    btnCancel.setEnabled(false);
//                    ivClose.setEnabled(false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private void downloadProgress() throws IOException {
        LogUtils.e("======", url);
        //构建一个请求
        Request request = new Request.Builder()
                .url(url)
                .build();
        //构建我们的进度监听器
        final ProgressResponseBody.ProgressListener listener = new ProgressResponseBody.ProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength, boolean done) {

                //计算百分比并更新ProgressBar
                App.handler.post(() -> {
                    final int percent = (int) (100 * bytesRead / contentLength);
                    mProgressBar.setProgress(percent);
                    tvProgress.setText((100 * bytesRead) / contentLength + "%");
                    setProgress((int) ((100 * bytesRead) / contentLength));
                    LogUtils.e("cylog=====", "下载进度：" + (100 * bytesRead) / contentLength + "%");
                });
            }
        };
        //创建一个OkHttpClient，并添加网络拦截器
        HttpsUtils.SSLParams sslSocketFactory = HttpsUtils.getSslSocketFactory(null, null, null);
        OkHttpClient client = new OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory.sSLSocketFactory, sslSocketFactory.trustManager)
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String s, SSLSession sslSession) {
                        return true;
                    }
                })
                .addNetworkInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Response response = chain.proceed(chain.request());
                        //这里将ResponseBody包装成我们的ProgressResponseBody
                        return response.newBuilder()
                                .body(new ProgressResponseBody(response.body(), listener))
                                .build();
                    }
                }).build();
        //发送响应
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //从响应体读取字节流
                final byte[] data = response.body().bytes();      // 1
                //由于当前处于非UI线程，所以切换到UI线程显示图片
                App.handler.post(() -> {
                    Log.e("=======", "success");
//                        mImageView.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
                    checkFilePath();
                    String path = Environment.getExternalStoragePublicDirectory("Download").getAbsolutePath();
                    byte2File(data, path, "udy.apk");
                    Log.e("========", path + "/udy.apk");
                    File targetFile = new File(path + "/udy.apk");
                    if (targetFile.exists()) {//先判断文件是否已存在
                        //1. 创建 Intent 并设置 action
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        //2. 设置 category
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        //添加 flag ,不记得在哪里看到的，说是解决：有些机器上不能成功跳转的问题
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        //3. 设置 data 和 type
                        intent.setDataAndType(Uri.fromFile(targetFile), "application/vnd.android.package-archive");
                        //3. 设置 data 和 type (效果和上面一样)
                        //intent.setDataAndType(Uri.parse("file://" + targetFile.getPath()),"application/vnd.android.package-archive");

                        if (isValidContext(mContext) && isShowing()) {
                            dismiss();
                        }
//                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        //4. 启动 activity
                        startActivity(Intent.createChooser(intent,""));

                        //关闭当前APP
                        android.os.Process.killProcess(android.os.Process.myPid());    //获取PID
                        System.exit(0);   //常规java、c#的标准退出法，返回值为0代表正常退出
                    }
                });
            }
        });
    }

    private void setProgress(int process) {
        int critical = 88;
        if (tvProgress != null) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tvProgress.getLayoutParams();
            if (process < critical) {
                params.leftMargin = (int) (pbUpdateWidth * process / 100);
            } else {
                params.leftMargin = (int) (pbUpdateWidth * (critical - 1) / 100);
            }
            tvProgress.setLayoutParams(params);
        }
    }

    public static void byte2File(byte[] buf, String filePath, String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            File dir = new File(filePath);
            if (!dir.exists() && dir.isDirectory()) {
                dir.mkdirs();
            }
            file = new File(filePath + File.separator + fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(buf);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void checkFilePath() {
        File file = new File(Environment.getExternalStoragePublicDirectory("Download").getAbsolutePath());
        //判断文件夹是否存在，如果不存在就创建，否则不创建
        if (!file.exists()) {
            //通过file的mkdirs()方法创建目录中包含却不存在的文件夹
            file.mkdir();
        }
    }

    private boolean isValidContext(Context c) {

        Activity a = (Activity) c;

        if (a.isDestroyed() || a.isFinishing()) {
            return false;
        } else {
            return true;
        }
    }

    public void setData(String title, String content, boolean isForce, String url) {
        tvTitle.setText(title);
        tvContent.setText(content);
        btnCancel.setVisibility(!isForce ? View.VISIBLE : View.GONE);
        vLine.setVisibility(!isForce ? View.VISIBLE : View.GONE);
        this.url = url;
    }

    public static class Builder {
        private UpdateDialog dialog;

        public Builder(Context context) {
            this.dialog = new UpdateDialog(context);
        }

        public UpdateDialog builder() {
            return dialog;
        }

        public Builder data(String title, String content, boolean isForce, String url, String version) {
            dialog.setData(title, content, isForce, url);
            return this;
        }
    }
}
