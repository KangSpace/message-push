package org.kangspace.messagepush.core.elasticsearch.config;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 高斯函数变量
 *
 * @author kango2gler@gmail.com
 */
@Setter
@Getter
public class GaussConfig implements Serializable {

    /**
     * 属性名
     */
    private String fieldName;
    /**
     * 起始值
     */
    private String origin;
    /**
     * 补偿系数
     */
    private String offset;
    /**
     * 级别因子
     */
    private String scale;
    /**
     * 衰减系数
     */
    private Double decay;
    /**
     * 权重
     */
    private Float weight;
}
