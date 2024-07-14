package org.kangspace.messagepush.core.elasticsearch.domain;

import lombok.Data;

import java.util.List;

/**
 * @author kango2gler@gmail.com
 * @description 分词集合
 */
@Data
public class SynonymWord {

    /**
     * 同义词集合，暂未用到
     */
    private List<List<String>> synonymList;

    /**
     * 分出来的词
     */
    private Word word;

}