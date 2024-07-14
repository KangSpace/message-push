package org.kangspace.messagepush.ws.gateway.hash;

import com.alibaba.nacos.api.naming.pojo.Instance;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.kangspace.messagepush.core.constant.MessagePushConstants;
import org.kangspace.messagepush.core.constant.RedisConstants;
import org.kangspace.messagepush.core.event.NacosServiceUpInfo;
import org.kangspace.messagepush.core.event.NacosServiceUpdateEvent;
import org.kangspace.messagepush.core.event.NacosServiceUpdateInfo;
import org.kangspace.messagepush.core.hash.ConsistencyHashing;
import org.kangspace.messagepush.core.hash.PhysicalNode;
import org.kangspace.messagepush.core.hash.VirtualNode;
import org.kangspace.messagepush.core.hash.repository.HashRouterRepository;
import org.kangspace.messagepush.core.redis.RedisService;
import org.kangspace.messagepush.core.util.MD5Util;
import org.kangspace.messagepush.ws.gateway.nacos.NacosNamingService;
import org.springframework.beans.BeansException;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <pre>
 * Redis一致性Hash路由数据处理类
 * Gateway网关处理逻辑:
 * 1. Gateway网关启动时,从Redis拉取Hash环数据,
 * 拉取Hash环境数据后,校验Hash环数据是否正常(即是否时最新服务列表的Hash数据),
 * 正常: 继续
 * 不正常: 说明Redis数据是旧的(也就是说当前Gateway网关是第一个启动的网关实例),则根据最新服务列表更新Hash环数据,并更新Redis
 * 2. 监听Nacos message-push-ws服务变更事件,服务变更时,所有Gateway网关实例处理各自实例内Hash环中节点的上线,下线操作,并更新本地Hash环数据,
 * 2.1 本地更新成功后,由其中的一个Gateway网关实例更新Redis Hash环数据(并更新本地Hash环摘要)
 * Gateway网关更新Redis Hash环数据时先检查摘要是否一致,不一致再更新
 * 2.2 节点下线时,只更新Hash环数据
 * 节点上线时,断开部分rehash用户连接(通过步骤3操作)
 * 3. Gateway网关与message-push-ws建立连接后，各Gateway网关缓存用户与message-push-ws机器连接的关系,
 * 当2中节点上线时,计算需要rehash的用户,并断开相关连接
 *
 * message-push-ws 服务,应用,UID,Session关系
 * 1. 用户登录后服务本地维护当前实例和用户的关系列表
 *
 * message-push-consumer服务
 * 1. 定时更新Gateway网关保存的Hash环数据,
 * 2. 收到数据后计算Hash环数据找到服务节点,推送数据
 * </pre>
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/28
 */
