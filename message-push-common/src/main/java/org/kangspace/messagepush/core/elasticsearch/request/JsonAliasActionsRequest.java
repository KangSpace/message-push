package org.kangspace.messagepush.core.elasticsearch.request;


import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import org.kangspace.messagepush.core.elasticsearch.ElasticSearchConst;
import org.kangspace.messagepush.core.util.JsonUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * es别名操作请求对象，用于创建索引时使用
 *
 * @author kango2gler@gmail.com
 * @since 2019/10/29
 */
@Data
public class JsonAliasActionsRequest {

    /**
     * 别名操作列表
     */
    private List<AliasAction> aliasActions;

    /**
     * 根节点
     */
    private ObjectNode rootNode = JsonUtil.createObjectNode();

    public JsonAliasActionsRequest() {
        aliasActions = new ArrayList<>();
    }

    public JsonAliasActionsRequest(AliasAction aliasAction) {
        this();
        addAliasAction(aliasAction);
    }

    public JsonAliasActionsRequest addAliasAction(AliasAction aliasAction) {
        aliasActions.add(aliasAction);
        build();
        return this;
    }

    /**
     * 构建查询语句
     */
    public void build() {
        ArrayNode actions = JsonUtil.createArrayNode();
        //别名
        if (aliasActions != null && aliasActions.size() > 0) {
            aliasActions.forEach(aliasAction -> {
                ObjectNode node = JsonUtil.createObjectNode();
                ObjectNode nodeInner = JsonUtil.createObjectNode();
                nodeInner.put(ElasticSearchConst.INDEX, aliasAction.getIndex());
                nodeInner.put(ElasticSearchConst.ALIAS, aliasAction.getAlias());
                nodeInner.put(ElasticSearchConst.IS_WRITE_INDEX, aliasAction.getWriteIndex());
                node.set(aliasAction.getAction(), nodeInner);
                actions.add(node);
            });
        }
        this.rootNode.set(ElasticSearchConst.ALIASES_ACTIONS, actions);
    }

}
