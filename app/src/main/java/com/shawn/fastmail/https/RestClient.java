package com.shawn.fastmail.https;

import com.shawn.fastmail.App;
import com.shawn.fastmail.config.Constants;
import com.shawn.fastmail.R;
import com.shawn.fastmail.utils.LogUtils;
import com.shawn.fastmail.utils.NetUtil;
import com.shawn.fastmail.utils.SpUtil;
import com.shawn.fastmail.utils.ToastUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.Buffer;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.alibaba.fastjson.util.IOUtils.UTF8;

/**
 * 描述：
 *
 * @author shawn
 * @date 2019/2/20
 */
public class RestClient {

    /**
     * 网络访问构造对象
     */
    private static RestClient mInstance = new RestClient();
    /**
     * 接口实例
     */
    private ApiService api;


    /**
     * 配置 网络访问
     */
    private RestClient() {
        boolean available = NetUtil.isNetworkAvailable(App.getContext());
        if (!available) {
            ToastUtils.show(R.string.view_no_network);
        }
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                LogUtils.i("=======>", message);
            }
        });
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        HttpsUtils.SSLParams sslSocketFactory = HttpsUtils.getSslSocketFactory(null, null, null);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(NetConfig.CONNECTION_TIME, TimeUnit.MILLISECONDS)//网络连接时间
                .readTimeout(NetConfig.READ_TIME_OUT, TimeUnit.MILLISECONDS)//网络读取时间
                .addInterceptor(logging)//添加Log拦截器
//                .addInterceptor(new Interceptor() {
//                    @Override
//                    public Response intercept(Chain chain) throws IOException {
//                        RequestBody requestBody = chain.request().body();
//                        Buffer buffer = new Buffer();
//                        requestBody.writeTo(buffer);
//                        Charset charset = Charset.forName("UTF-8");
//                        MediaType contentType = requestBody.contentType();
//                        if (contentType != null) {
//                            charset = contentType.charset(UTF8);
//                        }
//                        return chain.proceed(chain.request());
//                    }
//                })
                .sslSocketFactory(sslSocketFactory.sSLSocketFactory, sslSocketFactory.trustManager)
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String s, SSLSession sslSession) {
                        return true;
                    }
                })
                .retryOnConnectionFailure(true);//失败不重发


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)//基类url
                .client(builder.build())//网络访问库
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(ApiService.class);

    }

    /**
     * 获取网络访问接口
     *
     * @return 接口实例
     */
    public static ApiService api() {
        return mInstance.api;
    }
}

