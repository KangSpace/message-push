package org.kangspace.messagepush.consumer.core.service;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchException;
import org.kangspace.messagepush.core.dto.page.Order;
import org.kangspace.messagepush.core.dto.page.PageRequestDto;
import org.kangspace.messagepush.core.dto.page.PageResponseDto;
import org.kangspace.messagepush.core.elasticsearch.ElasticsearchManager;
import org.kangspace.messagepush.core.elasticsearch.request.JsonAliasActionsRequest;
import org.kangspace.messagepush.core.elasticsearch.request.JsonCreateIndexRequest;
import org.kangspace.messagepush.core.elasticsearch.request.JsonSearchRequest;
import org.kangspace.messagepush.core.elasticsearch.request.PlainRolloverRequest;
import org.kangspace.messagepush.core.util.JsonUtil;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * ElasticSearch处理Service
 *
 * @author kango2gler@gmail.com
 * @since 2021/8/9
 */
@Slf4j
@Service
public class ElasticSearchService {
    @Resource
    private ElasticsearchManager elasticsearchManager;

    /**
     * 创建索引
     *
     * @param request {@link JsonCreateIndexRequest}
     * @return true/false
     */
    public Boolean createIndex(JsonCreateIndexRequest request) {
        Boolean created = false;
        try {
            created = elasticsearchManager.createIndex(request);
        } catch (Exception e) {
            log.error("::: ElasticSearchService createIndex error,request:{} ,error:{}", request, e.getMessage(), e);
        }
        return created;
    }

    /**
     * 是否存在索引
     *
     * @param index 索引名称
     * @return true/false
     */
    public Boolean existsIndex(String index) {
        Boolean exists = false;
        try {
            exists = elasticsearchManager.existsIndex(index);
        } catch (Exception e) {
            log.error("::: ElasticSearchService existsIndex error,index:{} ,error:{}", index, e.getMessage(), e);
        }
        return exists;
    }

    /**
     * 判断指定别名是否存在
     *
     * @param alias 别名
     * @return true: 存在, false: 不存在
     */
    public Boolean existsAlias(String alias) {
        Boolean exists = false;
        try {
            exists = elasticsearchManager.existsAlias(alias);
        } catch (Exception e) {
            log.error("::: ElasticSearchService existsAlias error,alias:{} ,error:{}", alias, e.getMessage(), e);
        }
        return exists;
    }

    /**
     * 别名操作
     *
     * @param jsonAliasActionsRequest 别名操作对象
     * @return true:成功, false:失败
     */
    public Boolean aliasAction(JsonAliasActionsRequest jsonAliasActionsRequest) {
        Boolean action = false;
        try {
            action = elasticsearchManager.aliasAction(jsonAliasActionsRequest);
        } catch (Exception e) {
            log.error("::: ElasticSearchService aliasAction error,jsonAliasActionsRequest:{} ,error:{}", jsonAliasActionsRequest, e.getMessage(), e);
        }
        return action;
    }

    /**
     * 别名滚动索引
     *
     * @param rolloverRequest 滚动请求
     * @return true: 需要滚动/滚动成功,false: 无需滚动
     */
    public Boolean rollover(PlainRolloverRequest rolloverRequest) {
        Boolean rollover = false;
        try {
            rollover = elasticsearchManager.rollover(rolloverRequest);
        } catch (Exception e) {
            log.error("::: ElasticSearchService rollover error,rolloverRequest:{} ,error:{}", rolloverRequest, e.getMessage(), e);
        }
        return rollover;
    }


    /**
     * 插入数据
     *
     * @param index 索引名称
     * @param data  索引数据
     * @return true/false
     */
    public <T> Boolean insert(String index, T data) {
        Boolean inserted = false;
        try {
            inserted = elasticsearchManager.insert(index, data);
        } catch (Exception e) {
            log.error("::: ElasticSearchService insert error, index:{}, data:{},error:{}", index, data, e.getMessage(), e);
        }
        return inserted;
    }

    /**
     * ES分页查询
     *
     * @param jsonSearchRequest 查询对象
     * @return {@link PageResponseDto}
     */
    public <T> PageResponseDto<T> page(JsonSearchRequest<T> jsonSearchRequest) {
        String index = jsonSearchRequest.getIndex();
        if (log.isDebugEnabled()) {
            log.debug("::: ElasticSearchService page,分页查询语句为[{}]", JsonUtil.toFormatJson(jsonSearchRequest.getRootNode()));
        }
        PageResponseDto<T> page = null;
        try {
            page = elasticsearchManager.page(jsonSearchRequest);
        } catch (Exception e) {
            log.error("::: ElasticSearchService page error, index:{}, jsonSearchRequest:{},error:{}", index, jsonSearchRequest, e.getMessage(), e);
        }
        return page;
    }

    /**
     * ES分页查询
     *
     * @param index        索引
     * @param query        查询对象,继承于{@link PageResponseDto}
     * @param defaultOrder 默认排序
     * @param clazz        返回对象类型
     * @return {@link PageResponseDto}
     */
    public <T, QUERY extends PageRequestDto> PageResponseDto<T> page(String index, QUERY query, Order defaultOrder, Class<T> clazz) {
        JsonSearchRequest<T> jsonSearchRequest;
        try {
            jsonSearchRequest = new JsonSearchRequest<>(index, query, null, defaultOrder, clazz);
        } catch (Exception e) {
            log.error("::: ElasticSearchService page new JsonSearchRequest()  error, index:{}, query:{},order:{},clazz:{},error:{}",
                    index, query, defaultOrder, clazz, e.getMessage(), e);
            throw new ElasticsearchException(e);
        }
        return page(jsonSearchRequest);
    }

    /**
     * 异步写ElasticSearch
     *
     * @param index 索引
     * @param data  数据
     */
    @Async("asyncTaskExecutor")
    public void writeElasticSearch(String index, Object data) {
        // 消息异步写ES
        boolean inserted = insert(index, data);
        if (log.isDebugEnabled()) {
            log.debug("ElasticSearch 操作: insert [{}],index:[{}],data:[{}]", inserted, index, data.toString());
        } else {
            log.info("ElasticSearch 操作: insert [{}],index:[{}]", inserted, index);
        }
    }
}