@Slf4j
@Data
public class RedisHashRouterRepository<T> implements HashRouterRepository<T>, SmartApplicationListener,
        ApplicationContextAware, ApplicationEventPublisher {
    private final RedisService redisService;
    private ApplicationContext applicationContext;
    private ConsistencyHashing<T> consistencyHashing;
    private NacosNamingService nacosNamingService;

    public RedisHashRouterRepository(RedisService redisService, NacosNamingService nacosNamingService) {
        this.redisService = redisService;
        this.nacosNamingService = nacosNamingService;
    }

    @Override
    public ConsistencyHashing<T> get() {
        return consistencyHashing;
    }

    @PostConstruct
    @Override
    public ConsistencyHashing<T> init() {
        log.info("Redis Hash环数据初始化: 开始.");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        setConsistencyHashing(new ConsistencyHashing<>(MessagePushConstants.NUMBER_OF_VIRTUAL_NODE, Collections.emptyList()));
        // 1. 从Redis中获取 Hash环数据
        final Map<String, VirtualNode> ring = redisService.hGetAll(RedisConstants.MESSAGE_PUSH_HASH_RING_KEY, VirtualNode.class);
        // 获取实际服务列表
        List<String> actualServices = getAllInstances();
        ConsistencyHashing hashRouter;
        // 2. 初始化到 ConsistencyHashing 中
        if (CollectionUtils.isEmpty(ring)) {
            log.warn("Redis Hash环数据初始化: 一致性HASH数据不存在,进行rehashing");
            hashRouter = rehash(actualServices);
        } else {
            // 检查Hash环数据是否正确,不正确,则Rehash
            hashRouter = compareHashDataAndRehash(new ConsistencyHashing(ring, MessagePushConstants.NUMBER_OF_VIRTUAL_NODE), actualServices);
        }
        stopWatch.stop();
        log.info("Redis Hash环数据初始化: 结束. 新Hash环虚拟节点数:[{}],耗时:{}ms", hashRouter.getVirtualNodeCount(), stopWatch.getTotalTimeMillis());
        setConsistencyHashing(hashRouter);
        return hashRouter;
    }

    @Override
    public boolean store(ConsistencyHashing<T> hashRouter) {
        String redisKey = RedisConstants.MESSAGE_PUSH_HASH_RING_KEY;
        String lockKey = RedisConstants.MESSAGE_PUSH_HASH_RING_STORE_LOCK_KEY;
        long lockExpireSec = RedisConstants.MESSAGE_PUSH_HASH_RING_STORE_LOCK_EXPIRE_SEC;
        // 同步锁,同一时间只有1个网关实例更新Hash数据
        return redisService.lock(lockKey, lockExpireSec, () -> {
            redisService.del(redisKey);
            if (!hashRouter.getRing().isEmpty()) {
                NavigableMap<Long, VirtualNode<T>> tempMap = hashRouter.getRing().descendingMap();
                Map<String, Object> storeMap = new HashMap<>(tempMap.size());
                tempMap.entrySet().forEach(t -> storeMap.put(t.getKey().toString(), t.getValue()));
                redisService.hMSet(redisKey, storeMap);
            }
            log.info("Redis Hash环数据维护: 更新Redis Hash环数据成功!");
        });
    }

    /**
     * Rehash 处理服务在当前网关实例的上下线
     *
     * @param services
     * @return
     */
    @Override
    public ConsistencyHashing<T> rehash(List<String> services) {
        log.info("Redis Hash环数据维护: rehash 开始");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<PhysicalNode<T>> currPhysicalNodes = this.getConsistencyHashing().getPhysicalNodes();
        List<String> physicalNodeNodes = currPhysicalNodes.stream().map(t -> t.getNode()).collect(Collectors.toList());
        List<String> upServices = services.stream().filter(t -> !physicalNodeNodes.contains(t)).collect(Collectors.toList());
        List<String> downServices = physicalNodeNodes.stream().filter(t -> !services.contains(t)).collect(Collectors.toList());
        log.info("Redis Hash环数据维护: rehash,上线的服务:[{}],下线的服务:[{}]", upServices, downServices);
        // 存在上线的服务,找出需要断开连接的用户
        if (!CollectionUtils.isEmpty(upServices)) {
            upServices.forEach(node -> {
                this.getConsistencyHashing().addNode(new PhysicalNode<>(this.getConsistencyHashing().getNodeHash(node), node));
            });
            // 触发服务上线事件:
            publishServerUpEvent();
        }
        // 存在下线的服务,则直接删除物理节点
        if (!CollectionUtils.isEmpty(downServices) && !CollectionUtils.isEmpty(currPhysicalNodes)) {
            downServices.forEach(node -> {
                this.getConsistencyHashing().removeNode(new PhysicalNode<>(this.getConsistencyHashing().getNodeHash(node), node));
            });
        }
        // 保存rehash结果
        store(this.getConsistencyHashing());
        stopWatch.stop();
        log.info("Redis Hash环数据维护: rehash 结束,耗时:{}ms", stopWatch.getTotalTimeMillis());
        return this.getConsistencyHashing();
    }

    /**
     * 发布服务上线事件
     */
    public void publishServerUpEvent() {
        log.info("Nacos服务动态监听: 发布服务上线事件!");
        NacosServiceUpdateEvent<NacosServiceUpInfo> serviceUpEvent =
                new NacosServiceUpdateEvent<>(new NacosServiceUpInfo(this.getConsistencyHashing()));
        this.publishEvent((Object) serviceUpEvent);
    }

    @Override
    public boolean compareHashData(ConsistencyHashing<T> hashRouter, List<String> actualServices) {
        String oldHashRoute = hashRouter.getPhysicalNodesDigest();
        String newHashRoute = MD5Util.hashDigest(actualServices);
        return !newHashRoute.equals(oldHashRoute) || (!hashRouter.getRing().isEmpty() && CollectionUtils.isEmpty(actualServices));
    }

    @Override
    public ConsistencyHashing<T> compareHashDataAndRehash(ConsistencyHashing<T> hashRouter, List<String> actualServices) {
        if (compareHashData(hashRouter, actualServices)) {
            return rehash(actualServices);
        }
        return hashRouter;
    }


    /**
     * 获取所有实例IP:端口
     *
     * @return [ip:端口]列表
     */
    private List<String> getAllInstances() {
        return getIpPortListByServiceInstances(this.nacosNamingService.getAllInstances(MessagePushConstants.MESSAGE_WS_SERVICE_ID));
    }

    /**
     * 通过{@link ServiceInstance}列表获取IP:端口列表
     *
     * @param instances
     * @return
     */
    private List<String> getIpPortListByServiceInstances(List<ServiceInstance> instances) {
        return instances.stream().map(t -> t.getHost() + ":" + t.getPort()).collect(Collectors.toList());
    }

    /**
     * 通过{@link Instance}列表获取IP:端口列表
     *
     * @param instances
     * @return
     */
    private List<String> getIpPortListByInstances(List<Instance> instances) {
        return instances.stream().map(t -> t.getIp() + ":" + t.getPort()).collect(Collectors.toList());
    }

    /**
     * 设定支持的事件类型
     *
     * @param aClass ApplicationEvent
     * @return boolean
     */
    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> aClass) {
        return NacosServiceUpdateEvent.class.isAssignableFrom(aClass);
    }

    /**
     * 设定事件对象为NacosServerUpdateInfo
     *
     * @param sourceType NacosServerUpdateInfo
     * @return boolean
     */
    @Override
    public boolean supportsSourceType(Class<?> sourceType) {
        return NacosServiceUpdateInfo.class.isAssignableFrom(sourceType);
    }

    /**
     * 服务变化事件
     *
     * @param event ApplicationEvent
     */
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        log.info("Redis Hash环数据维护: Nacos服务更新事件,处理开始. event:{}", event);
        NacosServiceUpdateEvent<NacosServiceUpdateInfo> serviceUpdateEvent = (NacosServiceUpdateEvent<NacosServiceUpdateInfo>) event;
        if (serviceUpdateEvent != null && serviceUpdateEvent.getSource() != null) {
            List<String> actualServices = getIpPortListByInstances(((NacosServiceUpdateInfo) (serviceUpdateEvent.getSource())).getInstances());
            // 检查服务是否需要Rehash
            this.compareHashDataAndRehash(this.consistencyHashing, actualServices);
        }
        log.info("Redis Hash环数据维护: Nacos服务更新事件,处理结束, 当前服务数:[{}]", this.consistencyHashing.getPhysicalNodes().size());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void publishEvent(Object event) {
        applicationContext.publishEvent(event);
    }
}
