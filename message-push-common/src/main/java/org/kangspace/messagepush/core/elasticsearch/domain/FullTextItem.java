package org.kangspace.messagepush.core.elasticsearch.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.kangspace.messagepush.core.elasticsearch.config.GaussConfig;
import org.kangspace.messagepush.core.elasticsearch.config.ScriptConfig;

import java.io.Serializable;
import java.util.List;

/**
 * 全文检索查询属性对象
 *
 * @author kango2gler@gmail.com
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FullTextItem implements Serializable {

    /**
     * 查询属性名
     */
    private String fieldName;

    /**
     * 整体权重，影响拼接出来的结果，默认为1
     */
    private Float weight = 1F;

    /**
     * 高斯衰减函数列表
     */
    private List<GaussConfig> gaussConfigs;

    /**
     * 脚本函数列表
     */
    private List<ScriptConfig> scriptConfigs;

    public FullTextItem(String fieldName) {
        this.fieldName = fieldName;
    }

    public FullTextItem(String fieldName, List<GaussConfig> gaussConfigs, List<ScriptConfig> scriptConfigs) {
        this.fieldName = fieldName;
        this.gaussConfigs = gaussConfigs;
        this.scriptConfigs = scriptConfigs;
    }
}
