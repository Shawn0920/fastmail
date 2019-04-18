package com.shawn.fastmail.entity;

/**
 * 描述：
 *
 * @author shawn
 * @date 2019/2/22
 */
public class SearchRequestBean {
    public int pageNumber;
    public int pageSize = 10;
    public int orderField;    // 排序字段 1:入库时间，2:出库时间
    public boolean orderDesc;  // true:倒序，false:顺序

    public  SearchRequestBean(int pageNumber, int orderField, boolean orderDesc) {
        this.pageNumber = pageNumber;
        this.orderField = orderField;
        this.orderDesc = orderDesc;
    }

    public String merchantGid;

    public String expressOrderGid;

    public String receiverName;

    public String receiverPhone;

    public String serialNumber;

    public Integer deliverStatus; // 0:待出库，1:已出库

    public Long inStartTime; // 入库开始时间

    public Long inEndTime; // 入库结束时间

    public Long outStartTime; // 出库开始时间

    public Long outEndTime; // 出库结束时间

    public String queryGroup;  //全字段查询
}
