package org.kangspace.messagepush.core.elasticsearch.config;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 脚本配置
 *
 * @author kango2gler@gmail.com
 */
@Setter
@Getter
public class ScriptConfig implements Serializable {

    /**
     * 脚本
     */
    private String script;
    /**
     * 权重
     */
    private Float weight;
}
