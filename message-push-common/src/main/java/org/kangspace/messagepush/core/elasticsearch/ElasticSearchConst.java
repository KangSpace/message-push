package org.kangspace.messagepush.core.elasticsearch;

/**
 * @author kango2gler@gmail.com
 * @date 2024/7/13
 * @since
 */
public interface ElasticSearchConst {

    /**
     * 分页最大查询记录数 TODO 后续考虑使用 scroll解决查询不可超过10000的问题
     */
    int MAX_PAGE_SEARCH_COUNT = 10000;
    /**
     * 默认分页大小
     */
    int DEFAULT_PAGE_SIZE = 10;
    /**
     * es默认时间格式
     */
    String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    /**
     * 分词rest接口连接池名称
     */
    String REST_TEMPLATE_POOL = "elasticsearchFullText";

    /**
     * 分词rest接口分词参数名
     */
    String KW = "kw";

    Long ZERO = 0L;
    // ***************************************创建索引开始***************************************
    /**
     * 索引开启状态
     */
    String STATE = "state";
    /**
     * 索引开启状态-open
     */
    String OPEN = "open";
    /**
     * 设置项
     */
    String SETTINGS = "settings";
    /**
     * 分片数
     */
    String NUMBER_OF_SHARDS = "number_of_shards";
    /**
     * 副本数
     */
    String NUMBER_OF_REPLICAS = "number_of_replicas";
    /**
     * 别名ALIASES
     */
    String ALIASES = "aliases";
    /**
     * 别名ALIAS
     */
    String ALIAS = "alias";
    /**
     * 别名操作 请求路径
     */
    String ALIASES_ENDPOINT = "_aliases";
    /**
     * 别名actions
     */
    String ALIASES_ACTIONS = "actions";
    /**
     * 别名actions add
     */
    String ALIASES_ACTIONS_ADD = "add";
    /**
     * 别名actions remove
     */
    String ALIASES_ACTIONS_REMOVE = "remove";
    /**
     * 索引key
     */
    String INDEX = "index";
    /**
     * is_write_index
     */
    String IS_WRITE_INDEX = "is_write_index";
    /**
     * 映射
     */
    String MAPPINGS = "mappings";
    /**
     * 属性
     */
    String PROPERTIES = "properties";
    /**
     * 数据类型
     */
    String TYPE = "type";
    /**
     * 数据类型 - 文本
     */
    String TEXT = "text";
    /**
     * 数据类型 - 关键字
     */
    String KEYWORD = "keyword";
    /**
     * 数据结构 - 有符号的8位整数, 范围: [-128 ~ 127]
     */
    String BYTE = "byte";
    /**
     * 数据类型 - 有符号的16位整数, 范围: [-32768 ~ 32767]
     */
    String SHORT = "short";
    /**
     * 数据类型 - 有符号的32位整数, 范围: [$-2^{31}$ ~ $2^{31}$-1]
     */
    String INTEGER = "integer";
    /**
     * 数据类型 - 有符号的32位整数, 范围: [$-2^{63}$ ~ $2^{63}$-1]
     */
    String LONG = "long";
    /**
     * 数据类型 - 32位单精度浮点数
     */
    String FLOAT = "float";
    /**
     * 数据类型 - 64位双精度浮点数
     */
    String DOUBLE = "double";
    /**
     * 数据类型 - 日期
     */
    String DATE = "date";
    /**
     * 数据类型 - 布尔
     */
    String BOOLEAN = "boolean";
    /**
     * 索引的分词器,一般索引和搜索用同样的分词器，如需不一样可更改
     */
    String SEARCH_ANALYZER = "search_analyzer";
    /**
     * 字符串分析器,默认值:"standard"
     */
    String ANALYZER = "analyzer";
    /**
     * 字符串专用，查询时将term-document关系存储在内存中
     */
    String FIELDDATA = "fielddata";
    /**
     * BM25/classic/boolean，主要用于文本字段的相似度算法，默认值："BM25"
     */
    String SIMILARITY = "similarity";
    /**
     * 默认情况字段被索引可以搜索，但没有存储原始值且不能用原始值查询，_resource包含了所有的值，当大段文本需要搜索时可以修改为true，默认值：false
     */
    String STORE = "store";
    /**
     * docs(只索引文档编号)/freqs(索引文档编号和词频)/positions(索引文档编号/词频/词位置)/offsets(索引文档编号/词频/词偏移量/词位置) ,被索引的字段默认用positions，其他的docs，默认值：positions/docs
     */
    String INDEX_OPTIONS = "index_options";

