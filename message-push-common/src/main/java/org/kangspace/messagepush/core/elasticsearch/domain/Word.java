package org.kangspace.messagepush.core.elasticsearch.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author kango2gler@gmail.com
 **/
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Word implements Serializable {

    /**
     * 业务词性
     */
    @JsonProperty(value = "businessPos")
    private Long businessPos;
    /**
     * 词性
     */
    private Long pos;
    /**
     * 分出来的词
     */
    private String word;

    public Word(String word) {
        this.word = word;
    }


}