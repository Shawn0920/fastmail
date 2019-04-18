package com.shawn.fastmail.entity;

import com.shawn.fastmail.base.BaseBean;

/**
 * 描述：
 *
 * @author shawn
 * @date 2019/2/22
 */
public class PacketDetailBean extends BaseBean {
    public String gid;
    public String serialNumber;
    public String receiverPhone;
    public String receiverName;
    public String expressName;
    public String expressOrderId;
    public int goodsType;
    public String comment;
    public Long inTime;
    public Long outTime;
    public int deliverStatus;  // 0:待出库，1:已出库
    public boolean isCheck;
}
