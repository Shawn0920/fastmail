package com.shawn.fastmail.entity;

import com.shawn.fastmail.base.BaseBean;

import java.io.Serializable;

/**
 * 描述：
 *
 * @author shawn
 * @date 2019/2/21
 */
public class ExpressListBean extends BaseBean implements Serializable {
    public String expressGid;
    public String expressName;
    public int hotFlag;    //0:非热门，1:热门

    private String sortLetters;  //显示数据拼音的首字母


    public String getSortLetters() {
        return sortLetters;
    }
    public void setSortLetters(String sortLetters) {
        this.sortLetters = sortLetters;
    }
}
