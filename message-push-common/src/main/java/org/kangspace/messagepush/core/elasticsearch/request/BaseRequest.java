package org.kangspace.messagepush.core.elasticsearch.request;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.kangspace.messagepush.core.util.JsonUtil;

/**
 * Es请求基础实体
 *
 * @author kango2gler@gmail.com
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@RequiredArgsConstructor
public class BaseRequest {

    /**
     * 索引名，如果是多个索引半角逗号分割
     */
    @NonNull
    private String index;

    /**
     * 根节点
     */
    private ObjectNode rootNode = JsonUtil.createObjectNode();

}
