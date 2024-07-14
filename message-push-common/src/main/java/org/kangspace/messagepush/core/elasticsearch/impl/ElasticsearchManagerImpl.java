package org.kangspace.messagepush.core.elasticsearch.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.indices.rollover.RolloverRequest;
import org.elasticsearch.client.indices.rollover.RolloverResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.*;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchModule;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.histogram.ParsedDateHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.ParsedSingleValueNumericMetricsAggregation;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.kangspace.messagepush.core.dto.page.PageResponseDto;
import org.kangspace.messagepush.core.elasticsearch.ElasticSearchConst;
import org.kangspace.messagepush.core.elasticsearch.ElasticsearchManager;
import org.kangspace.messagepush.core.elasticsearch.RestHighLevelClientContextHolder;
import org.kangspace.messagepush.core.elasticsearch.annotation.EntityField;
import org.kangspace.messagepush.core.elasticsearch.annotation.Score;
import org.kangspace.messagepush.core.elasticsearch.request.*;
import org.kangspace.messagepush.core.elasticsearch.response.AggregationsResult;
import org.kangspace.messagepush.core.util.*;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.data.annotation.Id;
import org.springframework.http.HttpMethod;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;


/**
 * Elasticsearch数据访问对象
 *
 * @author kango2gler@gmail.com
 */
@Slf4j
public class ElasticsearchManagerImpl implements ElasticsearchManager {

    /**
     * 根据value类型返回script脚本value值。 <br/>
     * (字符串时返回带双引号的值)<br/>
     * 如:
     * 1. value = "123", 返回 "123"
     * 2. value = 123, 返回 123
     * 3. value = ["1","2","3"],返回 "[\"1\",\"2\",\"3\"]"
     * 3. value = [1,2,3],返回 "[1, 2, 3]"
     *
     * @return 返回实际的脚本value字符串
     */
    private static <T> Object toActualTypeValueForScript(T value) {
        if (value == null) {
            return "";
        }
        if (value instanceof String) {
//            return new StringBuilder("\"").append(value).append("\"").toString();
            return new StringBuilder("").append(value).append("").toString();
        }
        boolean isArray;
        if ((isArray = value.getClass().isArray()) || value instanceof List) {
            List<Object> listObj;
            if (isArray) {
                listObj = Arrays.asList(ArrayUtil.toBoxed(value));
            } else {
                listObj = (List<Object>) value;
            }
            return listObj.stream().map(t -> {
                if (t instanceof String) {
//                    return new StringBuilder("\"").append(t).append("\"");
                    return new StringBuilder("").append(t).append("");
                }
                return t.toString();
            }).collect(Collectors.joining(","));
        }
        return value;
    }

    @Override
    public Boolean existsIndex(String index) throws IOException {
        GetIndexRequest request = new GetIndexRequest();
        request.indices(index);
        return RestHighLevelClientContextHolder.getRestHighLevelClientByKey().indices().exists(request, RequestOptions.DEFAULT);
    }

    @Override
    public Boolean createIndex(JsonCreateIndexRequest jsonIndexRequest) throws Exception {
        if (existsIndex(jsonIndexRequest.getIndex())) {
            log.warn("索引" + jsonIndexRequest.getIndex() + "已存在！");
            return false;
        }
        String endpoint = "/" + jsonIndexRequest.getIndex();
        Request req = new Request(HttpMethod.PUT.name(), endpoint);
        String json = JsonUtil.toJson(jsonIndexRequest.getRootNode());
        req.setJsonEntity(json);
        log.debug("ElasticsearchDaoImpl.createIndex:{endpoint:{},json:{}}", endpoint, json);
        Response response = RestHighLevelClientContextHolder.getRestHighLevelClientByKey().getLowLevelClient().performRequest(req);
        return RestStatus.OK.getStatus() == response.getStatusLine().getStatusCode();
    }

