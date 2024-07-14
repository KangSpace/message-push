package org.kangspace.messagepush.core.elasticsearch;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestHighLevelClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Es请求客户端上下文
 *
 * @author kango2gler@gmail.com
 */
@Slf4j
public class RestHighLevelClientContextHolder {

    /**
     * 存放所有的Es请求客户端
     */
    private static final Map<String, RestHighLevelClient> REST_HIGH_LEVEL_CLIENT_MAP = new ConcurrentHashMap<>();

    /**
     * 线程级别的私有变量
     */
    private static final ThreadLocal<String> HOLDER = ThreadLocal.withInitial(() -> ElasticSearchConst.DEFAULT);

    /**
     * 根据名字获取当前线程的请求客户端
     *
     * @return 数据源
     */
    public static RestHighLevelClient getRestHighLevelClientByKey() {
        return REST_HIGH_LEVEL_CLIENT_MAP.get(HOLDER.get());
    }

    /**
     * 获取当前线程的请求客户端的Key
     *
     * @return 数据源
     */
    public static String getRestHighLevelClientKey() {
        return HOLDER.get();
    }

    /**
     * 设置当前线程的请求客户端
     *
     * @param restHighLevelClientKey 客户端key
     */
    public static void setRestHighLevelClientKey(String restHighLevelClientKey) {
        log.info("切换至{}RestHighLevelClient", restHighLevelClientKey);
        HOLDER.set(restHighLevelClientKey);
    }

    /**
     * 设置之前RestHighLevelClient一定要先移除
     */
    public static void removeRestHighLevelClientKey() {
        HOLDER.remove();
    }

    /**
     * 判断指定的RestHighLevelClient当前是否存在
     *
     * @param restHighLevelClientKey 连接名
     * @return true存在；false不存在
     */
    public static boolean containsRestHighLevelClientKey(String restHighLevelClientKey) {
        return REST_HIGH_LEVEL_CLIENT_MAP.containsKey(restHighLevelClientKey);
    }

    /**
     * 添加请求客户端
     *
     * @param restHighLevelClientKey 客户端key
     * @param restHighLevelClient    客户端
     */
    public static void addRestHighLevelClient(String restHighLevelClientKey, RestHighLevelClient restHighLevelClient) {
        REST_HIGH_LEVEL_CLIENT_MAP.put(restHighLevelClientKey, restHighLevelClient);
    }

}
