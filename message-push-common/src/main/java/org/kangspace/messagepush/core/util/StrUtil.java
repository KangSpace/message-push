package org.kangspace.messagepush.core.util;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 字符串工具类
 *
 * @author kango2gler@gmail.com
 * @since 2021-04-29
 */
public class StrUtil {

    /**
     * SimpleDateFormat是线程不安全的，此处用ThreadLocal来设置线程的SimpleDateFormat对象
     */
    private static final ThreadLocal<SimpleDateFormat> SIMPLE_DATE_FORMAT_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 是否为空
     *
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * 是否不为空
     *
     * @param str
     * @return
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 是否为空
     *
     * @param str
     * @return
     */
    public static boolean isBlank(String str) {
        int strLen;
        if (str != null && (strLen = str.length()) != 0) {
            for (int i = 0; i < strLen; ++i) {
                // 判断字符是否为空格、制表符、tab
                if (!Character.isWhitespace(str.charAt(i))) {
                    return false;
                }
            }
            return true;
        } else {
            return true;
        }
    }

    /**
     * 是否不为空
     *
     * @param str
     * @return
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * @param str
     * @param defaultValue 默认值
     * @return
     * @description 字符串转int
     * @date 2014-1-3
     */
    public static int strToInt(String str, int defaultValue) {
        if (isBlank(str)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(str);
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    /**
     * @param str
     * @param defaultValue 默认值
     * @return
     * @description 字符串转long
     */
    public static long strToLong(String str, long defaultValue) {
        if (isBlank(str)) {
            return defaultValue;
        }
        try {
            return Long.parseLong(str);
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    /**
     * @param str
     * @param defaultValue 默认值
     * @return
     * @description 字符串转double
     */
    public static double strToDouble(String str, Double defaultValue) {
        if (isBlank(str)) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(str);
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    /**
     * @param str
     * @param defaultValue 默认值
     * @return
     * @description 字符串转long
     */
    public static BigDecimal strToBigDecimal(String str, BigDecimal defaultValue) {
        if (isBlank(str)) {
            return defaultValue;
        }
        try {
            return new BigDecimal(str);
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    /**
     * @param str
     * @return
     * @description 字符串转日期，格式：yyyy-MM-dd HH:mm:ss
     * @date 2014-1-3
     */
    public static Date strToDate(String str) {
        if (isBlank(str)) {
            return null;
        }
        try {
            return getSimpleDateFormat().parse(str);
        } catch (Exception e) {
            return null;
        }

    }

    /**
     * 日期转字符串，格式：yyyy-MM-dd HH:mm:ss
     *
     * @param date
     * @return
     */
    public static String dateToStr(Date date) {
        if (date == null) {
            return null;
        }
        return getSimpleDateFormat().format(date);
    }

    /**
     * 列表转字符串，半角逗号分割
     *
     * @param list
     * @return
     */
    public static String listToStr(List<String> list) {
        return listToStr(list, ",", "", "");
    }

    /**
     * 列表转字符串，自定义分割方式
     *
     * @param list
     * @param separator 分隔符
     * @param prefix    附加前缀
     * @param suffix    附加后缀
     * @return
     */
    public static String listToStr(List<String> list, String separator, String prefix, String suffix) {
        if (list == null || list.size() == 0) {
            return "";
        }
        if (separator == null) {
            separator = "";
        }
        if (prefix == null) {
            prefix = "";
        }
        if (suffix == null) {
            suffix = "";
        }
        StringBuffer s = new StringBuffer("");
        for (int i = 0, len = list.size(); i < len; i++) {
            if (isNotEmpty(list.get(i))) {
                s.append(prefix + list.get(i) + suffix + separator);
            }
        }
        if (s.length() > 0) {
            s.delete(s.length() - separator.length(), s.length());
        }
        return s.toString();
    }

    /**
     * @param array
     * @return
     * @description 数组转字符串
     * @date 2012-12-5
     */
    public static String arrayToStr(String[] array) {
        if (array == null || array.length == 0) {
            return "";
        }
        StringBuffer s = new StringBuffer("");
        for (int i = 0, len = array.length; i < len; i++) {
            if (isNotBlank(array[i])) {
                s.append(array[i] + ",");
            }
        }
        if (s.length() > 0) {
            s.delete(s.length() - 1, s.length());
        }
        return s.toString();
    }


    public static List<String> strToList(String str) {
        return strToList(str, ",");
    }

    public static List<String> strToList(String str, String separator) {
        ArrayList<String> list = new ArrayList<String>();
        if (isBlank(str)) {
            return list;
        }
        return new ArrayList<String>(Arrays.asList(str.split(separator)));
    }

    /**
     * @param strs
     * @return
     * @description 字符串数组转字符串列表
     * @date Feb 21, 2012
     */
    public static List<String> getList(String... strs) {
        if (strs == null || strs.length == 0) {
            return new ArrayList<String>();
        }
        return new ArrayList<String>(Arrays.asList(strs));
    }


    /**
     * SimpleDateFormat是线程不安全的，此处用ThreadLocal来设置线程的SimpleDateFormat对象
     *
     * @return
     */
    private static synchronized SimpleDateFormat getSimpleDateFormat() {
        if (SIMPLE_DATE_FORMAT_THREAD_LOCAL.get() == null) {
            SIMPLE_DATE_FORMAT_THREAD_LOCAL.set(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        }
        return SIMPLE_DATE_FORMAT_THREAD_LOCAL.get();
    }

    /**
     * 打印
     *
     * @param o
     */
    public static void p(Object o) {
        System.out.println(o);
    }


}
