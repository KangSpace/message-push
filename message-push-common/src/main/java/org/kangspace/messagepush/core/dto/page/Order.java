package org.kangspace.messagepush.core.dto.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 排序字段
 *
 * @author kango2gler@gmail.com
 * @since 2021-04-23
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    /**
     * 字段名
     */
    private String field;

    /**
     * 排序方式，ApiConst.ASC或ApiConst.DESC
     */
    private Integer sequence;

}
