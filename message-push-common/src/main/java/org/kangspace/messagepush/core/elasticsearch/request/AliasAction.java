package org.kangspace.messagepush.core.elasticsearch.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 别名操作对象
 *
 * @author kango2gler@gmail.com
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AliasAction {

    /**
     * 操作方式 remove 或 add
     */
    private String action;
    /**
     * 索引名
     */
    private String index;
    /**
     * 别名
     */
    private String alias;
    /**
     * 是否写索引
     * is_write_index
     */
    @JsonProperty("is_write_index")
    private Boolean writeIndex;

}
