package com.shawn.fastmail.entity;

import com.shawn.fastmail.base.BaseBean;

import java.util.List;

/**
 * 描述：
 *
 * @author shawn
 * @date 2019/2/22
 */
public class SearchBean extends BaseBean {
    public int total;
    public List<PacketDetailBean> rows;
}