    // ***************************************创建索引结束***************************************

    // ***************************************查询root开始***************************************

    /**
     * 起始行
     */
    String FROM = "from";
    /**
     * 查询数量
     */
    String SIZE = "size";
    /**
     * 单分片扫描终结数量（准确度换速度）
     */
    String TERMINATE_AFTER = "terminate_after";
    /**
     * 等待超时时间
     */
    String TIMEOUT = "timeout";
    /**
     * 显示列表
     */
    String UNDERSCORE_SOURCE = "_source";
    /**
     * 查询
     */
    String QUERY = "query";
    /**
     * 高亮
     */
    String HIGHLIGHT = "highlight";
    /**
     * 排序
     */
    String SORT = "sort";

    /**
     * 分数
     */
    String UNDERSCORE_SCORE = "_score";

    /**
     * 返回每个搜索命中的版本
     */
    String VERSION = "version";
    /**
     * 关联度得分计算
     */
    String EXPLAIN = "explain";
    /**
     * 允许在搜索多个索引时为每个索引配置不同的提升权重
     */
    String INDICES_BOOST = "indices_boost";
    /**
     * 排除_score小于min_score中指定的最小值的文档
     */
    String MIN_SCORE = "min_score";
    /**
     * 对query和post_filter阶段返回的Top-K结果执行第二个查询
     */
    String RESCORE = "rescore";


    // ***************************************查询root结束***************************************


    // ***************************************查询query开始***************************************
    /**
     * 布尔。包含使用must、should、must_not、filter等。
     * "bool": {
     * "must": { "match":  { "email": "business opportunity" }},
     * "should": [
     * { "match":    { "starred": true }},
     * { "bool": {
     * "must":   { "match": { "folder": "inbox" }},
     * "must_not": { "match": { "spam": true }}
     * }}
     * ],
     * "minimum_should_match": 1
     * }
     */
    String BOOL = "bool";
    /**
     * 过滤
     */
    String FILTER = "filter";
    /**
     * 与
     */
    String MUST = "must";
    /**
     * 或
     */
    String SHOULD = "should";
    /**
     * 最小匹配度，跟在should后
     */
    String MINIMUM_SHOULD_MATCH = "minimum_should_match";
    /**
     * 非
     */
    String MUST_NOT = "must_not";
    /**
     * 关键词项， "term": { "age":26}
     */
    String TERM = "term";
    /**
     * 关键词项数组，
     * "terms": {
     * "tag": [ "search", "full_text", "nosql" ]
     * }
     */
    String TERMS = "terms";
    /**
     * 匹配查询，  "match": { "content": "中国杭州" }
     */
    String MATCH = "match";
    /**
     * <pre>
     * 分词匹配-短语匹配查询
     * 如: "match_phrase": { "content": "中国杭州" }
     * 结果: 会匹配到"%中国杭州%"的数据
     *
     * </pre>
     */
    String MATCH_PHRASE = "match_phrase";

    /**
     * <pre>
     * 分词匹配-短语匹配查询-对最后一个分词进行通配符匹配
     * 如: "match_phrase_prefix": { "content": "abc" }
     * 结果: 会匹配到"aaa abc def","abcdef"的数据
     *
     * </pre>
     */
    String MATCH_PHRASE_PREFIX = "match_phrase_prefix";