    @Override
    public Boolean deleteIndex(String index) throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest(index.split(","));
        AcknowledgedResponse response = RestHighLevelClientContextHolder.getRestHighLevelClientByKey().indices().delete(request, RequestOptions.DEFAULT);
        return response.isAcknowledged();
    }

    @Override
    public Boolean existsAlias(String alias) throws IOException {
        GetAliasesRequest request = new GetAliasesRequest();
        request.aliases(alias);
        return RestHighLevelClientContextHolder.getRestHighLevelClientByKey().indices().existsAlias(request, RequestOptions.DEFAULT);
    }

    @Override
    public Boolean aliasAction(JsonAliasActionsRequest jsonAliasActionsRequest) throws IOException {
        String json = JsonUtil.toJson(jsonAliasActionsRequest.getRootNode());
        log.debug("ElasticsearchDaoImpl.aliasAction: json:{}}", json);
        IndicesAliasesRequest indicesAliasesRequest = new IndicesAliasesRequest();
        jsonAliasActionsRequest.getAliasActions().forEach(aa -> {
            IndicesAliasesRequest.AliasActions aliasActions = null;
            if (ElasticSearchConst.ALIASES_ACTIONS_ADD.equals(aa.getAction())) {
                aliasActions = IndicesAliasesRequest.AliasActions.add();
            } else if (ElasticSearchConst.ALIASES_ACTIONS_REMOVE.equals(aa.getAction())) {
                aliasActions = IndicesAliasesRequest.AliasActions.remove();
            }
            if (aliasActions != null) {
                aliasActions.alias(aa.getAlias()).index(aa.getIndex()).writeIndex(aa.getWriteIndex());
                indicesAliasesRequest.addAliasAction(aliasActions);
            }
        });
        AcknowledgedResponse response = RestHighLevelClientContextHolder.getRestHighLevelClientByKey().indices()
                .updateAliases(indicesAliasesRequest, RequestOptions.DEFAULT);
        return response.isAcknowledged();
    }

    @Override
    public Boolean rollover(PlainRolloverRequest rolloverRequest) throws IOException {
        String alias = rolloverRequest.getAlias();
        String newIndexName = rolloverRequest.getNewIndexName();
        Objects.requireNonNull(rolloverRequest.getAlias(), "alias 别名不能为空");

        RolloverRequest request = new RolloverRequest(alias, newIndexName);
        if (rolloverRequest.getMaxAge() != null) {
            request.addMaxIndexAgeCondition(rolloverRequest.getMaxAge());
        }
        if (rolloverRequest.getMaxDocs() != null) {
            request.addMaxIndexDocsCondition(rolloverRequest.getMaxDocs());
        }
        if (rolloverRequest.getMaxSize() != null) {
            request.addMaxIndexSizeCondition(rolloverRequest.getMaxSize());
        }
        if (rolloverRequest.getDryRun() != null) {
            request.dryRun(rolloverRequest.getDryRun());
        }
        log.debug("ElasticsearchDaoImpl.rollover: plainRolloverRequest:{}}", rolloverRequest);
        RolloverResponse response = RestHighLevelClientContextHolder.getRestHighLevelClientByKey().indices().rollover(request, RequestOptions.DEFAULT);
        log.debug("ElasticsearchDaoImpl.rollover: response:{}}", response);
        return response.isAcknowledged() || (response.isDryRun() && response.getConditionStatus().values().stream().anyMatch(t -> t));
    }

    @Override
    public <T> Boolean insert(String index, T entity) throws Exception {
        log.debug("ElasticsearchDaoImpl.insert:{index:{},entity:{}}", index, entity);
        IndexRequest indexRequest = getIndexRequest(index, entity);
        IndexResponse response = RestHighLevelClientContextHolder.getRestHighLevelClientByKey().index(indexRequest, RequestOptions.DEFAULT);
        log.debug("update status:{}", response.status());
        return RestStatus.OK.equals(response.status()) || RestStatus.CREATED.equals(response.status());
    }

    @Override
    public <T> Boolean insert(String index, List<T> list) throws Exception {
        log.debug("ElasticsearchDaoImpl.insert:{index:{},list:{}}", index, list);
        BulkRequest bulkRequest = new BulkRequest();
        for (T item : list) {
            bulkRequest.add(getIndexRequest(index, item));
        }
        BulkResponse response = RestHighLevelClientContextHolder.getRestHighLevelClientByKey().bulk(bulkRequest, RequestOptions.DEFAULT);
        log.debug("update status:{}", response.status());
        return RestStatus.OK.equals(response.status());
    }

    @Override
    public <T> List<String> batchInsert(String index, List<T> list) throws Exception {
        log.debug("ElasticsearchDaoImpl.bactchInsert:{index:{},list:{}}", index, list);
        BulkRequest bulkRequest = new BulkRequest();
        for (T item : list) {
            bulkRequest.add(getIndexRequest(index, item));
        }
        BulkResponse response = RestHighLevelClientContextHolder.getRestHighLevelClientByKey().bulk(bulkRequest, RequestOptions.DEFAULT);
        List<String> failuerMessages = Arrays.asList(response.getItems())
                .stream().filter(p -> StringUtils.isNotBlank(p.getFailureMessage()))
                .map(BulkItemResponse::getFailureMessage).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(failuerMessages)) {
            log.info("es bactchInsert error->{}", failuerMessages);
            return failuerMessages;
        }
        log.info("es bactchInsert totalCount：{}", list.size());
        return null;
    }

    @Override
    public Boolean insert(String index, String idKey, List<Map<String, Object>> list) throws Exception {
        BulkRequest bulkRequest = new BulkRequest();
        for (Map<String, Object> map : list) {
            IndexRequest indexRequest = new IndexRequest(index);
            if (StrUtil.isNotEmpty(idKey) && map.containsKey(idKey)) {
                String id = map.get(idKey) + "";
                indexRequest.id(id);
                map.remove(idKey);
                indexRequest.source(JsonUtil.toJson(map), XContentType.JSON);
                map.put(idKey, id);
            } else {
                indexRequest.source(JsonUtil.toJson(map), XContentType.JSON);
            }
            bulkRequest.add(indexRequest);
        }
        BulkResponse response = RestHighLevelClientContextHolder.getRestHighLevelClientByKey().bulk(bulkRequest, RequestOptions.DEFAULT);
        log.debug("update status:{}", response.status());
        return RestStatus.OK.equals(response.status());
    }

    @Override
    public <T> IndexRequest getIndexRequest(String index, T entity) throws IOException, IllegalAccessException {
        IndexRequest indexRequest = new IndexRequest(index);
        //设置id
        Field idField = getIdField(entity.getClass());
        String id = null;
        if (idField != null) {
            id = (String) idField.get(entity);
            indexRequest.id(id);
            idField.set(entity, null);
        }
        String source = JsonUtil.toJson(entity);
        if (idField != null) {
            idField.set(entity, id);
        }
        indexRequest.id();
        indexRequest.source(source, XContentType.JSON);

        return indexRequest;
    }

    @Override
    public Boolean update(String index, String id, Map<String, Object> map) throws Exception {
        UpdateRequest updateRequest = new UpdateRequest(index, null, id);
        updateRequest.doc(map);
        UpdateResponse response = RestHighLevelClientContextHolder.getRestHighLevelClientByKey().update(updateRequest, RequestOptions.DEFAULT);
        log.debug("update status:{}", response.status());
        return RestStatus.OK.equals(response.status());
    }

    @Override
    public <T> Boolean update(String index, String id, T entity) throws Exception {
        Map<String, Object> map = Maps.newHashMap();
        Field[] fields = entity.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            Object value = field.get(entity);
            if (value == null) {
                continue;
            }
            EntityField queryEntityField = field.getAnnotation(EntityField.class);
            map.put(queryEntityField != null && StrUtil.isNotEmpty(queryEntityField.field()) ? queryEntityField.field() : field.getName(), value);
        }
        return update(index, id, map);
    }

    @Override
    public Boolean update(String index, List<String> ids, List<Map<String, Object>> list) throws Exception {
        BulkRequest bulkRequest = new BulkRequest();
        if (ids.size() != list.size()) {
            throw new RuntimeException("id列表与跟新数据列表size不同");
        }
        for (int i = 0; i < list.size(); i++) {
            String id = ids.get(i);
            Map<String, Object> item = list.get(i);
            UpdateRequest updateRequest = new UpdateRequest(index, null, id);
            updateRequest.doc(item);
            bulkRequest.add(updateRequest);
        }
        BulkResponse response = RestHighLevelClientContextHolder.getRestHighLevelClientByKey().bulk(bulkRequest, RequestOptions.DEFAULT);
        log.debug("update status:{}", response.status());
        return RestStatus.OK.equals(response.status());
    }

    @Override
    public <T> List<String> batchUpdate(String index, List<String> ids, List<Map<String, Object>> list) throws Exception {
        BulkRequest bulkRequest = new BulkRequest();
        if (ids.size() != list.size()) {
            throw new RuntimeException("id列表与跟新数据列表size不同");
        }
        for (int i = 0; i < list.size(); i++) {
            String id = ids.get(i);
            Map<String, Object> item = list.get(i);
            UpdateRequest updateRequest = new UpdateRequest(index, null, id);
            updateRequest.doc(JSON.toJSONString(item.get(id)), XContentType.JSON);
            bulkRequest.add(updateRequest);
        }
        BulkResponse response = RestHighLevelClientContextHolder.getRestHighLevelClientByKey().bulk(bulkRequest, RequestOptions.DEFAULT);
        List<String> failuerMessages = (List) Arrays.asList(response.getItems()).stream().filter((p) -> {
            return StringUtils.isNotBlank(p.getFailureMessage());
        }).map(BulkItemResponse::getFailureMessage).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(failuerMessages)) {
            log.info("es batchUpdate error->{}", failuerMessages);
            return failuerMessages;
        } else {
            log.info("es batchUpdate totalCount：{}", list.size());
            return null;
        }
    }

    @Override
    public Boolean delete(String index, String id) throws Exception {
        DeleteRequest request = new DeleteRequest(index, null, id);
        DeleteResponse response = RestHighLevelClientContextHolder.getRestHighLevelClientByKey().delete(request, RequestOptions.DEFAULT);
        return RestStatus.OK.equals(response.status());
    }

    @Override
    public Boolean delete(String index, List<String> ids) throws Exception {
        BulkRequest bulkRequest = new BulkRequest();
        ids.forEach(id -> bulkRequest.add(new DeleteRequest(index, null, id)));
        BulkResponse response = RestHighLevelClientContextHolder.getRestHighLevelClientByKey().bulk(bulkRequest, RequestOptions.DEFAULT);
        log.debug("delete status:{}", response.status());
        return RestStatus.OK.equals(response.status());
    }

    @Override
    public <T> T get(String index, String id, Class<T> classType) throws Exception {
        return get(index, id, null, classType);
    }

    @Override
    public <T> T get(String index, String id, List<String> fields, Class<T> classType) throws Exception {
        GetRequest request = new GetRequest(index, id);
        GetResponse response = RestHighLevelClientContextHolder.getRestHighLevelClientByKey().get(request, RequestOptions.DEFAULT);
        if (response.getSource() == null) {
            return null;
        }

        //设置id
        Field idField = getIdField(classType);
        T t = JsonUtil.toObject(response.getSourceAsString(), classType);
        if (idField != null) {
            idField.set(t, response.getId());
        }
        BeanUtil.setFieldsNull(t, fields);
        return t;
    }

    @Override
    public <T> T get(JsonSearchRequest<T> jsonSearchRequest) throws Exception {
        //如果放弃查询，返回为空
        if (Objects.nonNull(jsonSearchRequest.getAbandon()) && jsonSearchRequest.getAbandon()) {
            return null;
        }
        jsonSearchRequest.getRootNode().put(ElasticSearchConst.FROM, 0);
        jsonSearchRequest.getRootNode().put(ElasticSearchConst.SIZE, 1);
        List<T> list = list(jsonSearchRequest);
        return ListUtil.isEmpty(list) ? null : list.get(0);
    }

    @Override
    public <T> List<T> list(JsonSearchRequest<T> jsonSearchRequest) throws Exception {
        //如果放弃查询，返回为空
        if (Objects.nonNull(jsonSearchRequest.getAbandon()) && jsonSearchRequest.getAbandon()) {
            return new ArrayList<>();
        }
        String json = JsonUtil.toJson(jsonSearchRequest.getRootNode());
        log.debug("ElasticsearchDaoImpl.list:{}", json);
        SearchResponse searchResponse = getSearchResponse(jsonSearchRequest.getIndex(), json);
        return getResults(searchResponse, jsonSearchRequest);
    }

    private SearchResponse getSearchResponse(String index, String query) throws IOException {
        String endpoint = "/" + index + "/_search";
        Request request = new Request(HttpMethod.POST.name(), endpoint);
        request.setJsonEntity(query);
        Response response = RestHighLevelClientContextHolder.getRestHighLevelClientByKey().getLowLevelClient().performRequest(request);
        XContentParser parser = getXContentParser(response);
        return SearchResponse.fromXContent(parser);
    }

    private XContentParser getXContentParser(Response response) throws IOException {
        return XContentType.JSON.xContent().createParser(NamedXContentRegistry.EMPTY, DeprecationHandler.THROW_UNSUPPORTED_OPERATION, response.getEntity().getContent());
    }

    @Override
    public <T> PageResponseDto<T> page(JsonSearchRequest<T> jsonSearchRequest) throws Exception {
        //如果放弃查询，返回为空
        if (Objects.nonNull(jsonSearchRequest.getAbandon()) && jsonSearchRequest.getAbandon()) {
            return new PageResponseDto<>(0L, 0L, new ArrayList<>());
        }
        String json = JsonUtil.toJson(jsonSearchRequest.getRootNode());
        log.debug("ElasticsearchDaoImpl.page:{}", json);
        SearchResponse searchResponse = getSearchResponse(jsonSearchRequest.getIndex(), json);
        return getListPageResponseDTO(searchResponse, jsonSearchRequest.getSize(), jsonSearchRequest);

    }

    @Override
    public Map<String, Object> map(JsonSearchRequest jsonSearchRequest) throws Exception {
        //如果放弃查询，返回为空
        if (Objects.nonNull(jsonSearchRequest.getAbandon()) && jsonSearchRequest.getAbandon()) {
            return new HashMap<>();
        }
        String json = JsonUtil.toJson(jsonSearchRequest.getRootNode());
        log.debug("ElasticsearchDaoImpl.list:{}", json);
        String endpoint = "/" + jsonSearchRequest.getIndex() + "/_search";
        Request request = new Request(HttpMethod.POST.name(), endpoint);
        request.setJsonEntity(json);
        Response response = RestHighLevelClientContextHolder.getRestHighLevelClientByKey().getLowLevelClient().performRequest(request);
        XContentParser parser = getXContentParser(response);
        return parser.map();
    }

    private <T> PageResponseDto<T> getListPageResponseDTO(SearchResponse searchResponse, int pageSize, JsonSearchRequest<T> jsonSearchRequest) throws Exception {
        List<T> results = getResults(searchResponse, jsonSearchRequest);
        long totalCount = searchResponse.getHits().getTotalHits().value;
        long totalPages = totalCount % pageSize == 0 ? totalCount / pageSize : totalCount / pageSize + 1;
        return new PageResponseDto<>(totalCount, totalPages, results);
    }

    @Override
    public <T> Long count(JsonSearchRequest<T> jsonSearchRequest) throws Exception {
        //如果放弃查询，返回为空
        if (Objects.nonNull(jsonSearchRequest.getAbandon()) && jsonSearchRequest.getAbandon()) {
            return NumberUtils.LONG_ZERO;
        }
        String json = JsonUtil.toJson(jsonSearchRequest.getRootNode());
        log.debug("ElasticsearchDaoImpl.count:{}", json);
        SearchResponse searchResponse = getSearchResponse(jsonSearchRequest.getIndex(), json);
        jsonSearchRequest.setTook(searchResponse.getTook().getMillis());
        return searchResponse.getHits().getTotalHits().value;
    }

    @Override
    public <T> Boolean upsert(String index, String id, T entity) throws Exception {
        return upsert(index, id, entity, null);
    }

    @Override
    public <T> Boolean upsert(String index, String id, T entity, Consumer<T> dataHandleOnInsert) throws Exception {
        UpdateRequest updateRequest = getUpsertRequest(index, id, entity, dataHandleOnInsert);
        UpdateResponse response = RestHighLevelClientContextHolder.getRestHighLevelClientByKey().update(updateRequest, RequestOptions.DEFAULT);
        log.debug("upsert status: index:{}, id:{}, status:{}", index, id, response.status());
        return RestStatus.OK.equals(response.status()) || RestStatus.CREATED.equals(response.status());
    }

    @Override
    public <T> List<String> batchUpsert(String index, List<T> list) throws Exception {
        return batchUpsert(index, list, null);
    }

    /**
     * 组织UpsetRequest
     *
     * @param index              索引
     * @param id                 doc _id
     * @param entity             更新的实体
     * @param dataHandleOnInsert 插入时的数据处理
     * @param <T>                T
     * @return {@link UpdateRequest}
     */
    private <T> UpdateRequest getUpsertRequest(String index, String id, T entity, Consumer<T> dataHandleOnInsert) {
        UpdateRequest updateRequest = new UpdateRequest(index, id);
        String updateVal = JsonUtil.toJson(entity);
        updateRequest.doc(updateVal, XContentType.JSON);
        String insertVal = updateVal;
        if (dataHandleOnInsert != null) {
            dataHandleOnInsert.accept(entity);
            insertVal = JsonUtil.toJson(entity);
        }
        IndexRequest insertIndexRequest = new IndexRequest(index).id(id).source(insertVal, XContentType.JSON);
        updateRequest.upsert(insertIndexRequest);
        return updateRequest;
    }

    @Override
    public <T> List<String> batchUpsert(String index, List<T> list, Consumer<T> dataHandleOnInsert) throws Exception {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        BulkRequest bulkRequest = new BulkRequest();
        list.forEach(entity -> {
            String id = getIdFieldValue(entity);
            UpdateRequest updateRequest = getUpsertRequest(index, id, entity, dataHandleOnInsert);
            bulkRequest.add(updateRequest);
        });
        BulkResponse response = RestHighLevelClientContextHolder.getRestHighLevelClientByKey().bulk(bulkRequest, RequestOptions.DEFAULT);
        if (response.hasFailures()) {
            List<String> failureMessages = Arrays.stream(response.getItems())
                    .map(BulkItemResponse::getFailureMessage)
                    .filter(StringUtils::isNotBlank).collect(Collectors.toList());
            log.info("es batchUpsert error, failureMessages:{}", failureMessages);
            return failureMessages;
        }
        log.info("es batchUpsert totalCount：{}", list.size());
        return null;
    }

    @Override
    public <T> List<T> simpleGroupBy(SimpleGroupByJsonSearchRequest<T> jsonSearchRequest) throws Exception {
        String json = JsonUtil.toJson(jsonSearchRequest.getRootNode());
        log.debug("ElasticsearchDaoImpl.groupBy:{}", json);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        SearchModule searchModule = new SearchModule(Settings.EMPTY, false, Collections.emptyList());
        XContentParser parser = XContentFactory.xContent(XContentType.JSON).createParser(new NamedXContentRegistry(searchModule.getNamedXContents()), null, json);
        searchSourceBuilder.parseXContent(parser);
        SearchRequest request = new SearchRequest();
        request.indices(jsonSearchRequest.getIndex());
        request.source(searchSourceBuilder);
        SearchResponse searchResponse = RestHighLevelClientContextHolder.getRestHighLevelClientByKey().search(request, RequestOptions.DEFAULT);
        return getSimpleGroupByResults(searchResponse, jsonSearchRequest);
    }

    /**
     * 分组查询
     *
     * @param jsonScript 完整的查询json语句
     * @return {@link AggregationsResult}
     * @throws Exception ex
     */
    @Override
    public AggregationsResult aggs(String[] indexes, String jsonScript) throws Exception {
        log.debug("ElasticsearchDaoImpl.aggs:{}", jsonScript);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        SearchModule searchModule = new SearchModule(Settings.EMPTY, false, Collections.emptyList());
        XContentParser parser = XContentFactory.xContent(XContentType.JSON).createParser(new NamedXContentRegistry(searchModule.getNamedXContents()), null, jsonScript);
        searchSourceBuilder.parseXContent(parser);
        SearchRequest request = new SearchRequest();
        request.indices(indexes);
        request.source(searchSourceBuilder);
        SearchResponse searchResponse = RestHighLevelClientContextHolder.getRestHighLevelClientByKey().search(request, RequestOptions.DEFAULT);
        return toAggregationResult(searchResponse);
    }

    /**
     * SearchResponse 转换为 AggregationResult
     *
     * @param searchResponse response
     * @return {@link AggregationsResult}
     * @throws Exception ex
     */
    private AggregationsResult toAggregationResult(SearchResponse searchResponse) throws Exception {

        AggregationsResult aggregationResult = new AggregationsResult();

        Long totalCount = searchResponse.getHits().getTotalHits().value;
        Long took = searchResponse.getTook().millis();
        boolean timeout = searchResponse.isTimedOut();

        aggregationResult.setTotal(totalCount);
        aggregationResult.setTook(took);
        aggregationResult.setTimeout(timeout);

        Map<String, AggregationsResult.GroupBuckets> aggregationsMap = new HashMap<>();
        aggregationResult.setAggregations(aggregationsMap);
        Aggregations aggregations = searchResponse.getAggregations();
        doResolveGroupBucketDetail(aggregationsMap, aggregations);
        return aggregationResult;
    }

    /**
     * 解析Aggregations为{@link AggregationsResult.GroupBuckets}
     *
     * @param aggregationsMap
     * @param aggregations
     */
    public void doResolveGroupBucketDetail(Map<String, AggregationsResult.GroupBuckets> aggregationsMap, Aggregations aggregations) {
        Map<String, Aggregation> aggs = aggregations.asMap();
        /* 解析aggregations, 如:
          {
              "totalSmsCountPerHourGroup" : {
                  "buckets" : [
                    {
                      "key_as_string" : "2022-05-10 14",
                      "key" : 1652191200000,
                      "doc_count" : 32,
                      "smsSendResultGroup" : {
                        "doc_count_error_upper_bound" : 0,
                        "sum_other_doc_count" : 0,
                        "buckets" : [{
                            "key" : "3",
                            "doc_count" : 31,
                            "smsTypeGroup" : {
                              "doc_count_error_upper_bound" : 0,
                              "sum_other_doc_count" : 0,
                              "buckets" : []
                         }]
                    }
               }
          }
         */
        for (Map.Entry<String, Aggregation> it : aggs.entrySet()) {
            // 返回结果中的groupBuckets
            AggregationsResult.GroupBuckets resultGroupBuckets = new AggregationsResult.GroupBuckets();
            List<AggregationsResult.GroupBucketDetail> resultGroupBucketsDetails = new ArrayList<>();
            resultGroupBuckets.setBuckets(resultGroupBucketsDetails);
            aggregationsMap.put(it.getKey(), resultGroupBuckets);
            Aggregation agg = it.getValue();
            // 各agg分组的bucket数组
            List<?> buckets;
            if (agg instanceof ParsedStringTerms) {
                // terms分组处理
                buckets = ((ParsedStringTerms) agg).getBuckets();
                resultGroupBuckets.setDocCountErrorUpperBound(((ParsedStringTerms) agg).getDocCountError());
                resultGroupBuckets.setSumOtherDocCount(((ParsedStringTerms) agg).getSumOfOtherDocCounts());
            } else if (agg instanceof ParsedDateHistogram) {
                // date_histogram 分组处理
                buckets = ((ParsedDateHistogram) agg).getBuckets();
            } else if (agg instanceof ParsedSingleValueNumericMetricsAggregation) {
                // 单值数值型 分组处理2
                ParsedSingleValueNumericMetricsAggregation singleValue = ((ParsedSingleValueNumericMetricsAggregation) agg);
                String value = singleValue.getValueAsString();
                resultGroupBuckets.setSingleNumericValue(value);
                return;
            } else {
                // TODO 需要时扩展其他实现分组类型实现
                return;
            }
            for (Object temp : buckets) {
                MultiBucketsAggregation.Bucket bucket = (MultiBucketsAggregation.Bucket) temp;
                AggregationsResult.GroupBucketDetail resultDetail = new AggregationsResult.GroupBucketDetail(bucket.getKey(), bucket.getKeyAsString(), bucket.getDocCount());
                resultGroupBucketsDetails.add(resultDetail);
                Aggregations subAggs = bucket.getAggregations();
                if (subAggs != null) {
                    Map<String, AggregationsResult.GroupBuckets> subAggregationsMap = new HashMap<>();
                    resultDetail.setAggs(subAggregationsMap);
                    doResolveGroupBucketDetail(subAggregationsMap, subAggs);
                }
            }
        }
    }

    @Override
    public Boolean updateByQuery(String[] indexes, QueryBuilder query, Map<String, Object> updateMap) throws Exception {
        UpdateByQueryRequest request = new UpdateByQueryRequest(indexes);
        request.setQuery(query);
        request.setConflicts("proceed");
        request.setScript(buildUpdateScript(updateMap));
        log.info("updateByQuery request:{}", request);
        BulkByScrollResponse response = RestHighLevelClientContextHolder.getRestHighLevelClientByKey().updateByQuery(request, RequestOptions.DEFAULT);
        printFailures(response.getBulkFailures());
        return response.getTotal() > 0;
    }

    /**
     * 通过要修改的字段构建更新脚本
     * (脚本中使用参数化传值)
     *
     * @param updateMap 更新的参数, key: 更新的字段(嵌套字段可使用.分隔符), value: 更新的值
     * @return
     */
    private Script buildUpdateScript(Map<String, Object> updateMap) {
        StringBuilder sourceSb = new StringBuilder();
        Map<String, Object> paramMap = new HashMap<>();
        updateMap.forEach((k, v) -> {
            if (v != null) {
                sourceSb.append("ctx._source.").append(k).append("=params.").append(k).append(";");
//                toActualTypeValueForScript(v)
                paramMap.put(k, v);
            }
        });
        return new Script(ScriptType.INLINE, Script.DEFAULT_SCRIPT_LANG, sourceSb.toString(), paramMap);
    }

    /**
     * 打印失败内容
     *
     * @param failures {@link BulkItemResponse.Failure}
     */
    private List<String> printFailures(List<BulkItemResponse.Failure> failures) {
        if (CollectionUtils.isNotEmpty(failures)) {
            List<String> failureMessages = failures.stream()
                    .map(t -> t.getMessage())
                    .filter(StringUtils::isNotBlank).collect(Collectors.toList());
            log.info("es handle error, failureMessages:{}", failureMessages);
            return failureMessages;
        }
        return Collections.emptyList();
    }

    private <T> List<T> getSimpleGroupByResults(SearchResponse searchResponse, SimpleGroupByJsonSearchRequest<T> jsonSearchRequest) throws Exception {
        Aggregations aggregations = searchResponse.getAggregations();
        List<Aggregation> aggs = aggregations.asList();
        List<T> resultList = new ArrayList<>();
        Class<T> responseClass = jsonSearchRequest.getResponseClass();
        Objects.requireNonNull(responseClass, "responseClass 不能为空");
        List<String> fields = jsonSearchRequest.getGroupByAggFields();
        for (Aggregation aggregation : aggs) {
            ParsedStringTerms agg = (ParsedStringTerms) aggregation;
            for (int i1 = 0; i1 < agg.getBuckets().size(); i1++) {
                Terms.Bucket bucket = agg.getBuckets().get(i1);
                T obj = (T) ReflectUtils.newInstance(responseClass);
                for (String fieldName : fields) {
                    if (bucket == null) {
                        break;
                    }
                    Field field = ReflectionUtils.findField(responseClass, fieldName);
                    if (field != null) {
                        field.setAccessible(true);
                        field.set(obj, bucket.getKeyAsString());
                    }
                    if (bucket.getAggregations() != null && bucket.getAggregations().asList().size() > 0) {
                        List<? extends Terms.Bucket> buckets = ((ParsedTerms) (bucket.getAggregations().asList().get(0))).getBuckets();
                        if (CollectionUtils.isNotEmpty(buckets)) {
                            bucket = ((ParsedTerms) (bucket.getAggregations().asList().get(0))).getBuckets().get(0);
                        } else {
                            bucket = null;
                        }
                    } else {
                        bucket = null;
                    }
                }
                resultList.add(obj);
            }
        }
        return resultList;
    }

    /**
     * 查询结果转对象列表
     *
     * @param searchResponse    查询响应
     * @param jsonSearchRequest json查询请求
     * @param <T>               响应列表项泛型
     * @return 查询列表
     * @throws Exception 异常
     */
    private <T> List<T> getResults(SearchResponse searchResponse, JsonSearchRequest<T> jsonSearchRequest) throws Exception {

        Field idField = getIdField(jsonSearchRequest.getResponseClass());
        Field scoreField = getScoreField(jsonSearchRequest.getResponseClass());
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        List<T> list = new ArrayList<>();
        for (SearchHit searchHit : searchHits) {
            T obj = JsonUtil.toObject(searchHit.getSourceAsString(), jsonSearchRequest.getResponseClass());
            if (idField != null) {
                idField.set(obj, searchHit.getId());
            }
            if (scoreField != null) {
                scoreField.set(obj, searchHit.getScore());
            }
            list.add(obj);
        }
        jsonSearchRequest.setTook(searchResponse.getTook().getMillis());
        return list;
    }

    /**
     * 获取使用Id注解的属性对象
     *
     * @param classType 域所属的的类
     * @param <T>       泛型
     * @return 域对象
     */
    private <T> Field getIdField(Class<T> classType) {
        Field idField = null;
        Field[] fields = classType.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Id.class)) {
                field.setAccessible(true);
                idField = field;
            }
        }
        return idField;
    }

    /**
     * 获取使用Id注解的属性对象值
     *
     * @param <T> 泛型
     * @param t   对象
     * @return ID值
     */
    @SneakyThrows
    private <T, V> V getIdFieldValue(T t) {
        Field field = getIdField(t.getClass());
        if (field != null) {
            V val = (V) field.get(t);
            if (val == null) {
                val = getFieldValueByGetMethod(t, field);
            }
            return val;
        }
        return null;
    }

    /**
     * 通过Get方法获取参数值
     *
     * @param t     目标对象
     * @param field 目标字段
     * @param <T>   目标对象类型
     * @param <V>   返回值类型
     * @return
     */
    private <T, V> V getFieldValueByGetMethod(T t, Field field) {
        Method method = ReflectionUtils.findMethod(t.getClass(), "get" + StringUtils.capitalize(field.getName()));
        if (method != null) {
            try {
                return (V) method.invoke(t, null);
            } catch (Exception e) {
                log.error("通过Get方法获取参数值错误,方法调用异常,error: {}", e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * 获取使用Id注解的属性对象
     *
     * @param classType 域所属的的类
     * @param <T>       泛型
     * @return 域对象
     */
    private <T> Field getScoreField(Class<T> classType) {
        Field idField = null;
        Field[] fields = classType.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Score.class)) {
                field.setAccessible(true);
                idField = field;
            }
        }
        return idField;
    }

}