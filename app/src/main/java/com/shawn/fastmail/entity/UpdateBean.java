package com.shawn.fastmail.entity;

import com.shawn.fastmail.base.BaseBean;

public class UpdateBean extends BaseBean {

    public int isNeedUpdate;            //是否需要更新    0、不需要    1、需要
    public String url;                  //更新包下载地址
    public int isForce;                 //是否强制更新   0、不强制   1、强制
    public String NewVersion;              //当前最新版本号
    public String title;                //更新弹窗标题（可为空）
    public String content;              //更新弹窗内容（可为空）
}
