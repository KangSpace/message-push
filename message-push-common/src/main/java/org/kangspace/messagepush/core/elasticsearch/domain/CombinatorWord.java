package org.kangspace.messagepush.core.elasticsearch.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author kango2gler@gmail.com
 * @description 逆向组合同义词，暂未用到
 */
@Setter
@Getter
public class CombinatorWord {

    /**
     * 逆向组合同义词
     */
    private Word word;

    /**
     * 成员的索引集合
     */
    @JsonProperty(value = "Positions")
    private List<Integer> positionsList;

}