    /**
     * <pre>
     * 前缀查询-对前缀后内容做匹配，类似sql中的like
     * 如: "prefix": { "content": "abc" }
     * 结果: 会匹配到前缀为"abc"的数据
     *
     * </pre>
     */
    String PREFIX = "prefix";

    /**
     * 查询所有文档，"match_all": {}
     */
    String MATCH_ALL = "match_all";

    /**
     * 分组标记
     */
    String AGGS = "aggs";

    /**
     * 指定多个字段
     * "multi_match": {
     * "query":    "full text search",
     * "fields":   [ "title", "body" ]
     * }
     */
    String MULTI_MATCH = "multi_match";
    /**
     * 属性
     */
    String FIELDS = "fields";
    /**
     * 分词项的共同前缀长度，"range": {"age": {"gte":  20,"lt":   30}}
     */
    String RANGE = "range";
    /**
     * 大于
     */
    String GT = "gt";
    /**
     * 大于等于
     */
    String GTE = "gte";
    /**
     * 小于
     */
    String LT = "lt";
    /**
     * 小于等于
     */
    String LTE = "lte";
    /**
     * 存在 "exists": {"field": "title"}
     */
    String EXISTS = "exists";
    /**
     * 属性
     */
    String FIELD = "field";
    /**
     * 模糊查询
     */
    String FUZZY = "fuzzy";
    /**
     * 相关性提高权重
     */
    String BOOST = "boost";
    /**
     * 匹配的最小相似度
     */
    String MIN_SIMILARITY = "min_similarity";
    /**
     * 分词项的共同前缀长度
     */
    String PREFIX_LENGTH = "prefix_length";
    /**
     *
     */
    String SOURCE = "source";
    /**
     * 值
     */
    String VALUE = "value";
    /**
     *
     */
    String QUERY_STRING = "query_string";
    /**
     * 缓存
     */
    String CACHE = "_cache";
    /**
     * 方法
     */
    String FUNCTION_SCORE = "function_score";
    /**
     * 方法集
     */
    String FUNCTIONS = "functions";
    /**
     * 高斯衰减函数名
     */
    String GAUSS = "gauss";
    /**
     * 起始值
     */
    String ORIGIN = "origin";
    /**
     * 级别因子
     */
    String SCALE = "scale";
    /**
     * 补偿系数
     */
    String OFFSET = "offset";
    /**
     * 衰减系数
     */
    String DECAY = "decay";
    /**
     * 脚本打分方法
     */
    String SCRIPT_SCORE = "script_score";
    /**
     * 脚本
     */
    String SCRIPT = "script";
    /**
     * 脚本行
     */
    String INLINE = "inline";
    /**
     * 权重
     */
    String WEIGHT = "weight";
    /**
     * 函数内部各方法怎样汇总
     */
    String SCORE_MODE = "score_mode";
    /**
     * 与query查询分的计算方式
     */
    String BOOST_MODE = "boost_mode";
    /**
     * score_mode或boost_mode或其他的运算方式
     */
    String MULTIPLY = "multiply";
    /**
     * score_mode或boost_mode或其他的运算方式
     */
    String REPLACE = "replace";
    /**
     * score_mode或boost_mode或其他的运算方式
     */
    String SUM = "sum";
    /**
     * score_mode或boost_mode或其他的运算方式
     */
    String AVG = "avg";
    /**
     * score_mode或boost_mode或其他的运算方式
     */
    String MAX = "max";
    /**
     * score_mode或boost_mode或其他的运算方式
     */
    String MIN = "min";

    // ***************************************查询query结束***************************************

    // ***************************************查询其他项开始***************************************
    /**
     * 顺序
     */
    String ASC = "asc";
    /**
     * 逆序
     */
    String DESC = "desc";

    /**
     * 获取真实的总数开关
     */
    String TRACK_TOTAL_HITS = "track_total_hits";

// ***************************************查询其他项结束***************************************
    String DEFAULT = "default";
}
