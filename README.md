# message-push 消息推送服务

基于Websocket的消息推送服务

Message push service by websocket.

## 1. 目标
实现消息实时推送通道。包括以下方面：
1. 点对点
   
   向单个客户端推送消息，如按别名推送(用别名来标识一个用户)，按客户端标识推送。

常见场景：

- 用户给用户发送站内信 (用户间互动)
- 管理员给单用户发送站内信
- 系统给单用户发送站内信

## 2. 技术方案
### 2.1. 方案概述
 
  本方案采用websocket 长连接的方式实现消息通道，使用Kafka消息队列实现消息的存储和分发，ElasticSearch保存消息发送记录。

### 2.2. 实现
![message-push-arch.png](docs%2Fimgs%2Fmessage-push-arch.png)

方案说明:

以上方案主要涉及2部分内容：
 
##### 1. 消息收集
   
- 通过对外提供推送消息报送的API接口，将待推送的业务数据发送到Kafka。
- 由推送服务消费者消费数据，并通过一致性哈希算法找到接受消息的客户端所在的websocket服务，并调用websocket服务的api接口发送消息。
- 同时在消费者中将推送消息持久化到ElasticSearch索引中。

##### 2. 消息推送

推送相关服务中包括：

- websocket网关: 
  
  1. 使用SpringGateway Netty websocket实现， 负责websocket路由，负载，转发)。
  2. 网关使用一致性哈希算法(基于UID),将同一个用户的websocket连接转发到同一个服务中。
  3. 监听nacos心跳，检测服务上下线状态，触发Rehash，剔除Rehash客户端(触发客户端重连)。（需考虑哪端维护Rehash状态,并处理websocket断开）
  4. 一致性哈希数据数据存储在Redis中。
                  
- websocket接口服务: 

  1. 使用netty实现，负责维持与客户端的连接，并且与客户端进行数据交互。
  2. 服务需注册到Nacos Server中，需要通过Nacos 感知服务上下线。
  3. 为消费者提供Http接口, 通过该接口向客户端发送数据。

- 消息消费者: 

  1. 负责消费Kafka的推送消息，并通过Redis中的一致性哈希数据找到对应服务地址，调用websocket接口服务api推送消息。
  2. Redis: 存储一致性哈希结果。
  3. Nacos Server: 服务注册中心。
  4. 一致性哈希的目的：为减少websocket接口服务上下线导致的客户端的重连。

注意点:
1. 网关多实例情况下的一致性哈希Rehash处理。

##### 3. 对外接口
推送平台提供2种类型的接口：
- 消息推送REST API接口

  使用Http Basic 认证，为调用方发放appId/appSecret作为调用权限验证。

  提供PUSH 推送接口，用于向客户端发送推送消息。

- 客户端websocket 连接服务

  提供websocket连接服务，定义websocket通道命令。

### 3. 接口文档

#### 3.1 Http API
1. 推送消息接口

```curl
curl --location 'https://push.kangspace.org/message-push/v1/push' \
--header 'Content-Type: application/json' \
--header 'Authorization: Basic YWFkNTVhNjNjYWRhNDAwNGE5YWZhZDU3YWMwMDlhMTY6ZDlhODZhNzJiY2U3MTVmNw==' \
--data '{
    "push_method": 1,
    "platform": "all",
    "audience": {
        "uids": [57098049]
    },
    "message": {
        "title": "title1",
        "content": "content1",
        "content_type": "content_type1",
        "extras": "123extras"
    }
}'
```

#### 3.2 Websocket API
```websocket
wss://push.kangspace.org/message-push/v1/message?Authorization=Bearer pc:ccd75db1e05dd67fd634b6dd9481840e&auth-app-id=DIY4856309478123
```
> 查询参数或请求头参数

| Query Params/<br/> Http Headers | value                                                                                           | -- |
|---------------------------------|-------------------------------------------------------------------------------------------------|----|
| Authorization                   | Bearer mobile:a939d574dfa281e0a9f886d4499fbc7c <br/> Bearer pc:ccd75db1e05dd67fd634b6dd9481840e | 
| auth-app-id                     | DIY4856309478123                                                                                | -- |

#### 3.3 测试调用
[push_test_20211106161914.mov](docs%2Fvideos%2Fpush_test_20211106161914.mov)

<video width="320" height="240" controls>
  <source src="docs/videos/push_test_20211106161914.mov" type="video/mp4">
  Your browser does not support the video tag.
</video>

### 4. 压测记录
测试时间: 2021-11-25

单Pod最高值: 2.5W Websocket连接
JVM 堆内存: 2G

message-push-ws-gateway 服务状态图:
![gateway-yace-1.png](docs%2Fimgs%2Fgateway-yace-1.png)
![gateway-yace-2.png](docs%2Fimgs%2Fgateway-yace-2.png)
![gateway-yace-3.png](docs%2Fimgs%2Fgateway-yace-3.png)

message-push-ws 服务状态图:
![push-ws-yace-1.png](docs%2Fimgs%2Fpush-ws-yace-1.png)
![push-ws-yace-2.png](docs%2Fimgs%2Fpush-ws-yace-2.png)
![push-ws-yace-3.png](docs%2Fimgs%2Fpush-ws-yace-3.png)