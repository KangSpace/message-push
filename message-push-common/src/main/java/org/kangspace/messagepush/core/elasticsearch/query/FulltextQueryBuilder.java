package org.kangspace.messagepush.core.elasticsearch.query;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.protocol.HTTP;
import org.kangspace.messagepush.core.elasticsearch.ElasticSearchConst;
import org.kangspace.messagepush.core.elasticsearch.domain.*;
import org.kangspace.messagepush.core.elasticsearch.enumeration.OccurEnum;
import org.kangspace.messagepush.core.elasticsearch.request.JsonSearchRequest;
import org.kangspace.messagepush.core.elasticsearch.util.QueryUtil;
import org.kangspace.messagepush.core.http.RestProperties;
import org.kangspace.messagepush.core.http.RestTemplateFactory;
import org.kangspace.messagepush.core.util.JsonUtil;
import org.kangspace.messagepush.core.util.ListUtil;
import org.kangspace.messagepush.core.util.StrUtil;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 全文检索构建器
 *
 * @author kango2gler@gmail.com
 */
@Slf4j
public class FulltextQueryBuilder {

    private static HttpHeaders DEFAULT_HEADERS;

    static {
        DEFAULT_HEADERS = new HttpHeaders();
        DEFAULT_HEADERS.add(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.16 Safari/537.36");
        DEFAULT_HEADERS.add(HttpHeaders.ACCEPT_ENCODING, "gzip,deflate");
        DEFAULT_HEADERS.add(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN");
        DEFAULT_HEADERS.add(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=utf-8");
        DEFAULT_HEADERS.add(HttpHeaders.CONNECTION, HTTP.CONN_KEEP_ALIVE);
    }

    @Resource
    private RestTemplateFactory restTemplateFactory;

    public void handler(JsonSearchRequest jsonSearchRequest, FullTextQuery fullTextQuery) {
        //获取分词
        segmentation(fullTextQuery);

        //有关键词但无分词结果 则 设置放弃查询为true
        if (StrUtil.isNotEmpty(fullTextQuery.getKeyword()) && ListUtil.isEmpty(fullTextQuery.getWords())) {
            jsonSearchRequest.setAbandon(true);
            return;
        }

        //需要打分
        if (fullTextQuery.isScore()) {
            //设置分词增强函数
            QueryUtil.addQuery(jsonSearchRequest, OccurEnum.MUST, getFunctionScoreNode(fullTextQuery.getWords(), fullTextQuery.getFullTextItems()));
            //增加排序字段
            Optional.ofNullable(jsonSearchRequest.getRootNode().get(ElasticSearchConst.SORT)).ifPresent(
                    jsonNode -> ((ArrayNode) jsonNode).insert(0, QueryUtil.getNode(ElasticSearchConst.UNDERSCORE_SCORE, ElasticSearchConst.DESC))
            );
        }
        //不需要打分
        else if (fullTextQuery.getFullTextItems() != null) {
            List<String> words = fullTextQuery.getWords().stream().map(Word::getWord).collect(Collectors.toList());
            fullTextQuery.getFullTextItems().forEach(fullTextItem -> {
                QueryUtil.terms(jsonSearchRequest, OccurEnum.FILTER, fullTextItem.getFieldName(), words);
            });
        }
    }

    /**
     * 获取分词信息
     *
     * @param fullTextQuery 查询参数
     */
    private void segmentation(FullTextQuery fullTextQuery) {
        if (ListUtil.isNotEmpty(fullTextQuery.getWords()) || StrUtil.isEmpty(fullTextQuery.getIkUrl())) {
            return;
        }

        //发送http请求获取分词结果
        List<Word> words = Optional.ofNullable(segmentation(fullTextQuery.getIkUrl(), fullTextQuery.getKeyword()))
                //获取分词结果中的同义词集合
                .map(segmentation -> Optional.ofNullable(segmentation.getSynonymWordList())
                        //获取同义词
                        .map(synonymWordInfos -> synonymWordInfos.stream().map(SynonymWord::getWord).collect(Collectors.toList()))
                        //如果为空则分会空集合
                        .orElse(Lists.newArrayList()))
                //如果为空则分会空集合
                .orElse(Lists.newArrayList());
        fullTextQuery.setWords(words);
    }

    /**
     * 设置增强函数
     *
     * @param words         分词结果
     * @param fullTextItems 全文检索项列表结果
     */
    private JsonNode getFunctionScoreNode(List<Word> words, List<FullTextItem> fullTextItems) {
        //初始化增强函数参数节点
        ObjectNode functionScoreParamNode = JsonUtil.createObjectNode()
                .put(ElasticSearchConst.SCORE_MODE, ElasticSearchConst.SUM)
                .put(ElasticSearchConst.BOOST_MODE, ElasticSearchConst.REPLACE);
        //设置分词权重、高斯函数、脚本函数
        setFunctions(functionScoreParamNode, words, fullTextItems);

        if (ListUtil.isNotEmpty(words)) {
            //设置should参数节点
            ArrayNode shouldParamNode = JsonUtil.createArrayNode();
            fullTextItems.forEach(fullTextItem -> {
                words.forEach(word -> {
                    JsonNode termNode = QueryUtil.getNode(ElasticSearchConst.TERM, QueryUtil.getNode(fullTextItem.getFieldName(), word.getWord()));
                    shouldParamNode.add(termNode);
                });
            });
            //设置should节点
            JsonNode shouldNode = QueryUtil.getNode(ElasticSearchConst.SHOULD, shouldParamNode);
            //设置bool节点
            JsonNode boolNode = QueryUtil.getNode(ElasticSearchConst.BOOL, shouldNode);
            //设置query节点
            functionScoreParamNode.set(ElasticSearchConst.QUERY, boolNode);
        }

        //设置functionScore节点
        JsonNode functionScoreNode = JsonUtil.createObjectNode().set(ElasticSearchConst.FUNCTION_SCORE, functionScoreParamNode);
        return functionScoreNode;
    }

    /**
     * 构建高斯函数
     *
     * @param functionScoreParamNode functionScore节点
     */
    private void setFunctions(ObjectNode functionScoreParamNode, List<Word> words, List<FullTextItem> fullTextItems) {
        //默认是先构造高斯函数
        ArrayNode functionsNode = JsonUtil.createArrayNode();
        //遍历配置，依次构建分词、高斯、脚本函数
        fullTextItems.forEach(fullTextItem -> {
            //根据分词设置filter增强函数
            buildWordFilterFunction(functionsNode, words, fullTextItem);
            //获取高斯函数配置
            buildGaussFunction(functionsNode, fullTextItem);
            //获取脚本函数配置
            buildScriptFunction(functionsNode, fullTextItem);
        });

        //设置functionScore参数
        functionScoreParamNode.set(ElasticSearchConst.FUNCTIONS, functionsNode);

    }


    /**
     * 根据分词结果设置filter增强函数
     *
     * @param functionsNode 增强函数集合
     * @param words         分词结果
     * @param words         分词结果
     */
    private void buildWordFilterFunction(ArrayNode functionsNode, List<Word> words, FullTextItem fullTextItem) {
        if (ListUtil.isEmpty(words)) {
            return;
        }
        //循环分词结果，设置增强函数
        words.forEach(word -> {
            Float wordWeight = getWordWeight(word);
            //设置 某个分词的增强函数
            ObjectNode result = getFilterFunctionBuilder(fullTextItem.getFieldName(), word, wordWeight * fullTextItem.getWeight());
            //添加分词分节点
            functionsNode.add(result);
        });
    }

    /**
     * 获取分词权重
     *
     * @param word 分词对象
     * @return
     */
    public Float getWordWeight(Word word) {
        //获取基础词性
        Long pos = word.getPos();
        //获取业务词性
        Long businessPos = word.getBusinessPos();
        //默认权重为1
        Float wordWeight = (float) 1;
        //判断是否是重要基础词性
        if (ImportantPosEnum.judgeIsImportant(pos)) {
            //获取词性的权重
            ImportantPosEnum importantPosEnum = ImportantPosEnum.resolve(pos);
            //设置增强函数以及权重
            wordWeight = wordWeight.compareTo(importantPosEnum.getWeight()) >= 0 ? wordWeight : importantPosEnum.getWeight();
            //判断是否是重要业务词性
        }
        if (ImportantBizPosEnum.judgeIsImportant(businessPos)) {
            //获取词性的权重
            ImportantBizPosEnum importantBizPosEnum = ImportantBizPosEnum.resolve(businessPos);
            //设置增强函数以及权重
            wordWeight = wordWeight.compareTo(importantBizPosEnum.getWeight()) >= 0 ? wordWeight : importantBizPosEnum.getWeight();
        }
        return wordWeight;
    }


    /**
     * 获得分词的评分函数
     *
     * @param fieldName 字段名
     * @param word      分词
     * @param weight    权重
     * @return 分词的评分函数
     */
    private ObjectNode getFilterFunctionBuilder(String fieldName, Word word, Float weight) {
        //创建分词的增强函数节点
        ObjectNode filterNode = (ObjectNode) QueryUtil.getNode(ElasticSearchConst.FILTER, QueryUtil.getNode(ElasticSearchConst.TERM, QueryUtil.getNode(fieldName, word.getWord())));
        //设置权重
        filterNode.put(ElasticSearchConst.WEIGHT, weight);
        return filterNode;
    }

    /**
     * 拼装高斯函数
     *
     * @param functionsNode 函数列表节点
     * @param fullTextItem  全文检索对象
     */
    private void buildGaussFunction(ArrayNode functionsNode, FullTextItem fullTextItem) {
        if (ListUtil.isEmpty(fullTextItem.getGaussConfigs())) {
            return;
        }
        fullTextItem.getGaussConfigs().forEach(gaussConfig -> {
            ObjectNode gaussParamNode = JsonUtil.createObjectNode()
                    //设置起始值
                    .put(ElasticSearchConst.ORIGIN, gaussConfig.getOrigin())
                    //设置级别银子
                    .put(ElasticSearchConst.SCALE, gaussConfig.getScale())
                    //设置补偿系数
                    .put(ElasticSearchConst.OFFSET, gaussConfig.getOffset())
                    //设置衰减系数
                    .put(ElasticSearchConst.DECAY, gaussConfig.getDecay());
            //设置高斯属性节点
            ObjectNode fieldNode = (ObjectNode) JsonUtil.createObjectNode().set(gaussConfig.getFieldName(), gaussParamNode);
            //设置高斯节点以及权重
            functionsNode.add(JsonUtil.createObjectNode().put(ElasticSearchConst.WEIGHT, gaussConfig.getWeight() * fullTextItem.getWeight()).set(ElasticSearchConst.GAUSS, fieldNode));
        });
    }

    /**
     * 拼装高斯函数
     *
     * @param functionsNode 函数列表节点
     * @param fullTextItem  全文检索对象
     */
    private void buildScriptFunction(ArrayNode functionsNode, FullTextItem fullTextItem) {
        if (ListUtil.isEmpty(fullTextItem.getScriptConfigs())) {
            return;
        }
        fullTextItem.getScriptConfigs().forEach(scriptConfig -> {
            ObjectNode scriptNode = (ObjectNode) QueryUtil.getNode(ElasticSearchConst.SCRIPT_SCORE, QueryUtil.getNode(ElasticSearchConst.SCRIPT, QueryUtil.getNode(ElasticSearchConst.SOURCE, scriptConfig.getScript())));
            scriptNode.put(ElasticSearchConst.WEIGHT, scriptConfig.getWeight());
            functionsNode.add(scriptNode);
        });
    }

    /**
     * 获取RestTemplate
     *
     * @return
     */
    private synchronized RestTemplate getRestTemplate() {
        RestTemplate restTemplate = restTemplateFactory.getRestTemplate(ElasticSearchConst.REST_TEMPLATE_POOL);
        if (restTemplate == null) {
            restTemplate = restTemplateFactory.registerRestTemplate(ElasticSearchConst.REST_TEMPLATE_POOL, new RestProperties());
        }
        return restTemplate;
    }


    /**
     * 获取分词信息
     *
     * @param ikUrl
     * @param keyword
     * @return
     */
    public Segmentation segmentation(String ikUrl, String keyword) {
        //构建分词查询参数
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.add(ElasticSearchConst.KW, keyword);
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(paramMap, DEFAULT_HEADERS);
        return getRestTemplate().postForObject(ikUrl, httpEntity, Segmentation.class);
    }
}
