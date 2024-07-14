package org.kangspace.messagepush.core.elasticsearch.request;


import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kangspace.messagepush.core.constant.OrderConstant;
import org.kangspace.messagepush.core.dto.page.Order;
import org.kangspace.messagepush.core.dto.page.PageRequestDto;
import org.kangspace.messagepush.core.elasticsearch.ElasticSearchConst;
import org.kangspace.messagepush.core.elasticsearch.query.CustomQueryBuilder;
import org.kangspace.messagepush.core.elasticsearch.util.QueryUtil;
import org.kangspace.messagepush.core.util.JsonUtil;
import org.kangspace.messagepush.core.util.ListUtil;

import java.util.List;

/**
 * json查询请求
 *
 * @author kango2gler@gmail.com
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Slf4j
@NoArgsConstructor
public class JsonSearchRequest<R> extends BaseRequest {
    private String scriptJson;
    /**
     * 类型，如果多个类型半角逗号分割
     */
    private String type;

    /**
     * 查询数量
     */
    private Integer size;

    /**
     * 默认排序字段,默认排序字段
     */
    private Order defaultOrder;

    /**
     * 返回结果类对象
     */
    private Class<R> responseClass;

    /**
     * 自定义查询条件构建器，调用其handler方法构建自定义查询语句
     */
    private CustomQueryBuilder customQueryBuilder;

    /**
     * 是否放弃查询，某些特殊情况，如全文检索无可用分词，可设置此值为true，则调用es查询方法时不进行es查询，直接返回空数据
     */
    private Boolean abandon;

    /**
     * es查询时间（毫秒）
     */
    private Long took;


    /**
     * 构建查询对象
     *
     * @param index         索引，多个索引半角逗号分割
     * @param query         查询对象
     * @param responseClass 返回结果类
     * @param <T>           查询对象泛型
     */
    public <T extends PageRequestDto> JsonSearchRequest(String index, T query, Class<R> responseClass) throws Exception {
        this(index, query, null, null, responseClass);
    }

    /**
     * 构建查询对象
     *
     * @param index              索引，多个索引半角逗号分割
     * @param query              查询对象
     * @param customQueryBuilder 自定义查询条件构建器
     * @param defaultOrder       默认排序字段
     * @param responseClass      返回结果类
     * @param <T>                查询对象泛型
     */
    public <T extends PageRequestDto> JsonSearchRequest(String index, T query, CustomQueryBuilder customQueryBuilder, Order defaultOrder, Class responseClass) throws Exception {
        super(index);
        this.defaultOrder = defaultOrder;
        this.responseClass = responseClass;

        //显示字段
        setSource(query);
        //分页
        setFromAndSize(query);
        //排序
        setSort(query, getDefaultOrder());
        //查询
        setQuery(query, customQueryBuilder);
    }

    /**
     * 构建简版查询对象，适用于count查询
     *
     * @param index              索引，多个索引半角逗号分割
     * @param query              查询对象
     * @param customQueryBuilder 自定义查询条件构建器
     * @param <T>                查询对象泛型
     */
    public <T> JsonSearchRequest(String index, T query, CustomQueryBuilder customQueryBuilder) throws Exception {
        super(index);
        //查询
        setQuery(query, customQueryBuilder);
        //设置不需要返回结果集
        super.getRootNode().put(ElasticSearchConst.SIZE, 0);
        //获取按条查询出来真实的总数
        super.getRootNode().put(ElasticSearchConst.TRACK_TOTAL_HITS, true);
    }

    /**
     * 设置查询语句
     *
     * @param query
     * @param customQueryBuilder
     * @param <T>
     */
    private <T> void setQuery(T query, CustomQueryBuilder<T> customQueryBuilder) throws Exception {
        QueryUtil.entityToQuery(this, query);
        if (customQueryBuilder != null) {
            customQueryBuilder.handler(this, query);
        }

    }

    /**
     * 分页处理
     *
     * @param query 查询实体类
     */
    public <T extends PageRequestDto> void setFromAndSize(T query) {
        if (query == null) {
            return;
        }

        //设置默认页码
        if (query.getPageNum() == null || query.getPageNum() <= 0) {
            log.debug("PageNum={},不符合规范，设置为默认值{}。{}", query.getPageNum(), 1, query);
            query.setPageNum(1);
        }

        //未分页，查询默认条数
        if (query.getPageSize() == null || query.getPageSize() <= 0) {
            log.debug("pageSize={},不符合规范，设置为默认值{}。{}", query.getPageNum(), ElasticSearchConst.DEFAULT_PAGE_SIZE, query.getPageSize());
            query.setPageSize(ElasticSearchConst.DEFAULT_PAGE_SIZE);
        }

        //保证性能，默认不超过10000条，调整页码
        if (query.getPageNum() * query.getPageSize() > ElasticSearchConst.MAX_PAGE_SEARCH_COUNT) {
            log.warn("保证性能，PageNum*pageSize不得超过{}条，设置PageNum为{}。{}", ElasticSearchConst.MAX_PAGE_SEARCH_COUNT, 1, query);
            query.setPageNum(1);
        }

        //保证性能，默认不超过10000条，调整页显示记录数
        if (query.getPageNum() * query.getPageSize() > ElasticSearchConst.MAX_PAGE_SEARCH_COUNT) {
            log.warn("保证性能，PageNum*pageSize不得超过{}条，设置pageSize为{}。{}", ElasticSearchConst.MAX_PAGE_SEARCH_COUNT, ElasticSearchConst.DEFAULT_PAGE_SIZE, query);
            query.setPageSize(ElasticSearchConst.MAX_PAGE_SEARCH_COUNT);
        }

        super.getRootNode().put(ElasticSearchConst.FROM, (query.getPageNum() - 1) * query.getPageSize());
        super.getRootNode().put(ElasticSearchConst.SIZE, query.getPageSize());
        setSize(query.getPageSize());
    }

    /**
     * 排序处理
     *
     * @param query        查询实体类
     * @param defaultOrder 默认排序字段
     */
    public <T extends PageRequestDto> void setSort(T query, Order defaultOrder) {
        if (query == null) {
            return;
        }
        List<Order> orders = ListUtil.isEmpty(query.getOrders()) && defaultOrder != null ? ListUtil.getList(defaultOrder) : query.getOrders();

        if (ListUtil.isEmpty(orders)) {
            return;
        }
        ArrayNode sorNode = JsonUtil.createArrayNode();
        orders.forEach(order -> {
            if (!"".equals(order.getField())) {
                sorNode.add(JsonUtil.createObjectNode().put(order.getField(), OrderConstant.ASC.equals(order.getSequence()) ? ElasticSearchConst.ASC : ElasticSearchConst.DESC));
            }
        });
        if (sorNode.size() > 0) {
            super.getRootNode().set(ElasticSearchConst.SORT, sorNode);
        }

    }

    /**
     * 拼装显示字段
     *
     * @param query
     * @param <T>
     */
    public <T extends PageRequestDto> void setSource(T query) {
        if (query == null || ListUtil.isEmpty(query.getFields())) {
            return;
        }
        ArrayNode arrayNode = JsonUtil.createArrayNode();
        query.getFields().forEach(field -> arrayNode.add(field));
        super.getRootNode().set(ElasticSearchConst.UNDERSCORE_SOURCE, arrayNode);
    }

}
