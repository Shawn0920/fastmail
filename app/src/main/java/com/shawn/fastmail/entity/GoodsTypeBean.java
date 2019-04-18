package com.shawn.fastmail.entity;

/**
 * 描述：
 *
 * @author shawn
 * @date 2019/2/21
 */
public class GoodsTypeBean {
    public int goodsTypeId;
    public String goodsTypeName;

    public GoodsTypeBean() {

    }

    public GoodsTypeBean(int id, String name) {
        this.goodsTypeId = id;
        this.goodsTypeName = name;
    }

    @Override
    public String toString() {
        return goodsTypeName;
    }
}
