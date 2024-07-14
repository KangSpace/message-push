package org.kangspace.messagepush.core.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JSON工具类
 *
 * @author kango2gler@gmail.com
 * @since 2021-04-29
 */
public final class JsonUtil {

    /**
     * 默认MAPPER
     */
    private static final ObjectMapper OBJECT_MAPPER;

    /**
     * 自定义属性类型的MAPPER
     */
    private static final Map<String, ObjectMapper> OBJECT_MAPPER_MAP = new ConcurrentHashMap<>();

    /**
     * 缓存自定义ObjectMapper，可在系统启动时使用此方法缓存自定义ObjectMapper
     *
     * @param key          自定义ObjectMapper标识主键
     * @param objectMapper ObjectMapper
     */
    public static void setObjectMapper(String key, ObjectMapper objectMapper) {
        if (StrUtil.isEmpty(key) || objectMapper == null) {
            return;
        }
        OBJECT_MAPPER_MAP.put(key, objectMapper);
    }

    /**
     * 获取默认ObjectMapper
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    /**
     * 获取缓存的自定义ObjectMapper
     *
     * @param key 自定义ObjectMapper标识主键
     */
    public static ObjectMapper getObjectMapper(String key) {
        if (StrUtil.isEmpty(key)) {
            return null;
        }
        return OBJECT_MAPPER_MAP.get(key);
    }

