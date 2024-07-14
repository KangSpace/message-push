package org.kangspace.messagepush.core.constant;

/**
 * 常量类
 *
 * @author kango2gler@gmail.com
 * @date 2021-08-25
 */
public interface MessagePushConstants {

    /**
     * 服务路由前缀
     */
    String LB_PATH = "lb://";

    /**
     * 成功码
     */
    Integer SUCCESS_CODE = 1;
    /**
     * 失败码
     */
    Integer FAIL_CODE = 0;

    /**
     * Response body 属性
     */
    String CACHED_RESPONSE_BODY_ATTR = "customCachedResponseBody";

    /**
     * WebSocket 支持的协议
     */
    String[] WEBSOCKET_PROTOCOLS = {"ws", "wss"};

    /**
     * UID Http请求头Key
     */
    String HTTP_HEADER_UID_KEY = "uid";
    /**
     * auth-app-id Http请求头,值为生成Passport Token使用的AppId
     */
    String HTTP_HEADER_AUTH_APP_ID_KEY = "auth-app-id";

    /**
     * 请求参数 Exchange Attr Key
     */
    String EXCHANGE_ATTR_REQUEST_PARAM = "_requestParam";

    /**
     * 一致性HASH每个物理节点的虚拟节点数
     * (每个物理节点的总虚拟节点保持在100-200,默认取:160,此处默认值:40(每个虚拟节点会生成4个节点))
     */
    int NUMBER_OF_VIRTUAL_NODE = 40;

    /**
     * 消息推送Websocket服务名
     */
    String MESSAGE_WS_SERVICE_ID = "message-push-ws-microservice";

    /**
     * 用户Session绑定 ServerWebExchange属性key
     */
    String USER_SESSION_HOLDER_EXCHANGE_ATTR_KEY = "user-session-holder";

    /**
     * 用户Session绑定 ServerWebExchange属性key
     */
    String WEBSOCKET_TARGET_SERVICE_NODE_ATTR_KEY = "websocket-target-service-node";

    /**
     * 推送目标平台
     */
    enum PUSH_PLATFORM {
        ALL,
        H5,
        Android,
        iOS;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }
}
