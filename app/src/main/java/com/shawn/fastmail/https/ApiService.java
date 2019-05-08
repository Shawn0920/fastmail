package com.shawn.fastmail.https;

import com.shawn.fastmail.base.BaseBean;
import com.shawn.fastmail.entity.CurrentTimeBean;
import com.shawn.fastmail.entity.ExpressListBean;
import com.shawn.fastmail.entity.HomeBean;
import com.shawn.fastmail.entity.InBoundBean;
import com.shawn.fastmail.entity.InboundRequestBean;
import com.shawn.fastmail.entity.LoginRequestBean;
import com.shawn.fastmail.entity.OutboundRequestBean;
import com.shawn.fastmail.entity.PacketDetailBean;
import com.shawn.fastmail.entity.SearchBean;
import com.shawn.fastmail.entity.SearchRequestBean;
import com.shawn.fastmail.entity.UpdateBean;
import com.shawn.fastmail.entity.UpdateRequestBean;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * 描述：
 *
 * @author shawn
 * @date 2018/4/28
 */
public interface ApiService {

    /**
     * 获取服务器当前时间
     * @return
     */
    @GET("system/info")
    Call<BaseBean<CurrentTimeBean>> getCurrentTime();

    /**
     * 首页统计数据
     * @param startTime
     * @param endTime
     * @param token
     * @return
     */
    @POST("merchant/package/statistics")
    @FormUrlEncoded
    Call<BaseBean<HomeBean>> getHomeData(@Field("startTime") long startTime, @Field("endTime") long endTime, @Field("t") String token);

    /**
     * 获取快递列表
     * @return
     */
    @GET("merchant/express/list")
    Call<BaseBean<List<ExpressListBean>>> getExpressData();

    /**
     * 入库
     * @param token
     * @param requestBean
     * @return
     */
    @POST("merchant/package/in")
    @Headers({"Content-Type: application/json"})
    Call<BaseBean<InBoundBean>> inBound(@Query("t") String token, @Body InboundRequestBean requestBean);

    /**
     * 搜索
     * @param token
     * @param requestBean  包括运单号，手机号，编码，姓名，时间
     * @return
     */
    @POST("merchant/package/list")
    @Headers({"Content-Type: application/json"})
    Call<BaseBean<SearchBean>> searchData(@Query("t") String token, @Body SearchRequestBean requestBean);

    /**
     * 快件详情
     * @param gid
     * @return
     */
    @POST("merchant/package/detail")
    @FormUrlEncoded
    Call<BaseBean<PacketDetailBean>> getPackageDetails(@Field("gid") String gid);

    /**
     * 出库快件信息
     * @param token
     * @param requestBean
     * @return
     */
    @POST("merchant/package/scan")
    @Headers({"Content-Type: application/json"})
    Call<BaseBean<List<PacketDetailBean>>> getOutboundInfo(@Query("t") String token, @Body RequestBody requestBean);

    /**
     * 出库
     * @param token
     * @param requestBean
     * @return
     */
    @POST("merchant/package/out")
    @Headers({"Content-Type: application/json"})
    Call<BaseBean> outbound(@Query("t") String token, @Body OutboundRequestBean requestBean);


    @POST("merchant/user/login")
    @Headers({"Content-Type: application/json"})
    Call<BaseBean> login( @Body LoginRequestBean requestBean);

    /**
     * APP检查更新接口
     * @param requestBean
     * @return
     */
    @POST("version/merchant/getversionupdate")
    @Headers({"Content-Type: application/json"})
    Call<BaseBean<UpdateBean>> checkUpdateForFastMail(@Body UpdateRequestBean requestBean);

    /**
     * APP检查更新接口
     * @param requestBean
     * @return
     */
    @POST("/version/express/getversionupdate")
    @Headers({"Content-Type: application/json"})
    Call<BaseBean<UpdateBean>> checkUpdateForCourier(@Body UpdateRequestBean requestBean);
}

