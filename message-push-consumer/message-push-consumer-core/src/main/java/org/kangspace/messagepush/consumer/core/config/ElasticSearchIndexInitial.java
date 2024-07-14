package org.kangspace.messagepush.consumer.core.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.kangspace.messagepush.consumer.core.service.ElasticSearchService;
import org.kangspace.messagepush.core.elasticsearch.ElasticSearchConst;
import org.kangspace.messagepush.core.elasticsearch.request.AliasAction;
import org.kangspace.messagepush.core.elasticsearch.request.JsonAliasActionsRequest;
import org.kangspace.messagepush.core.elasticsearch.request.JsonCreateIndexRequest;
import org.kangspace.messagepush.core.elasticsearch.request.PlainRolloverRequest;
import org.kangspace.messagepush.core.util.JsonUtil;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * ES索引初始化
 *
 * @author kango2gler@gmail.com
 * @since 2021/8/9
 */
@Slf4j
@Setter
@Getter
@Configuration
@ConfigurationProperties("messagepush.elasticsearch")
public class ElasticSearchIndexInitial implements ApplicationListener<ApplicationStartedEvent>, ApplicationContextAware {
    /**
     * 索引别名对应的第一个索引名
     */
    private static final String DEFAULT_FIRST_INDEX_SUFFIX = "-000001";
    /**
     * 索引别名关联的索引字符串(模糊索引)
     */
    private static final String DEFAULT_ALIAS_INDIES_SUFFIX = "-*";
    private ApplicationContext applicationContext;
    private Boolean isLoaded = false;


    @Resource
    private ElasticSearchService elasticSearchService;

    /**
     * 消息推送记录索引
     * (当前名称会作为索引记录的别名,实际索引名为-000001格式)
     * 如:
     * messagePushSingleTopicIndex = "messagePushSingleTopicIndex"
     * 则:
     * 1. 创建名称为 messagePushSingleTopicIndex-000001 的索引
     * 2. 创建别名为 messagePushSingleTopicIndex 的别名索引, 别名对应的索引为: messagePushSingleTopicIndex-*,同时设置可写
     * 3. 检查 messagePushSingleTopicIndex 别名是否满足滚动(_rollover)索引条件(2000w条),
     * 若满足滚动索引条件,则调用滚动API,将自动生成名称为 messagePushSingleTopicIndex-000002 的索引,并将名 messagePushSingleTopicIndex-000002
     * 设置为可写索引
     */
    private String messagePushSingleTopicIndex;
    /**
     * 消息推送记录索引别名滚动最大日志数(默认2000W)
     */
    private Long messagePushSingleTopicIndexRolloverMaxDoc = 2000_0000L;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        if (this.isLoaded) {
            return;
        }
        log.info("ElasticSearch 索引初始化: start, ElasticSearchIndexInitial: messagepush.elasticsearch.message-push-single-topic-index: [{}]", messagePushSingleTopicIndex);
        if (StringUtils.isBlank(messagePushSingleTopicIndex)) {
            log.error("ElasticSearch 索引初始化:: ElasticSearchIndexInitial error, config [{}] not found", "messagepush.elasticsearch.message-push-single-topic-index");
            return;
        }
        initElasticSearchIndexAlias();
        log.info("ElasticSearch 索引初始化: end");
        this.isLoaded = true;
    }

    /**
     * 初始化ElasticSearch 索引
     * (包括设置滚动索引,别名)
     * 1. 检查别名是否存在,
     * a. 别名不存在->[检查别名对应的第一个索引是否存在,第一个索引不存在则自动创建索引]->创建别名
     * b. 别名存在-> 检查别名索引是否需要滚动,需要则调用滚动API
     */
    public void initElasticSearchIndexAlias() {
        // 当前索引名作为别名,新索引名: 在当前索引名后添加-000001
        String indexAlias = messagePushSingleTopicIndex;
        String defaultFirstIndex = indexAlias + DEFAULT_FIRST_INDEX_SUFFIX;
        // 检查索引别名是否存在
        boolean aliasExists = elasticSearchService.existsAlias(indexAlias);
        // 别名不存在则检查索引是否存在
        if (!aliasExists) {
            log.info("ElasticSearch 索引初始化: 别名不存在: 别名:{}", indexAlias);
            boolean firstIndexExists = elasticSearchService.existsIndex(defaultFirstIndex);
            // 检查默认索引是否存在,即索引名
            if (!firstIndexExists) {
                log.info("ElasticSearch 索引初始化: index:[{}] 不存在,开始创建!", defaultFirstIndex);
                boolean result = elasticSearchService.createIndex(defaultJsonCreateIndexRequest(defaultFirstIndex));
                log.info("ElasticSearch 索引初始化: index: [{}] 创建结束, result: [{}]", defaultFirstIndex, result);
            }
            String aliasIndies = indexAlias + DEFAULT_ALIAS_INDIES_SUFFIX;
            // 创建索引别名
            AliasAction action = new AliasAction("add", aliasIndies, indexAlias, true);
            JsonAliasActionsRequest actionsRequest = new JsonAliasActionsRequest(action);
            boolean createdAlias = elasticSearchService.aliasAction(actionsRequest);
            log.info("ElasticSearch 索引初始化: 创建别名:{} ,创建结果:{}", actionsRequest, createdAlias);
        } else {
            PlainRolloverRequest plainRolloverRequest = new PlainRolloverRequest();
            plainRolloverRequest.setAlias(indexAlias);
            plainRolloverRequest.setDryRun(true);
            plainRolloverRequest.setMaxDocs(messagePushSingleTopicIndexRolloverMaxDoc);
            log.info("ElasticSearch 索引初始化: 别名存在,检查别名是否需要滚动, 别名:{}, plainRolloverRequest:{}", indexAlias, plainRolloverRequest);
            if (elasticSearchService.rollover(plainRolloverRequest)) {
                log.info("ElasticSearch 索引初始化: 别名需要滚动,alias:{}", indexAlias);
                plainRolloverRequest.setDryRun(false);
                boolean rollover = elasticSearchService.rollover(plainRolloverRequest);
                log.info("ElasticSearch 索引初始化: 别名需要滚动结果,alias:{},结果:{}", indexAlias, rollover);
            } else {
                log.info("ElasticSearch 索引初始化: 别名无需滚动, alias:{}", indexAlias);
            }
        }
    }

    /**
     * 创建ElasticSearch 创建索引请求对象
     *
     * @param indexName 索引名称
     * @return {@link JsonCreateIndexRequest}
     */
    public JsonCreateIndexRequest defaultJsonCreateIndexRequest(String indexName) {
        return new JsonCreateIndexRequest(indexName, 2, 1, getDefaultModelMapping(), null);
    }


    /**
     * 获取默认实体映射(设置c_time类型)
     *
     * @return map
     */
    private Map<String, Object> getDefaultModelMapping() {
        Map<String, Object> defaultMap = new HashMap<>();
        defaultMap.put("c_time", JsonUtil.createObjectNode().put(ElasticSearchConst.TYPE, ElasticSearchConst.DATE)
                .put("format", "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||" + ElasticSearchConst.DATE_FORMAT)
        );
        return defaultMap;
    }

}
