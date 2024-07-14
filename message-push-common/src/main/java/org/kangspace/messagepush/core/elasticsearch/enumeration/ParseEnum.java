package org.kangspace.messagepush.core.elasticsearch.enumeration;

/**
 * 通用拼装枚举
 *
 * @author kango2gler@gmail.com
 */
public enum ParseEnum {

    /**
     * 关键词完全匹配
     */
    TERM(0, "关键词完全匹配"),

    /**
     * 关键词完全匹配，且值大于0
     */
    TERM_GT_ZERO(1, "关键词完全匹配，且值大于0"),

    /**
     * 关键词完全匹配，且值大于等于0
     */
    TERM_GTE_ZERO(2, "关键词完全匹配，且值大于等于0"),

    /**
     * 关键词完全匹配，多项或运算，支持List及String
     */
    TERMS(3, "关键词完全匹配，多项或运算，支持List及String"),

    /**
     * 关键词完全匹配，多项或运算，且值大于0
     */
    TERMS_GT_ZERO(4, "关键词完全匹配，多项或运算，且值大于0"),

    /**
     * 关键词完全匹配，多项或运算，且值大于0
     */
    TERMS_GTE_ZERO(5, "关键词完全匹配，多项或运算，且值大于0"),

    /**
     * 关键词完全匹配，多项与运算，支持List及String
     */
    TERMS_AND(3, "关键词完全匹配，多项与运算，支持List及String"),

    /**
     * 分词匹配
     */
    MATCH(10, "分词匹配"),
    /**
     * <pre>
     * 分词匹配-短语匹配
     * 与MATCH类似,区别:match_phrase的分词结果必须在text字段分词中都包含，而且分词顺序必须相同，必须都是连续的
     * </pre>
     */
    MATCH_PHRASE(11, "分词匹配-短语匹配"),
    /**
     * <pre>
     * 分词匹配-短语匹配-对最后一个分词进行通配符匹配
     * 与MATCH_PHRASE类似,区别:MATCH_PHRASE_PREFIX会对最后一个分词进行通配符匹配
     *
     * </pre>
     */
    MATCH_PHRASE_PREFIX(12, "分词匹配-短语匹配-对最后一个分词进行通配符匹配"),

    /**
     * 前缀查询，类似于sql中的like
     */
    PREFIX(13, "前缀查询"),

    /**
     * 范围查询，大于某个值
     */
    RANGE_GT(20, "范围查询，大于某个值"),

    /**
     * 范围查询，大于等于某个值
     */
    RANGE_GTE(21, "范围查询，大于等于某个值"),

    /**
     * 范围查询，小于某个值
     */
    RANGE_LT(22, "范围查询，小于某个值"),

    /**
     * 范围查询，小于等于某个值
     */
    RANGE_LTE(23, "范围查询，小于等于某个值"),

    /**
     * 多项与查询
     */
    MUST_TERM_FROM_STRING(30, "多项与查询"),

    /**
     * 多项或查询
     */
    SHOULD_TERM_FROM_LIST(40, "多项或查询"),

    /**
     * 半角逗号分割的字符串转多项或查询
     */
    SHOULD_TERM_FROM_STRING(41, "半角逗号分割的字符串转多项或查询"),

    /**
     * 全文检索
     */
    FULL_TEXT(90, "全文检索");

    /**
     * 索引
     */
    private final int index;
    /**
     * 描述
     */
    private final String description;


    ParseEnum(int index, String description) {
        this.index = index;
        this.description = description;
    }


    public static String getDescription(int index) {
        for (ParseEnum p : ParseEnum.values()) {
            if (p.index == index) {
                return p.description;
            }
        }
        return null;
    }


}