package org.kangspace.messagepush.core.elasticsearch.enumeration;

import org.kangspace.messagepush.core.elasticsearch.ElasticSearchConst;

/**
 * 查询条件拼装的位置，是在must、filter、should中
 *
 * @author kango2gler@gmail.com
 */
public enum OccurEnum {

    /**
     * must节点枚举
     */
    MUST(ElasticSearchConst.MUST, "与，算分"),

    /**
     * must_not节点枚举
     */
    MUST_NOT(ElasticSearchConst.MUST_NOT, "非，算分"),
    /**
     * filter节点枚举
     */
    FILTER(ElasticSearchConst.FILTER, "与，不算分"),

    /**
     * should节点枚举
     */
    SHOULD(ElasticSearchConst.SHOULD, "或，算分");

    /**
     * 类型值
     */
    private final String occur;
    /**
     * 描述
     */
    private final String description;


    OccurEnum(String occur, String description) {
        this.occur = occur;
        this.description = description;
    }


    public static String getDescription(String occur) {
        for (OccurEnum p : OccurEnum.values()) {
            if (p.occur.equals(occur)) {
                return p.description;
            }
        }
        return null;
    }

}