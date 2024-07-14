package org.kangspace.messagepush.core.elasticsearch.request;


import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.kangspace.messagepush.core.elasticsearch.ElasticSearchConst;
import org.kangspace.messagepush.core.util.JsonUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * es索引创建对象，用于创建索引时使用
 *
 * @author kango2gler@gmail.com
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class JsonCreateIndexRequest extends BaseRequest {

    /**
     * 分片数，一般同节点数
     */
    private Integer numberOfShards;

    /**
     * 副本数，一般为1
     */
    private Integer numberOfReplicas;

    /**
     * 别名
     */
    private List<String> aliases;

    /**
     * 映射<类型,<字段名，字段类型数据类型字符串 或 ObjectNode对象>>
     */
    private Map<String, Object> mappings;


    /**
     * 构建创建索引对象
     *
     * @param index            索引名
     * @param numberOfShards   分片数，一般同节点数
     * @param numberOfReplicas 副本数，一般为1
     * @param mappings         别名，支持多个
     */
    public JsonCreateIndexRequest(String index, Integer numberOfShards, Integer numberOfReplicas, Map<String, Object> mappings) {
        super(index);
        this.numberOfShards = numberOfShards;
        this.numberOfReplicas = numberOfReplicas;
        this.mappings = mappings;
        build();
    }

    /**
     * 构建创建索引对象
     *
     * @param index            索引名
     * @param numberOfShards   分片数，一般同节点数
     * @param numberOfReplicas 副本数，一般为1
     * @param mappings         映射<类型,<字段名，字段类型数据类型字符串 或 ObjectNode对象>>
     * @param aliases          别名，支持多个
     */
    public JsonCreateIndexRequest(String index, Integer numberOfShards, Integer numberOfReplicas, Map<String, Object> mappings, List<String> aliases) {
        super(index);
        this.numberOfShards = numberOfShards;
        this.numberOfReplicas = numberOfReplicas;
        this.mappings = mappings;
        this.aliases = aliases;
        build();
    }

    /**
     * 构建查询语句
     */
    public void build() {
        //索引状态为开启
        //super.getRootNode().put(ElasticSearchConst.STATE, ElasticSearchConst.OPEN);

        //分片数和副本数
        super.getRootNode().set(ElasticSearchConst.SETTINGS, JsonUtil.createObjectNode().put(ElasticSearchConst.NUMBER_OF_SHARDS, numberOfShards).put(ElasticSearchConst.NUMBER_OF_REPLICAS, numberOfReplicas));

        //别名
        if (aliases != null && aliases.size() > 0) {
            ObjectNode aliasesNode = super.getRootNode().putObject(ElasticSearchConst.ALIASES);
            aliases.forEach(alias -> aliasesNode.set(alias, JsonUtil.createObjectNode()));
        }

        //映射
        ObjectNode mappingsNode = super.getRootNode().putObject(ElasticSearchConst.MAPPINGS);
        ObjectNode propertiesNode = mappingsNode.putObject(ElasticSearchConst.PROPERTIES);
        Optional.ofNullable(mappings)
                .ifPresent(v -> v.forEach((fieldName, fieldType) -> {
                            if (fieldType instanceof String) {
                                propertiesNode.putObject(fieldName).put(ElasticSearchConst.TYPE, (String) fieldType);
                            } else if (fieldType instanceof ObjectNode) {
                                propertiesNode.set(fieldName, (ObjectNode) fieldType);
                            }
                        })
                );
    }

}
