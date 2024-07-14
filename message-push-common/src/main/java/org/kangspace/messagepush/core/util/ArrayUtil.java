package org.kangspace.messagepush.core.util;

import org.apache.commons.lang3.ArrayUtils;

/**
 * 数组工具类
 *
 * @author kango2gler@gmail.com
 * @since 2022/5/13
 */
public class ArrayUtil {

    /**
     * 基本类型数组转换为包装类型数据
     *
     * @param arr 数组
     * @return 包装类型数据
     */
    public static <T> T[] toBoxed(Object arr) {
        T[] result;
        if (arr.getClass().isArray()) {
            switch (arr.getClass().getComponentType().getName()) {
                case "boolean":
                    result = (T[]) ArrayUtils.toObject((boolean[]) arr);
                    break;
                case "byte":
                    result = (T[]) ArrayUtils.toObject((byte[]) arr);
                    break;
                case "char":
                    result = (T[]) ArrayUtils.toObject((char[]) arr);
                    break;
                case "int":
                    result = (T[]) ArrayUtils.toObject((int[]) arr);
                    break;
                case "short":
                    result = (T[]) ArrayUtils.toObject((short[]) arr);
                    break;
                case "float":
                    result = (T[]) ArrayUtils.toObject((float[]) arr);
                    break;
                case "double":
                    result = (T[]) ArrayUtils.toObject((double[]) arr);
                    break;
                case "long":
                    result = (T[]) ArrayUtils.toObject((long[]) arr);
                    break;
                default:
                    result = (T[]) arr;
            }
            return result;
        }
        return null;
    }


    public static void main(String[] args) {
        System.out.println(toBoxed(new int[]{1, 2, 3}));
        System.out.println(toBoxed(new Integer[]{1, 2, 3}));
    }
}
