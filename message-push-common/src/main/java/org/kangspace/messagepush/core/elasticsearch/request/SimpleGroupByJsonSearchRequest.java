package org.kangspace.messagepush.core.elasticsearch.request;


import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.kangspace.messagepush.core.elasticsearch.ElasticSearchConst;
import org.kangspace.messagepush.core.elasticsearch.util.QueryUtil;
import org.kangspace.messagepush.core.util.JsonUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * json查询请求
 *
 * @author kango2gler@gmail.com
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class SimpleGroupByJsonSearchRequest<R> extends JsonSearchRequest<R> {

    /**
     * groupBy Agg分组字段
     */
    private List<String> groupByAggFields = new ArrayList<>();

    public SimpleGroupByJsonSearchRequest(String index, R query, Class<R> responseClass) throws Exception {
        setIndex(index);
        setSize(0);
        setQuery(query);
        setResponseClass(responseClass);
        super.getRootNode().put(ElasticSearchConst.SIZE, 0);
    }

    public SimpleGroupByJsonSearchRequest(String index, String scriptJson, Class<R> responseClass) throws Exception {
        setIndex(index);
        setScriptJson(scriptJson);
        setResponseClass(responseClass);
    }

    /**
     * 设置查询语句
     *
     * @param query 查询对象
     * @param <T>   T
     */
    private <T> void setQuery(T query) throws Exception {
        QueryUtil.entityToQuery(this, query);
    }

    /**
     * 添加分组字段
     * <code>
     * "aggs":{
     * "groupByIndex":{
     * "terms":{
     * "field":"index",
     * "size":1000
     * },
     * "aggs":{
     * "groupByTime":{
     * "terms":{
     * "field":"time",
     * "size":1
     * }
     * }
     * }
     * }
     * }
     * </code>
     *
     * @param field 分组字段名
     * @return {@link AggSimpleGroupByParam}
     */
    public AggSimpleGroupByParam addGroupByField(String field) {
        AggSimpleGroupByParam param = new AggSimpleGroupByParam(this).addAggNodeField(field, 10000);
        super.getRootNode().setAll(param.getAggNode());
        return param;
    }

    /**
     * agg 简单分组字段对象
     *
     * @author kango2gler@gmail.com
     * @since 2022/1/24
     */
    public static class AggSimpleGroupByParam<T> {
        private SimpleGroupByJsonSearchRequest<T> searchRequest;
        private ObjectNode aggNode;
        private ObjectNode node;

        private AggSimpleGroupByParam() {
        }

        AggSimpleGroupByParam(SimpleGroupByJsonSearchRequest<T> searchRequest) {
            this.searchRequest = searchRequest;
        }

        /**
         * 添加子
         *
         * @param field
         * @return
         */
        public AggSimpleGroupByParam<T> addField(String field) {
            addNodeField(this.node, field, 1);
            return this;
        }

        AggSimpleGroupByParam<T> addAggNodeField(String field, int size) {
            ObjectNode aggNode = JsonUtil.createObjectNode();
            ObjectNode node = addNodeField(aggNode, field, size);
            this.aggNode = aggNode;
            this.node = node;
            return this;
        }

        /**
         * agg节点下添加group by 字段
         *
         * @param aggNode aggNode
         * @param field   group by 字段
         * @param size    结果集size,默认为1
         */
        private ObjectNode addNodeField(ObjectNode aggNode, String field, int size) {
            ObjectNode node = JsonUtil.createObjectNode();
            node.set("terms", JsonUtil.createObjectNode().put("field", field).put("size", size));
            ObjectNode groupByNode = JsonUtil.createObjectNode();
            groupByNode.set("group_by_" + field, node);
            aggNode.set(ElasticSearchConst.AGGS, groupByNode);
            searchRequest.getGroupByAggFields().add(field);
            return node;
        }

        ObjectNode getAggNode() {
            return aggNode;
        }
    }
}
