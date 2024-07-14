package org.kangspace.messagepush.core.elasticsearch.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * @author kango2gler@gmail.com
 * @description 分词结果DTO
 */
@Data
public class Segmentation {

    /**
     * 消耗时间
     */
    private Integer elapsed;

    /**
     * 是否成功
     */
    private Boolean isValid;

    /**
     * 文本信息
     */
    private String message;

    /**
     * 原始文本
     */
    private String originalWord;

    /**
     * 分词文本
     */
    private String segmentWord;

    /**
     * 分词集合
     */
    @JsonProperty(value = "synonymWords")
    private List<SynonymWord> synonymWordList;

    /**
     * 组合词集合
     */
    @JsonProperty(value = "combinatorWords")
    private List<CombinatorWord> combinatorWordList;

}
