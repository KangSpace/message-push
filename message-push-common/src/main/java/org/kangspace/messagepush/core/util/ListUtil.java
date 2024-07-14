package org.kangspace.messagepush.core.util;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 列表工具类
 *
 * @author kango2gler@gmail.com
 * @since 2021-04-29
 */
@Slf4j
public class ListUtil {

    /**
     * 列表是否为空
     *
     * @param list
     * @return
     */
    public static boolean isEmpty(List list) {
        return list == null || list.isEmpty();
    }

    /**
     * 列表是否不为空
     *
     * @param list
     * @return
     */
    public static boolean isNotEmpty(List list) {
        return !isEmpty(list);
    }

    /**
     * 对象数组转对象列表
     *
     * @param objs
     * @param <T>
     * @return
     */
    public static <T> List<T> getList(T... objs) {
        if (objs == null || objs.length == 0) {
            return new ArrayList<T>();
        }
        return new ArrayList<T>(Arrays.asList(objs));
    }

    /**
     * 逗号分割的字符串转字符串列表
     *
     * @param str 逗号分割的字符串
     * @return 字符串列表
     */
    public static List<String> strToList(String str) {
        if (StrUtil.isEmpty(str)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(str.split(",")));
    }

    /**
     * 数组转列表
     *
     * @param array
     * @param <T>
     * @return
     */
    public static <T> List<T> arrayToList(T[] array) {
        if (array == null) {
            return null;
        }
        return new ArrayList<>(Arrays.asList(array));
    }

    /**
     * 列表转数组
     *
     * @param list
     * @param <T>
     * @return
     */
    public static <T> T[] listToArray(List<T> list, Class classType) {
        if (list == null) {
            return null;
        }
        return list.toArray((T[]) Array.newInstance(classType, list.size()));
    }

    /**
     * 拆分对象列表
     *
     * @param list
     * @param size
     * @return
     */
    public static <T> List<List<T>> splitList(List<T> list, int size) {
        List<List<T>> res = new ArrayList<>();
        List<T> temp = new ArrayList<>();
        res.add(temp);
        int index = 1;
        for (T map : list) {
            if (index > size) {
                temp = new ArrayList<>();
                res.add(temp);
                index = 1;
            }
            temp.add(map);
            index++;
        }
        return res;
    }

    /**
     * 拷贝列表（仅支持浅拷贝）效果同BeanUtils.copyProperties
     *
     * @param sourceList  源列表
     * @param targetClass 目标列表项Class
     * @param <T>         目标列表项泛型
     * @return 返回目标列表
     */
    public static <T> List<T> copyList(List sourceList, Class<T> targetClass) {
        List<T> result = new ArrayList<>();
        if (ListUtil.isNotEmpty(sourceList)) {
            for (Object item : sourceList) {
                T data = null;
                try {
                    data = targetClass.newInstance();
                } catch (InstantiationException e) {
                    log.warn("拷贝列表异常-不可实例化异常，目标类：[" + targetClass.getName() + "]");
                    return result;
                } catch (IllegalAccessException e) {
                    log.warn("拷贝列表异常-反射异常，目标类：[" + targetClass.getName() + "]");
                    return result;
                }
                BeanUtils.copyProperties(item, data);
                result.add(data);
            }
        }
        return result;
    }
}
