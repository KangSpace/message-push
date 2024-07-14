package org.kangspace.messagepush.core.elasticsearch.domain;

import lombok.extern.slf4j.Slf4j;
import org.kangspace.messagepush.core.elasticsearch.ElasticSearchConst;

import java.util.Objects;

/**
 * 重要基本词性以及对用的权重
 *
 * @author kango2gler@gmail.com
 */
@Slf4j
public enum ImportantPosEnum {


    /**
     * 版本
     */
    POS_C_VS(137_438_953_472L, 1.1f),

    /**
     * 文件格式
     */
    POS_C_EXT(274_877_906_944L, 1.1f);

    /**
     * 词性编号
     */
    private final Long posCode;
    /**
     * 权重
     */
    private final Float weight;

    ImportantPosEnum(Long posCode, Float weight) {
        this.posCode = posCode;
        this.weight = weight;
    }

    public static ImportantPosEnum resolve(Long posCode) {
        ImportantPosEnum response = null;
        for (ImportantPosEnum importantPosEnum : ImportantPosEnum.values()) {
            if (!Objects.equals(importantPosEnum.getPosCode() & posCode, ElasticSearchConst.ZERO)) {
                log.debug("命中的重要基本词性：{}", importantPosEnum);
                if (Objects.isNull(response) || importantPosEnum.weight > response.weight) {
                    response = importantPosEnum;
                }
            }
        }
        return response;
    }

    public static Boolean judgeIsImportant(Long posCode) {
        if (posCode == null) {
            return false;
        }
        Long judge = 1L;
        for (ImportantPosEnum importantPosEnum : ImportantPosEnum.values()) {
            judge |= importantPosEnum.getPosCode();
        }
        return !Objects.equals(judge & posCode, ElasticSearchConst.ZERO);
    }

    public Long getPosCode() {
        return posCode;
    }

    public Float getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return "ImportantPosEnum{" +
                "posCode=" + posCode +
                ", weight=" + weight +
                '}';
    }

}
