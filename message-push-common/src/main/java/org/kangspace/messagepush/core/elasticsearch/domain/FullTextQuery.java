package org.kangspace.messagepush.core.elasticsearch.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.kangspace.messagepush.core.elasticsearch.config.GaussConfig;
import org.kangspace.messagepush.core.elasticsearch.config.ScriptConfig;
import org.kangspace.messagepush.core.util.ListUtil;

import java.io.Serializable;
import java.util.List;

/**
 * 全文检索查询对象
 *
 * @author kango2gler@gmail.com
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FullTextQuery implements Serializable {
    /**
     * 词库URL
     */
    private String ikUrl;
    /**
     * 搜索关键字，如果提供了关键字，但无分词结果，则不再进行全文检索；如果未提供关键字，则直接进行查询计算
     */
    private String keyword;
    /**
     * 全文检索项，一个字段一项
     */
    private List<FullTextItem> fullTextItems;
    /**
     * 是否需要打分。如果为true(默认值)，则全文检索并打分（分词分、高斯分、脚本分）；如果为false，则仅分词后拼如filter用于过滤查询
     */
    private boolean score = true;
    /**
     * 分词信息，构建对象时，如果传入则直接使用，如果没有传入则根据ikUrl和keyword调用词库获取。
     */
    private List<Word> words;

    /**
     * 构建单个属性的需要打分的全文检索对象
     *
     * @param ikUrl         ikUrl
     * @param fieldName     fieldName
     * @param gaussConfigs  gaussConfigs
     * @param scriptConfigs scriptConfigs
     */
    public FullTextQuery(String ikUrl, String fieldName, String keyword, List<GaussConfig> gaussConfigs, List<ScriptConfig> scriptConfigs) {
        this.ikUrl = ikUrl;
        this.keyword = keyword;
        this.fullTextItems = ListUtil.getList(new FullTextItem(fieldName, gaussConfigs, scriptConfigs));
    }

    /**
     * 构建单个属性的不需要打分的全文检索对象
     *
     * @param ikUrl     ikUrl
     * @param fieldName fieldName
     */
    public FullTextQuery(String ikUrl, String fieldName, String keyword) {
        this.ikUrl = ikUrl;
        this.keyword = keyword;
        this.fullTextItems = ListUtil.getList(new FullTextItem(fieldName));
        this.score = false;
    }

}
