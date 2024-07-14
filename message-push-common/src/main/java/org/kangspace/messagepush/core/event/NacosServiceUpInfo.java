package org.kangspace.messagepush.core.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kangspace.messagepush.core.hash.ConsistencyHashing;

import java.io.Serializable;

/**
 * 服务上线信息(用于Rehash时客户端剔除下线)
 *
 * @author kango2gler@gmail.com
 * @since 2021/11/2
 */
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NacosServiceUpInfo implements Serializable {
    private ConsistencyHashing consistencyHashing;
}
