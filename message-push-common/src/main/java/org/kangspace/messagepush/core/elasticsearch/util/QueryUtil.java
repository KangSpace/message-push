package org.kangspace.messagepush.core.elasticsearch.util;

import cn.hutool.core.date.DateUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.kangspace.messagepush.core.elasticsearch.ElasticSearchConst;
import org.kangspace.messagepush.core.elasticsearch.annotation.QueryField;
import org.kangspace.messagepush.core.elasticsearch.domain.FullTextQuery;
import org.kangspace.messagepush.core.elasticsearch.enumeration.OccurEnum;
import org.kangspace.messagepush.core.elasticsearch.query.FulltextQueryBuilder;
import org.kangspace.messagepush.core.elasticsearch.request.JsonSearchRequest;
import org.kangspace.messagepush.core.util.JsonUtil;
import org.kangspace.messagepush.core.util.ListUtil;
import org.kangspace.messagepush.core.util.StrUtil;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 查询工具类
 *
 * @author kango2gler@gmail.com
 */
@Slf4j
public class QueryUtil {

    /**
     * Date类型默认格式
     */
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    /**
     * LocalDateTime类型默认格式
     */
    private static final String DEFAULT_LOCAL_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'+'08:00";

    /**
     * 全文检索构建器
     */
    private static FulltextQueryBuilder fulltextQueryBuilder;

    /**
     * 初始化
     *
     * @param fulltextQueryBuilder 全文检索构建对象
     */
    public static void init(FulltextQueryBuilder fulltextQueryBuilder) {
        QueryUtil.fulltextQueryBuilder = fulltextQueryBuilder;
    }

