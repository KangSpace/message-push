package org.kangspace.messagepush.core.elasticsearch;


import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.index.query.QueryBuilder;
import org.kangspace.messagepush.core.dto.page.PageResponseDto;
import org.kangspace.messagepush.core.elasticsearch.request.*;
import org.kangspace.messagepush.core.elasticsearch.response.AggregationsResult;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * ES方法Dao层接口
 *
 * @author kango2gler@gmail.com
 */
public interface ElasticsearchManager {

    /**
     * 判断指定的索引名是否存在
     *
     * @param index 索引名
     * @return 存在：true; 不存在：false;
     * @throws IOException Io异常
     */
    Boolean existsIndex(String index) throws IOException;

    /**
     * 创建索引
     *
     * @param jsonIndexRequest json创建索引对象
     * @return 创建索引是否成功
     * @throws Exception 异常
     */
    Boolean createIndex(JsonCreateIndexRequest jsonIndexRequest) throws Exception;

    /**
     * 删除索引(慎用)
     *
     * @param index 索引名
     * @return 删除索引是否成功
     * @throws IOException 异常
     */
    Boolean deleteIndex(String index) throws IOException;

    /**
     * 判断指定别名是否存在
     *
     * @param alias 别名
     * @return true: 存在, false: 不存在
     * @throws IOException IO异常
     */
    Boolean existsAlias(String alias) throws IOException;

    /**
     * 别名操作
     *
     * @param jsonAliasActionsRequest 别名操作对象
     * @return true:成功, false:失败
     * @throws IOException
     */
    Boolean aliasAction(JsonAliasActionsRequest jsonAliasActionsRequest) throws IOException;

    /**
     * 别名滚动索引
     *
     * @param rolloverRequest 滚动请求
     * @return true: 需要滚动/滚动成功,false: 无需滚动
     * @throws IOException IO异常
     */
    Boolean rollover(PlainRolloverRequest rolloverRequest) throws IOException;

    /**
     * 插入数据
     *
     * @param index  索引
     * @param entity 插入实体
     * @param <T>    实体泛型
     * @return 插入是否成功
     * @throws Exception 异常
     */
    <T> Boolean insert(String index, T entity) throws Exception;

    /**
     * 批量插入数据
     *
     * @param index 索引
     * @param list  插入列表
     * @param <T>   实体泛型
     * @return 插入是否成功
     * @throws Exception 异常
     */
    <T> Boolean insert(String index, List<T> list) throws Exception;

    /**
     * 批量插入数据，有返回值
     *
     * @param index
     * @param list
     * @return
     * @throws Exception
     */
    <T> List<String> batchInsert(String index, List<T> list) throws Exception;

    /**
     * 批量插入数据
     *
     * @param index 索引
     * @param idKey es id主键
     * @param list  插入列表
     * @return 插入是否成功
     * @throws Exception 异常
     */
    Boolean insert(String index, String idKey, List<Map<String, Object>> list) throws Exception;

    /**
     * 拼装插入请求对象
     *
     * @param index  索引
     * @param entity 实体
     * @param <T>    实体泛型
     * @return 插入是否成功
     * @throws IOException            IO异常
     * @throws IllegalAccessException 访问权限异常
     */
    <T> IndexRequest getIndexRequest(String index, T entity) throws IOException, IllegalAccessException;

    /**
     * 更新数据
     *
     * @param index 索引
     * @param id    id
     * @param map   更新的数据
     * @throws Exception 异常
     */
    Boolean update(String index, String id, Map<String, Object> map) throws Exception;

    /**
     * 更新数据
     *
     * @param index  索引
     * @param id     id
     * @param entity 更新的实体类数据，不为null的属性都参与更新
     * @return 更新是否成功
     * @throws Exception 异常
     */
    <T> Boolean update(String index, String id, T entity) throws Exception;

    /**
     * 更新数据
     *
     * @param index 索引
     * @param ids   ids
     * @param list  更新的数据列表
     * @return 更新是否成功
     * @throws Exception 异常
     */
    Boolean update(String index, List<String> ids, List<Map<String, Object>> list) throws Exception;

    /**
     * 更新数据
     *
     * @param index 索引
     * @param ids   ids
     * @param list  更新的数据列表
     * @return 更新是否成功
     * @throws Exception 异常
     */
    <T> List<String> batchUpdate(String index, List<String> ids, List<Map<String, Object>> list) throws Exception;

    /**
     * 删除数据
     *
     * @param index 索引
     * @param id    es id
     * @return 删除是否成功
     * @throws Exception 异常
     */
    Boolean delete(String index, String id) throws Exception;

    /**
     * 批量删除数据
     *
     * @param index 索引
     * @param ids   es id列表
     * @return 删除是否成功
     * @throws Exception 异常
     */
    Boolean delete(String index, List<String> ids) throws Exception;

    /**
     * 获取一个ES数据对象
     *
     * @param index     索引
     * @param id        id
     * @param classType 返回对象class对象
     * @param <T>       返回对象泛型
     * @return 结果对象
     * @throws Exception 异常
     */
    <T> T get(String index, String id, Class<T> classType) throws Exception;

    /**
     * 获取一个ES数据对象
     *
     * @param index     索引
     * @param id        id
     * @param fields    需要返回的字段
     * @param classType 返回对象class对象
     * @param <T>       返回对象泛型
     * @return 结果对象
     * @throws Exception 异常
     */
    <T> T get(String index, String id, List<String> fields, Class<T> classType) throws Exception;

