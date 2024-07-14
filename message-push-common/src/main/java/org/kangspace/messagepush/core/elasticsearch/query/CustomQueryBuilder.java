package org.kangspace.messagepush.core.elasticsearch.query;


import org.kangspace.messagepush.core.elasticsearch.request.JsonSearchRequest;

/**
 * 自定义查询语句构建器接口
 *
 * @author kango2gler@gmail.com
 */
public interface CustomQueryBuilder<T> {

    /**
     * 拼装自定义查询语句
     *
     * @param jsonSearchRequest elasticsearch.request.JsonSearchRequest
     * @param query             query
     */
    void handler(JsonSearchRequest jsonSearchRequest, T query);
}
