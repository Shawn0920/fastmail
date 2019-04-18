package com.shawn.fastmail.entity;

/**
 * 描述：
 *
 * @author shawn
 * @date 2019/2/21
 */
public class InboundRequestBean {
    public String expressGid;
    public int goodsType;
    public String expressOrderGid;
    public String receiverName;
    public String receiverPhone;
    public String comment;

    public InboundRequestBean(String expressGid, int goodsType, String expressOrderGid, String receiverName, String receiverPhone, String comment) {
        this.expressGid = expressGid;
        this.goodsType = goodsType;
        this.expressOrderGid = expressOrderGid;
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.comment = comment;
    }
}