    /**
     * 自动拼装
     *
     * @param jsonSearchRequest 查询请求对象
     * @param entity            实体
     * @param <T>               泛型
     * @throws Exception 异常
     */
    public static <T> void entityToQuery(JsonSearchRequest jsonSearchRequest, T entity) throws Exception {

        //初始化查询框架
        initQueryNode(jsonSearchRequest);
        if (entity == null) {
            return;
        }
        //遍历实体类属性
        for (Field field : entity.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(QueryField.class)) {
                continue;
            }
            //属性名
            String fieldName = field.getName();
            QueryField fieldAnnotation = field.getAnnotation(QueryField.class);
            if (StrUtil.isNotEmpty(fieldAnnotation.field())) {
                fieldName = fieldAnnotation.field().trim();
            }

            //属性值
            Object fieldValue = getMethod(field, entity);
            if (fieldValue == null) {
                continue;
            }
            fieldValue = formatValue(fieldValue, fieldAnnotation.format());

            //按处理方式拼装查询条件
            switch (fieldAnnotation.value()) {
                case TERM:
                    term(jsonSearchRequest, fieldAnnotation.occur(), fieldName, fieldValue);
                    break;
                case TERM_GT_ZERO:
                    termGtZero(jsonSearchRequest, fieldAnnotation.occur(), fieldName, Integer.parseInt(String.valueOf(fieldValue)));
                    break;
                case TERM_GTE_ZERO:
                    termGteZero(jsonSearchRequest, fieldAnnotation.occur(), fieldName, Integer.parseInt(String.valueOf(fieldValue)));
                    break;
                case TERMS:
                    terms(jsonSearchRequest, fieldAnnotation.occur(), fieldName, fieldValue);
                    break;
                case TERMS_GT_ZERO:
                    termsGtZero(jsonSearchRequest, fieldAnnotation.occur(), fieldName, fieldValue);
                    break;
                case TERMS_GTE_ZERO:
                    termsGteZero(jsonSearchRequest, fieldAnnotation.occur(), fieldName, fieldValue);
                    break;
                case TERMS_AND:
                    termsAnd(jsonSearchRequest, fieldAnnotation.occur(), fieldName, fieldValue);
                    break;
                case MATCH:
                    match(jsonSearchRequest, fieldAnnotation.occur(), fieldName, fieldValue);
                    break;
                case MATCH_PHRASE:
                    matchPhrase(jsonSearchRequest, fieldAnnotation.occur(), fieldName, fieldValue);
                    break;
                case MATCH_PHRASE_PREFIX:
                    buildQueryNode(ElasticSearchConst.MATCH_PHRASE_PREFIX, jsonSearchRequest, fieldAnnotation.occur(), fieldName, fieldValue);
                    break;
                case PREFIX:
                    buildQueryNode(ElasticSearchConst.PREFIX, jsonSearchRequest, fieldAnnotation.occur(), fieldName, fieldValue);
                    break;
                case RANGE_GT:
                    range(jsonSearchRequest, fieldAnnotation.occur(), fieldName, fieldValue, ElasticSearchConst.GT);
                    break;
                case RANGE_GTE:
                    range(jsonSearchRequest, fieldAnnotation.occur(), fieldName, fieldValue, ElasticSearchConst.GTE);
                    break;
                case RANGE_LT:
                    range(jsonSearchRequest, fieldAnnotation.occur(), fieldName, fieldValue, ElasticSearchConst.LT);
                    break;
                case RANGE_LTE:
                    range(jsonSearchRequest, fieldAnnotation.occur(), fieldName, fieldValue, ElasticSearchConst.LTE);
                    break;
                case FULL_TEXT:
                    fulltext(jsonSearchRequest, fieldValue);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 全文检索
     *
     * @param jsonSearchRequest 查询对象
     * @param fieldValue        属性值
     */
    private static void fulltext(JsonSearchRequest jsonSearchRequest, Object fieldValue) {
        if (fulltextQueryBuilder == null || !(fieldValue instanceof FullTextQuery)) {
            return;
        }
        fulltextQueryBuilder.handler(jsonSearchRequest, ((FullTextQuery) fieldValue));
    }

    /**
     * 初始化查询节点
     *
     * @param jsonSearchRequest 查询对象
     */
    private static void initQueryNode(JsonSearchRequest jsonSearchRequest) {
        ObjectNode queryNode = getNode();
        ObjectNode boolNode = getNode();
        ArrayNode filterNode = JsonUtil.createArrayNode();
        ArrayNode mustNode = JsonUtil.createArrayNode();
        ArrayNode mustNotNode = JsonUtil.createArrayNode();
        ArrayNode shouldNode = JsonUtil.createArrayNode();
        boolNode.set(ElasticSearchConst.FILTER, filterNode);
        boolNode.set(ElasticSearchConst.MUST, mustNode);
        boolNode.set(ElasticSearchConst.MUST_NOT, mustNotNode);
        boolNode.set(ElasticSearchConst.SHOULD, shouldNode);
        queryNode.set(ElasticSearchConst.BOOL, boolNode);
        jsonSearchRequest.getRootNode().set(ElasticSearchConst.QUERY, queryNode);
    }

    /**
     * 得到属性get方法的值
     *
     * @param field  字段域
     * @param entity 实体
     * @param <T>    泛型
     * @return 调用get方法返回字段值
     * @throws Exception 异常
     */
    private static <T> Object getMethod(Field field, T entity) throws Exception {
        PropertyDescriptor pd = new PropertyDescriptor(field.getName(), entity.getClass());
        Method method = pd.getReadMethod();
        method.setAccessible(true);
        return method.invoke(entity);
    }

    /**
     * 格式化属性值
     *
     * @param fieldValue 属性值
     * @param format     格式
     * @return 格式化后值
     */
    private static Object formatValue(Object fieldValue, String format) {
        if (fieldValue == null) {
            return null;
        }
        Object value;
        if (fieldValue instanceof LocalDateTime) {
            if (StrUtil.isEmpty(format)) {
                format = DEFAULT_LOCAL_DATE_TIME_FORMAT;
            }
            value = DateUtil.format(((LocalDateTime) fieldValue), format);
        } else if (fieldValue instanceof Date) {
            if (StrUtil.isEmpty(format)) {
                format = DEFAULT_DATE_FORMAT;
            }
            value = DateUtil.format(((Date) fieldValue), format);
        } else {
            value = fieldValue;
        }
        return value;
    }


    /**
     * 等于
     *
     * @param jsonSearchRequest 查询对象
     * @param occurEnum         查询条件拼装的位置，是在must、filter、should中
     * @param fieldName         属性名
     * @param fieldValue        属性值
     */
    public static void term(JsonSearchRequest jsonSearchRequest, OccurEnum occurEnum, String fieldName, Object fieldValue) {
        if (jsonSearchRequest == null || occurEnum == null || StrUtil.isEmpty(fieldName) || fieldValue == null) {
            return;
        }
        getOccurNode(jsonSearchRequest, occurEnum).add(getNode(ElasticSearchConst.TERM, getNode(fieldName, fieldValue)));

    }

    /**
     * 等于，值大于0才拼装
     *
     * @param jsonSearchRequest 查询对象
     * @param occurEnum         查询条件拼装的位置，是在must、filter、should中
     * @param fieldName         属性名
     * @param fieldValue        属性值
     */
    public static void termGtZero(JsonSearchRequest jsonSearchRequest, OccurEnum occurEnum, String fieldName, Integer fieldValue) {
        if (jsonSearchRequest == null || occurEnum == null || StrUtil.isEmpty(fieldName) || fieldValue == null || fieldValue <= 0) {
            return;
        }
        getOccurNode(jsonSearchRequest, occurEnum).add(getNode(ElasticSearchConst.TERM, getNode(fieldName, fieldValue)));
    }

    /**
     * 等于，值大于等于0才拼装
     *
     * @param jsonSearchRequest 查询对象
     * @param occurEnum         查询条件拼装的位置，是在must、filter、should中
     * @param fieldName         属性名
     * @param fieldValue        属性值
     */
    public static void termGteZero(JsonSearchRequest jsonSearchRequest, OccurEnum occurEnum, String fieldName, Integer fieldValue) {
        if (jsonSearchRequest == null || occurEnum == null || StrUtil.isEmpty(fieldName) || fieldValue == null || fieldValue < 0) {
            return;
        }
        getOccurNode(jsonSearchRequest, occurEnum).add(getNode(ElasticSearchConst.TERM, getNode(fieldName, fieldValue)));
    }

    /**
     * 多项或完全匹配
     *
     * @param jsonSearchRequest 查询对象
     * @param occurEnum         查询条件拼装的位置，是在must、filter、should中
     * @param fieldName         属性名
     * @param fieldValue        属性值
     */
    public static void terms(JsonSearchRequest jsonSearchRequest, OccurEnum occurEnum, String fieldName, Object fieldValue) {
        List values = getValues(fieldValue);
        if (values == null) {
            return;
        }
        baseTerms(jsonSearchRequest, occurEnum, fieldName, values);
    }

    /**
     * 多项或完全匹配，值为正整数才拼装
     *
     * @param jsonSearchRequest 查询对象
     * @param occurEnum         查询条件拼装的位置，是在must、filter、should中
     * @param fieldName         属性名
     * @param fieldValue        属性值
     */
    public static void termsGtZero(JsonSearchRequest jsonSearchRequest, OccurEnum occurEnum, String fieldName, Object fieldValue) {
        List values = getValues(fieldValue);
        if (values == null) {
            return;
        }
        for (int i = values.size() - 1; i >= 0; i--) {
            Object value = values.get(i);
            if (value instanceof Integer) {
                if ((Integer) value <= 0) {
                    values.remove(i);
                }
            } else if (value instanceof Double) {
                if ((Double) value <= 0) {
                    values.remove(i);
                }
            } else if (value instanceof String) {
                if (StrUtil.strToInt((String) value, 0) <= 0) {
                    values.remove(i);
                }
            } else {
                values.remove(i);
            }
        }
        baseTerms(jsonSearchRequest, occurEnum, fieldName, values);
    }

    /**
     * 多项或完全匹配，值为自然数才拼装
     *
     * @param jsonSearchRequest 查询对象
     * @param occurEnum         查询条件拼装的位置，是在must、filter、should中
     * @param fieldName         属性名
     * @param fieldValue        属性值
     */
    public static void termsGteZero(JsonSearchRequest jsonSearchRequest, OccurEnum occurEnum, String fieldName, Object fieldValue) {
        List values = getValues(fieldValue);
        if (values == null) {
            return;
        }
        for (int i = values.size() - 1; i >= 0; i--) {
            Object value = values.get(i);
            if (value instanceof Integer) {
                if ((Integer) value < 0) {
                    values.remove(i);
                }
            } else if (value instanceof Double) {
                if ((Double) value < 0) {
                    values.remove(i);
                }
            } else if (value instanceof String) {
                if (StrUtil.strToInt((String) value, 0) < 0) {
                    values.remove(i);
                }
            } else {
                values.remove(i);
            }
        }
        baseTerms(jsonSearchRequest, occurEnum, fieldName, values);
    }

    /**
     * 多项与完全匹配
     *
     * @param jsonSearchRequest 查询对象
     * @param occurEnum         查询条件拼装的位置，是在must、filter、should中
     * @param fieldName         属性名
     * @param fieldValue        属性值
     */
    public static void termsAnd(JsonSearchRequest jsonSearchRequest, OccurEnum occurEnum, String fieldName, Object fieldValue) {
        List values = getValues(fieldValue);
        if (values == null) {
            return;
        }
        values.forEach(value -> getOccurNode(jsonSearchRequest, occurEnum).add(getNode(ElasticSearchConst.TERM, getNode(fieldName, value))));
    }

    /**
     * 基于Object对象获取List
     *
     * @param fieldValue 属性值
     * @return
     */
    private static List getValues(Object fieldValue) {
        if (fieldValue == null) {
            return null;
        }
        if (fieldValue instanceof List) {
            if (((List) fieldValue).size() == 0) {
                return null;
            }
            return (List) fieldValue;
        } else if (fieldValue instanceof String) {
            if (StrUtil.isEmpty((String) fieldValue)) {
                return null;
            }
            return StrUtil.strToList((String) fieldValue);
        }
        return null;
    }

    /**
     * 基础terms方法
     *
     * @param jsonSearchRequest 查询对象
     * @param occurEnum         查询条件拼装的位置，是在must、filter、should中
     * @param fieldName         属性名
     * @param fieldValue        属性值
     */
    private static void baseTerms(JsonSearchRequest jsonSearchRequest, OccurEnum occurEnum, String fieldName, List fieldValue) {
        if (jsonSearchRequest == null || occurEnum == null || StrUtil.isEmpty(fieldName) || ListUtil.isEmpty(fieldValue)) {
            return;
        }
        getOccurNode(jsonSearchRequest, occurEnum).add(getNode(ElasticSearchConst.TERMS, getNode(fieldName, fieldValue)));
    }

    /**
     * 分词匹配查询
     *
     * @param jsonSearchRequest 查询对象
     * @param occurEnum         查询条件拼装的位置，是在must、filter、should中
     * @param fieldName         属性名
     * @param fieldValue        属性值
     */
    public static void match(JsonSearchRequest jsonSearchRequest, OccurEnum occurEnum, String fieldName, Object fieldValue) {
        if (jsonSearchRequest == null || occurEnum == null || StrUtil.isEmpty(fieldName) || fieldValue == null) {
            return;
        }
        getOccurNode(jsonSearchRequest, occurEnum).add(getNode(ElasticSearchConst.MATCH, getNode(fieldName, fieldValue)));
    }

    /**
     * 分词匹配-短语匹配查询
     *
     * @param jsonSearchRequest 查询对象
     * @param occurEnum         查询条件拼装的位置，是在must、filter、should中
     * @param fieldName         属性名
     * @param fieldValue        属性值
     */
    public static void matchPhrase(JsonSearchRequest jsonSearchRequest, OccurEnum occurEnum, String fieldName, Object fieldValue) {
        if (jsonSearchRequest == null || occurEnum == null || StrUtil.isEmpty(fieldName) || fieldValue == null) {
            return;
        }
        getOccurNode(jsonSearchRequest, occurEnum).add(getNode(ElasticSearchConst.MATCH_PHRASE, getNode(fieldName, fieldValue)));
    }

    /**
     * 构建查询节点
     *
     * @param queryType         查询类型 如:{@link ElasticSearchConst#MATCH_PHRASE_PREFIX},{@link ElasticSearchConst#MATCH_PHRASE}等
     * @param jsonSearchRequest jsonSearchRequest
     * @param occurEnum         查询条件拼装的位置，是在must、filter、should中
     * @param fieldName         属性名
     * @param fieldValue        属性值
     */
    public static void buildQueryNode(String queryType, JsonSearchRequest jsonSearchRequest, OccurEnum occurEnum, String fieldName, Object fieldValue) {
        if (jsonSearchRequest == null || occurEnum == null || StrUtil.isEmpty(fieldName) || fieldValue == null) {
            return;
        }
        getOccurNode(jsonSearchRequest, occurEnum).add(getNode(queryType, getNode(fieldName, fieldValue)));
    }

    /**
     * 范围查询
     *
     * @param jsonSearchRequest 查询对象
     * @param occurEnum         查询条件拼装的位置，是在must、filter、should中
     * @param fieldName         属性名
     * @param fieldValue        属性值
     * @param relation          关系运算符 ElasticSearchConst.GT、ElasticSearchConst.GTE、ElasticSearchConst.LT、ElasticSearchConst.LTE
     */
    public static void range(JsonSearchRequest jsonSearchRequest, OccurEnum occurEnum, String fieldName, Object fieldValue, String relation) {
        if (jsonSearchRequest == null || occurEnum == null || StrUtil.isEmpty(fieldName) || fieldValue == null) {
            return;
        }
        getOccurNode(jsonSearchRequest, occurEnum).add(getNode(ElasticSearchConst.RANGE, getNode(fieldName, getNode(relation, fieldValue))));
    }

    /**
     * 增加查询条件
     *
     * @param jsonSearchRequest 查询对象
     * @param occurEnum         查询条件拼装的位置，是在must、filter、should中
     * @param jsonNode
     */
    public static void addQuery(JsonSearchRequest jsonSearchRequest, OccurEnum occurEnum, JsonNode jsonNode) {
        if (jsonSearchRequest == null || occurEnum == null || jsonNode == null) {
            return;
        }
        getOccurNode(jsonSearchRequest, occurEnum).add(jsonNode);
    }

    /**
     * 获取一个空JsonNode
     *
     * @return
     */
    private static ObjectNode getNode() {
        return JsonUtil.createObjectNode();
    }

    /**
     * 获取一个节点jsonNode
     *
     * @param fieldName  属性名
     * @param fieldValue 属性值
     * @return
     */
    public static JsonNode getNode(String fieldName, Object fieldValue) {
        if (fieldValue instanceof JsonNode) {
            return JsonUtil.createObjectNode().set(fieldName, (JsonNode) fieldValue);
        } else if (fieldValue instanceof List) {
            ArrayNode arrayNode = JsonUtil.createArrayNode();
            ((List) fieldValue).forEach(value -> {
                if (value instanceof String) {
                    arrayNode.add((String) value);
                } else {
                    arrayNode.addPOJO(value);
                }
            });
            return JsonUtil.createObjectNode().set(fieldName, arrayNode);
        } else if (fieldValue instanceof String) {
            return JsonUtil.createObjectNode().put(fieldName, (String) fieldValue);
        } else if (fieldValue instanceof Integer) {
            return JsonUtil.createObjectNode().put(fieldName, (Integer) fieldValue);
        } else if (fieldValue instanceof Long) {
            return JsonUtil.createObjectNode().put(fieldName, (Long) fieldValue);
        } else if (fieldValue instanceof Short) {
            return JsonUtil.createObjectNode().put(fieldName, (Short) fieldValue);
        } else if (fieldValue instanceof Float) {
            return JsonUtil.createObjectNode().put(fieldName, (Float) fieldValue);
        } else if (fieldValue instanceof Double) {
            return JsonUtil.createObjectNode().put(fieldName, (Double) fieldValue);
        } else if (fieldValue instanceof Boolean) {
            return JsonUtil.createObjectNode().put(fieldName, (Boolean) fieldValue);
        } else if (fieldValue instanceof BigDecimal) {
            return JsonUtil.createObjectNode().put(fieldName, (BigDecimal) fieldValue);
        } else {
            return JsonUtil.createObjectNode().putPOJO(fieldName, fieldValue);
        }
    }

    /**
     * 获取查询条件拼装的位置
     *
     * @param jsonSearchRequest 查询对象
     * @param occurEnum         查询条件拼装的位置，是在must、filter、should中
     * @return 查询条件拼装的位置
     */
    public static ArrayNode getOccurNode(JsonSearchRequest jsonSearchRequest, OccurEnum occurEnum) {
        ArrayNode occurNode = null;
        switch (occurEnum) {
            case FILTER:
                occurNode = ((ArrayNode) jsonSearchRequest.getRootNode().get(ElasticSearchConst.QUERY).get(ElasticSearchConst.BOOL).get(ElasticSearchConst.FILTER));
                break;
            case MUST:
                occurNode = ((ArrayNode) jsonSearchRequest.getRootNode().get(ElasticSearchConst.QUERY).get(ElasticSearchConst.BOOL).get(ElasticSearchConst.MUST));
                break;
            case MUST_NOT:
                occurNode = ((ArrayNode) jsonSearchRequest.getRootNode().get(ElasticSearchConst.QUERY).get(ElasticSearchConst.BOOL).get(ElasticSearchConst.MUST_NOT));
                break;
            case SHOULD:
                occurNode = ((ArrayNode) jsonSearchRequest.getRootNode().get(ElasticSearchConst.QUERY).get(ElasticSearchConst.BOOL).get(ElasticSearchConst.SHOULD));
                break;
            default:
                break;
        }
        return occurNode;
    }

}
