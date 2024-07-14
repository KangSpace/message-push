package org.kangspace.messagepush.core.elasticsearch.domain;

import lombok.extern.slf4j.Slf4j;
import org.kangspace.messagepush.core.elasticsearch.ElasticSearchConst;

import java.util.Objects;

/**
 * 重要业务词性以及对用的权重
 *
 * @author kango2gler@gmail.com
 */
@Slf4j
public enum ImportantBizPosEnum {

    SW_KNOWLEDGE(131_072L, 1.4f),

    HX_KNOWLEDGE(65_536L, 1.4f),

    WL_KNOWLEDGE(32_768L, 1.4f),

    DL_KNOWLEDGE(16_384L, 1.4f),

    LS_KNOWLEDGE(8192L, 1.4f),

    ZZ_KNOWLEDGE(4096L, 1.4f),

    YY_KNOWLEDGE(2048L, 1.4f),

    SXKnowledge(1024L, 1.4f),

    YW_KNOWLEDGE(512L, 1.4f);

    /**
     * 词性权重
     */
    private final Long posBizCode;
    /**
     * 权重
     */
    private final Float weight;

    ImportantBizPosEnum(Long posBizCode, Float weight) {
        this.posBizCode = posBizCode;
        this.weight = weight;
    }

    public static ImportantBizPosEnum resolve(Long posBizCode) {
        ImportantBizPosEnum response = null;
        for (ImportantBizPosEnum importantBizPosEnum : ImportantBizPosEnum.values()) {
            if (!Objects.equals(importantBizPosEnum.getPosBizCode() & posBizCode, ElasticSearchConst.ZERO)) {
                log.debug("重要的业务词性：{}", importantBizPosEnum);
                if (Objects.isNull(response) || importantBizPosEnum.weight > response.weight) {
                    response = importantBizPosEnum;
                }
            }
        }
        return response;
    }

    public static Boolean judgeIsImportant(Long posBizCode) {
        if (posBizCode == null) {
            return false;
        }
        Long judge = 1L;
        for (ImportantBizPosEnum importantBizPosEnum : ImportantBizPosEnum.values()) {
            judge |= importantBizPosEnum.getPosBizCode();
        }
        return !Objects.equals(judge & posBizCode, ElasticSearchConst.ZERO);
    }

    public Long getPosBizCode() {
        return posBizCode;
    }

    public Float getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return "ImportantBizPosEnum{" +
                "posBizCode=" + posBizCode +
                ", weight=" + weight +
                '}';
    }

}
