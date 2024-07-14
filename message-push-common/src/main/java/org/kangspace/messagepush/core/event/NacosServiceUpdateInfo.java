package org.kangspace.messagepush.core.event;

import com.alibaba.nacos.api.naming.pojo.Instance;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kangspace.messagepush.core.util.MD5Util;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 服务变化信息
 *
 * @author kango2gler@gmail.com
 * @since 2021/11/2
 */
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NacosServiceUpdateInfo implements Serializable {
    /**
     * 服务ID
     */
    private String serviceId;
    /**
     * 实例服务信息
     */
    private List<Instance> instances;

    /**
     * 获取服务列表摘要字符串
     * 摘要逻辑: 1. instances 按 "ip:端口" Hash排序
     * 2. 转换为按,分割的字符串
     * 3. 对字符串取MD5
     *
     * @return
     * @see MD5Util#hashDigest(Collection)
     */
    public String getInstancesDigest() {
        if (CollectionUtils.isEmpty(instances)) {
            return null;
        }
        return MD5Util.hashDigest(instances.stream().map(t -> t.getIp() + ":" + t.getPort())
                .collect(Collectors.toList()));
    }
}
