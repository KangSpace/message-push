# message-push-ws-gateway

消息推送websocket网关服务

## 项目提供服务

1. 对外提供Websocket服务,路由转发websocket到message-push-ws服务。

实现说明:

```
1. 网关启动时,从Redis 拉取 message-push-ws 服务列表的一致性Hash信息
2. 监听Nacos服务变更,若1中缓存的hash一致性数据不存在,则网关触发rehash(设置本地锁+Redis分布式锁),
3. 通过uid计算负载(使用一致性Hash,每个物理节点指定40个虚拟节点,每个物理节点最终产生160个虚拟节点)
4. 路由转发成功以后,将用户和物理节点关系保存到Redis

```

## 项目涉及中间件

    Spring-Gateway:
        websocket转发

    nacos(namespace):
        dev: kangspace_dev