package org.kangspace.messagepush.core.elasticsearch.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;

/**
 * 滚动索引操作请求类
 *
 * @author kango2gler@gmail.com
 * @since 2021/11/12
 */
@Data
@NoArgsConstructor
public class PlainRolloverRequest extends BaseRequest {
    /**
     * 是否实际执行rollover
     * true: 不执行,只检查是否需要翻转
     * false: 执行翻转
     */
    private Boolean dryRun;
    /**
     * 需要翻转的别名
     */
    private String alias;
    /**
     * 翻转时的新索引名
     */
    private String newIndexName;
    /**
     * 翻转条件,最大有效期
     */
    private TimeValue maxAge;
    /**
     * 翻转条件,最大文档数
     */
    private Long maxDocs;
    /**
     * 翻转跳转,最大索引大小
     */
    private ByteSizeValue maxSize;

}
