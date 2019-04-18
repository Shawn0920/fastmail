package com.shawn.fastmail.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * 描述：
 *
 * @author shawn
 * @date 2019/2/21
 */
public class DateStrUtil {

    //每天的毫秒数
    private static long day = 60 * 60 * 24;
    //日期字符串的格式
    private static DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 返回前一天的 00:00:00 字符串格式
     *
     * @return
     */
    public static String getLastDayStart(Long current) {
        return format.format(getTodayStartMills(current) - day);
    }

    /**
     * 返回前一天的 23:59:59 字符串格式
     *
     * @return
     */
    public static String getLastDayEnd(Long current) {
        return format.format(getTodayStartMills(current) - 1);
    }

    /**
     * 返回当日的 00:00:00 字符串格式
     *
     * @return
     */
    public static String getTodayStart(Long current) {
        long zero = getTodayStartMills(current);
        return format.format(zero);
    }

    /**
     * 返回当日的 23:59:59 字符串格式
     *
     * @return
     */
    public static String getTodayEnd(Long current) {
        return format.format(getTodayStartMills(current) + day - 1);
    }

    /**
     * 返回当日的 00:00:00 毫秒格式
     *
     * @return
     */
    public static long getTodayStartMills(Long current) {
//        long current = System.currentTimeMillis();
        long zero = ((current + TimeZone.getDefault().getRawOffset()) / day * day) - TimeZone.getDefault().getRawOffset();
        return zero;
    }

    /**
     * 返回当日的 23:59:59 字符串格式
     *
     * @return
     */
    public static long getTodayEndMills(Long current) {
        return getTodayStartMills(current) + day - 1;
    }

    /**
     * 返回前一天的 00:00:00 毫秒格式
     *
     * @return
     */
    public static long getLastDayStartMills(Long current) {
        return getTodayStartMills(current) - day;
    }

    public static String formatDate(Long current) {
        return format.format(current * 1000);
    }

    public static long duringDay(Long startTime, Long endTime) {
        return (endTime - startTime) / day;
    }

    public static void main(String[] args) {
        long in = 1550746082;
        long out = 1550837041;

        System.out.println((out - in) / day);
        System.out.println(formatDate(in));
        System.out.println(formatDate(out));
    }

}
