# message-push-ws

消息推送Websocket服务

## 项目提供服务

1. 提供websocket连接服务,并在服务中维护用户和信息
    - 创建Websocket服务,定义WebsocketHandler处理,处理PING,PONG请求
   > 调用websocket的请求,请求头中必须包含当前用户uid(由网关转发时添加该请求头)
    - 处理连接信息,注册用户和session
    - 提供Http接口,添加持续输出数据到Websocket Session
2. 提供Http接口用于websocket推送

Websocket协议消息格式:
PING PONG TEXT BINARY

## 项目涉及中间件

    nacos(namespace):
        dev: kangspace_dev