    /**
     * 基于指定的ObjectMapper进行对象转JSON字符串（ObjectMapper创建开销较大，务必缓存）
     *
     * @param src          对象
     * @param objectMapper ObjectMapper
     * @param <T>          对象泛型
     * @return JSON字符串
     */
    public static <T> String toJson(T src, ObjectMapper objectMapper) {
        if (src == null || objectMapper == null) {
            return null;
        }
        try {
            return src instanceof String ? (String) src : objectMapper.writeValueAsString(src);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 对象转JSON字符串
     *
     * @param src 对象
     * @param <T> 对象泛型
     * @return JSON字符串
     */
    public static <T> String toJson(T src) {
        return toJson(src, OBJECT_MAPPER);
    }

    /**
     * 对象转换为格式化后的JSON字符串
     *
     * @param src          对象
     * @param objectMapper 指定ObjectMapper
     * @param <T>          对象泛型
     * @return 格式化后的JSON字符串
     */
    public static <T> String toFormatJson(T src, ObjectMapper objectMapper) {
        if (src == null || objectMapper == null) {
            return null;
        }
        try{
            if (src instanceof String) {
                if (StrUtil.isBlank((String) src)) {
                    return "";
                }
                Object obj = objectMapper.readValue((String) src, Object.class);
                return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
            }
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(src);
        }catch (IOException e){
            throw new RuntimeException(e);
        }

    }

    /**
     * 对象转换为格式化后的JSON字符串
     *
     * @param src 对象
     * @param <T> 对象泛型
     * @return 格式化后的JSON字符串
     */
    public static <T> String toFormatJson(T src) {
        return toFormatJson(src, OBJECT_MAPPER);
    }

    /**
     * JSON字符串转换为对象
     *
     * @param json         JSON字符串
     * @param objectMapper 指定ObjectMapper
     * @param valueType    对象类
     * @param <T>          类泛型
     * @return 对象
     */
    public static <T> T toObject(String json, Class<T> valueType, ObjectMapper objectMapper) {
        if (StrUtil.isBlank(json) || objectMapper == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, valueType);
        } catch (IOException e) {
            // 抛出转换异常
            throw new RuntimeException(e);
        }
    }

    /**
     * JSON字符串转换为对象
     *
     * @param json      JSON字符串
     * @param valueType 对象类
     * @param <T>       类泛型
     * @return 对象
     */
    public static <T> T toObject(String json, Class<T> valueType) {
        if (String.class.equals(valueType)) {
            return (T) json;
        }
        return toObject(json, valueType, OBJECT_MAPPER);
    }

    /**
     * JSON字符串转换为对象
     *
     * @param json          JSON字符串
     * @param typeReference 返回对象泛型
     * @param <T>           类泛型
     * @return 对象
     */
    public static <T> T toObject(String json, TypeReference<T> typeReference) {
        if (StrUtil.isBlank(json) || typeReference == null) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 返回List对象
     *
     * @param json         JSON字符串
     * @param objectMapper 指定ObjectMapper
     * @param <T>          泛型
     * @return List对象
     */
    public static <T> Set<T> toSet(String json, ObjectMapper objectMapper) {
        if (StrUtil.isBlank(json) || objectMapper == null) {
            return null;
        }
        try{
            return OBJECT_MAPPER.readValue(json, new TypeReference<Set<T>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 返回List对象
     *
     * @param json         JSON字符串
     * @param objectMapper 指定ObjectMapper
     * @param <T>          泛型
     * @return List对象
     */
    public static <T> List<T> toList(String json, ObjectMapper objectMapper) {
        if (StrUtil.isBlank(json) || objectMapper == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<List<T>>() {
            });
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    /**
     * 返回List对象
     *
     * @param json JSON字符串
     * @param <T>  泛型
     * @return List对象
     */
    public static <T> List<T> toList(String json) {
        return toList(json, OBJECT_MAPPER);
    }

    /**
     * 返回List对象
     *
     * @param json JSON字符串
     * @param <T>  泛型
     * @return List对象
     */
    public static <T> Set<T> toSet(String json) {
        return toSet(json, OBJECT_MAPPER);
    }

    /**
     * 返回Map对象
     *
     * @param json         JSON字符串
     * @param objectMapper 指定ObjectMapper
     * @param <K>          Map的key泛型
     * @param <V>          Map的value泛型
     * @return Map对象
     */
    public static <K, V> Map<K, V> toMap(String json, ObjectMapper objectMapper) {
        if (StrUtil.isBlank(json) || objectMapper == null) {
            return null;
        }
        try{
            return objectMapper.readValue(json, new TypeReference<Map<K, V>>() {
            });
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    /**
     * 返回Map对象
     *
     * @param json JSON字符串
     * @param <K>  Map的key泛型
     * @param <V>  Map的value泛型
     * @return Map对象
     */
    public static <K, V> Map<K, V> toMap(String json) {
        return toMap(json, OBJECT_MAPPER);
    }

    /**
     * 创建一个json对象节点
     *
     * @return
     */
    public static ObjectNode createObjectNode() {
        return OBJECT_MAPPER.createObjectNode();
    }

    /**
     * 创建一个json数组节点
     *
     * @return
     */
    public static ArrayNode createArrayNode() {
        return OBJECT_MAPPER.createArrayNode();
    }

    /**
     * 克隆数据，大数据量复杂对象克隆慎用
     *
     * @param t
     * @param valueType
     * @param <T>
     * @return
     */
    public static <T> T clone(T t, Class<T> valueType) {
        return toObject(toJson(t), valueType);
    }

    /**
     * 判断字符串是否为json
     * @param json
     * @param valueType
     * @param objectMapper
     * @return
     */
    public static boolean isJson(String json, Class<Object> valueType, ObjectMapper objectMapper){
        if (StrUtil.isBlank(json) || objectMapper == null) {
            return false;
        }
        try {
            objectMapper.readValue(json, valueType);
            return true;
        } catch (IOException e) {
            // 抛出转换异常
            return false;
        }
    }



    /**
     * 初始化默认ObjectMapper
     */
    static {
        ObjectMapper customMapper = new ObjectMapper();
        //对象属性为null时不输出JSON字符串
        customMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        //忽略 JSON字符串中存在但Java对象无响应属性 时的报错
        customMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //禁止 使用int代表Enum的order()來反序列化Enum
        customMapper.configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, true);
        //忽略 属性大小写
        customMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        customMapper.findAndRegisterModules();
        OBJECT_MAPPER = customMapper;
    }



}