    /**
     * 获取一个ES数据对象
     *
     * @param jsonSearchRequest json查询对象
     * @param <T>               返回对象泛型
     * @return 结果对象
     * @throws Exception 异常
     */
    <T> T get(JsonSearchRequest<T> jsonSearchRequest) throws Exception;

    /**
     * 基于json脚本查询
     *
     * @param jsonSearchRequest json查询对象
     * @param <T>               返回对象泛型
     * @return 列表对象
     * @throws Exception 异常
     */
    <T> List<T> list(JsonSearchRequest<T> jsonSearchRequest) throws Exception;

    /**
     * 基于json脚本查询
     *
     * @param jsonSearchRequest json查询对象
     * @param <T>               返回对象泛型
     * @return 分页对象
     * @throws Exception 异常
     */
    <T> PageResponseDto<T> page(JsonSearchRequest<T> jsonSearchRequest) throws Exception;

    /**
     * 基于json脚本查询，并返回ES返回结果map
     *
     * @param jsonSearchRequest json查询对象
     * @return ES返回结果map
     * @throws Exception 异常
     */
    Map<String, Object> map(JsonSearchRequest jsonSearchRequest) throws Exception;

    /**
     * 查询数量
     *
     * @param jsonSearchRequest json查询对象
     * @return 数量
     * @throws Exception 异常
     */
    <T> Long count(JsonSearchRequest<T> jsonSearchRequest) throws Exception;

    /**
     * 批量更新/插入数据
     *
     * @param index 索引
     * @param list  更新的数据列表
     * @param <T>   T 中使用{@link org.yaml.snakeyaml.events.Event.ID}指定为_doc的_id
     * @return 更新是否成功
     * @throws Exception 异常
     */
    <T> List<String> batchUpsert(String index, List<T> list) throws Exception;

    /**
     * 批量更新/插入数据
     *
     * @param index              索引
     * @param list               更新的数据列表
     * @param dataHandleOnInsert 插入时的数据处理
     * @return 更新是否成功
     * @throws Exception 异常
     */
    <T> List<String> batchUpsert(String index, List<T> list, Consumer<T> dataHandleOnInsert) throws Exception;

    /**
     * 更新/插入数据
     *
     * @param index  索引
     * @param id     id
     * @param entity 更新的实体类数据，不为null的属性都参与更新
     * @return 更新是否成功
     * @throws Exception 异常
     */
    <T> Boolean upsert(String index, String id, T entity) throws Exception;

    /**
     * 更新/插入数据
     *
     * @param index              索引
     * @param id                 id
     * @param entity             更新的实体类数据，不为null的属性都参与更新
     * @param dataHandleOnInsert 插入时的数据处理
     * @return 更新是否成功
     * @throws Exception 异常
     */
    <T> Boolean upsert(String index, String id, T entity, Consumer<T> dataHandleOnInsert) throws Exception;

    /**
     * 简单分组查询
     * 如:
     * 请求:
     * <code>
     * {
     * "size": 0,
     * "query": {
     * "match_all": {}
     * },
     * "aggs": {
     * "groupByIndex": {
     * "terms": {
     * "field": "index",
     * "size": 1000
     * },
     * "aggs": {
     * "groupByTime": {
     * "terms": {
     * "field": "time",
     * "size": 1
     * }
     * }
     * }
     * }
     * }
     * }
     * 响应:
     * {
     * "took" : 2,
     * "timed_out" : false,
     * "_shards" : {...},
     * "hits" : {...},
     * "aggregations" : {
     * "groupByIndex" : {
     * "doc_count_error_upper_bound" : 0,
     * "sum_other_doc_count" : 0,
     * "buckets" : [
     * {
     * "key" : "bcl-test-batch-upsert-index",
     * "doc_count" : 10,
     * "groupByTime" : {
     * "doc_count_error_upper_bound" : 0,
     * "sum_other_doc_count" : 9,
     * "buckets" : [
     * {
     * "key" : 1643018137436,
     * "doc_count" : 1
     * }
     * ]
     * }
     * }
     * ]
     * }
     * }
     * }
     * </code>
     * 返回:
     * [{
     * "index":bcl-test-batch-upsert-index,
     * "time": 1643018137436
     * }]
     * <code>
     *
     * </code>
     *
     * @param jsonSearchRequest
     * @param <T>
     * @return T 对象列表
     * @throws Exception 异常
     */
    <T> List<T> simpleGroupBy(SimpleGroupByJsonSearchRequest<T> jsonSearchRequest) throws Exception;

    /**
     * 分组查询
     *
     * @param jsonScript 完整的查询json语句
     * @return {@link AggregationsResult}
     * @throws Exception ex
     */
    AggregationsResult aggs(String[] indexes, String jsonScript) throws Exception;

    /**
     * 通过查询更新数据
     *
     * @param indexes   索引列表
     * @param query     查询条件 {@link QueryBuilder}
     * @param updateMap 更新的参数, key: 更新的字段, value: 更新的值
     * @return 更新是否成功
     * @throws Exception ex
     */
    Boolean updateByQuery(String[] indexes, QueryBuilder query, Map<String, Object> updateMap) throws Exception;
}