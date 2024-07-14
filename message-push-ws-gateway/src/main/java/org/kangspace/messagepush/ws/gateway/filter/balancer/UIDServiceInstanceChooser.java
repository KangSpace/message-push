package org.kangspace.messagepush.ws.gateway.filter.balancer;

import lombok.extern.slf4j.Slf4j;
import org.kangspace.messagepush.core.constant.MessagePushConstants;
import org.kangspace.messagepush.core.hash.ConsistencyHashing;
import org.kangspace.messagepush.core.hash.VirtualNode;
import org.kangspace.messagepush.ws.gateway.model.MessageRequestParam;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;

/**
 * 根据用户ID选择服务实例
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/27
 */
@Slf4j
public class UIDServiceInstanceChooser implements LbServiceInstanceChooser {
    /**
     * 一致性hash路由
     */
    private ConsistencyHashing<ServiceInstance> hashRouter;

    public UIDServiceInstanceChooser(ConsistencyHashing<ServiceInstance> hashRouter) {
        this.hashRouter = hashRouter;
    }

    @Override
    public ServiceInstance choose(String serviceId, ServerWebExchange exchange, List<ServiceInstance> instances) {
        if (CollectionUtils.isEmpty(instances)) {
            log.warn("用户ID服务实例选择: No servers available for service: " + serviceId);
            return null;
        }
        // 获取必要的请求参数
        MessageRequestParam requestParam = exchange.getAttribute(MessagePushConstants.EXCHANGE_ATTR_REQUEST_PARAM);
        String uid = requestParam != null ? requestParam.getUid() : "";
        // 通过uid获取目标Service节点
        VirtualNode<ServiceInstance> node = hashRouter.getVirtualNode(uid);
        if (node == null) {
            log.warn("用户ID服务实例选择: 当前Hash环无可用服务实例; serviceId: [{}],uid: [{}], hashNode:[{}] serviceInstances:[{}]",
                    serviceId, uid, node, instances);
            return null;
        }
        String expectNode = node.getPhysicalNode().getNode();
        // 筛选出目标Service服务
        ServiceInstance expectServiceInstance = instances.stream()
                .filter(t -> expectNode.equals(t.getHost() + ":" + t.getPort()))
                .findFirst().orElse(null);
        boolean matched = expectServiceInstance != null;
        if (!matched) {
            log.warn("用户ID服务实例选择: 通过用户uid获取服务失败,没有匹配的服务实例; serviceId: [{}],uid: [{}], hashNode:[{}] serviceInstances:[{}]",
                    serviceId, uid, node, instances);
            return null;
        }
        return expectServiceInstance;
    }
}
