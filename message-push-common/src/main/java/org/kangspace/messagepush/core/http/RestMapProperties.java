package org.kangspace.messagepush.core.http;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * RestTemplate 连接池配置类
 *
 * @author kango2gler@gmail.com
 */
@Data
@Component
@ConfigurationProperties("rest")
public class RestMapProperties {

    /**
     * 是否开启rest组件
     */
    private Boolean restEnable;

    /**
     * 多连接池支持配置Map<连接池名称,连接池配置信息>
     */
    private Map<String, RestProperties> rest = new HashMap<>();

}
