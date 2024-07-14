package org.kangspace.messagepush.core.elasticsearch.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 分组查询结果基础Bean
 * <pre>
 * 查询:
 *    GET message-gateway.index_operators_info/_search
 * {
 *   "size": 0,
 *   "query": {
 *     "bool": {
 *       "filter": [
 *         {
 *           "range": {
 *             "reportTime": {
 *               "gt": "2022-05-10 14:00:00",
 *               "lt": "2022-05-10 15:00:00"
 *             }
 *           }
 *         }
 *       ],
 *       "must": [],
 *       "must_not": [],
 *       "should": []
 *     }
 *   },
 *   "aggs": {
 *     "totalSmsCountPerHourGroup": {
 *       "date_histogram": {
 *         "field": "reportTime",
 *         "interval": "hour",
 *         "format": "yyyy-MM-dd HH"
 *       },
 *       "aggs": {
 *         "smsSendResultGroup": {
 *           "terms": {
 *             "field": "operatorsSendStatus",
 *             "size": 10
 *           },
 *           "aggs": {
 *             "smsTypeGroup": {
 *               "terms": {
 *                 "field": "smsType",
 *                 "size": 10
 *               },
 *               "aggs": {
 *                 "serviceProviderGroup": {
 *                   "terms": {
 *                     "field": "operatorsType",
 *                     "size": 10
 *                   },
 *                   "aggs": {
 *                     "smsSizeSum": {
 *                       "sum": {
 *                         "field": "smsSize"
 *                       }
 *                     }
 *                   }
 *                 }
 *               }
 *             }
 *           }
 *         }
 *       }
 *     }
 *   }
 * }
 * 结果样例:
 *    "aggregations" : {
 *     "agroup" : {
 *       "value" : 1.65625
 *     },
 *     "totalSmsCountPerHourGroup" : {
 *       "buckets" : [
 *         {
 *           "key_as_string" : "2022-05-10 14",
 *           "key" : 1652191200000,
 *           "doc_count" : 32,
 *           "smsSendResultGroup" : {
 *             "doc_count_error_upper_bound" : 0,
 *             "sum_other_doc_count" : 0,
 *             "buckets" : [
 *               {
 *                 "key" : "3",
 *                 "doc_count" : 31,
 *                 "smsTypeGroup" : {
 *                   "doc_count_error_upper_bound" : 0,
 *                   "sum_other_doc_count" : 0,
 *                   "buckets" : [
 *                     {
 *                       "key" : "2",
 *                       "doc_count" : 20,
 *                       "serviceProviderGroup" : {
 *                         "doc_count_error_upper_bound" : 0,
 *                         "sum_other_doc_count" : 0,
 *                         "buckets" : [
 *                           {
 *                             "key" : "6",
 *                             "doc_count" : 20,
 *                             "smsSizeSum" : {
 *                               "value" : 41.0
 *                             }
 *                           }
 *                         ]
 *                       }
 *                     },
 *                     {
 *                       "key" : "1",
 *                       "doc_count" : 8,
 *                       "serviceProviderGroup" : {
 *                         "doc_count_error_upper_bound" : 0,
 *                         "sum_other_doc_count" : 0,
 *                         "buckets" : [
 *                           {
 *                             "key" : "6",
 *                             "doc_count" : 8,
 *                             "smsSizeSum" : {
 *                               "value" : 8.0
 *                             }
 *                           }
 *                         ]
 *                       }
 *                     },
 *                     {
 *                       "key" : "3",
 *                       "doc_count" : 3,
 *                       "serviceProviderGroup" : {
 *                         "doc_count_error_upper_bound" : 0,
 *                         "sum_other_doc_count" : 0,
 *                         "buckets" : [
 *                           {
 *                             "key" : "5",
 *                             "doc_count" : 3,
 *                             "smsSizeSum" : {
 *                               "value" : 3.0
 *                             }
 *                           }
 *                         ]
 *                       }
 *                     }
 *                   ]
 *                 }
 *               },
 *               {
 *                 "key" : "2",
 *                 "doc_count" : 1,
 *                 "smsTypeGroup" : {
 *                   "doc_count_error_upper_bound" : 0,
 *                   "sum_other_doc_count" : 0,
 *                   "buckets" : [
 *                     {
 *                       "key" : "1",
 *                       "doc_count" : 1,
 *                       "serviceProviderGroup" : {
 *                         "doc_count_error_upper_bound" : 0,
 *                         "sum_other_doc_count" : 0,
 *                         "buckets" : [
 *                           {
 *                             "key" : "6",
 *                             "doc_count" : 1,
 *                             "smsSizeSum" : {
 *                               "value" : 1.0
 *                             }
 *                           }
 *                         ]
 *                       }
 *                     }
 *                   ]
 *                 }
 *               }
 *             ]
 *           }
 *         }
 *       ]
 *     }
 *   }
 *
 *
 *
 * </pre>
 *
 * @author kango2gler@gmail.com
 * @since 2022/5/16
 */
@Data
public class AggregationsResult extends Result {
    /**
     * key: group name
     * value: {buckets...}
     */
    Map<String, GroupBuckets> aggregations;


    /**
     * 分组结果
     */
    @Data
    public static class GroupBuckets {
        /**
         * buckets列表
         */
        private List<GroupBucketDetail> buckets;

        private Long docCountErrorUpperBound;
        private Long sumOtherDocCount;
        /**
         * 简单分组函数值(key: 分组名称, value: 分组结果),可能为double类型.
         * 如sum, avg分组等
         */
        private Object singleNumericValue;
    }

    /**
     * 分组结果详情
     */
    @Data
    public static class GroupBucketDetail {
        /**
         * 内部分组
         * key: group name
         * value: {buckets...}
         */
        Map<String, GroupBuckets> aggs;
        private Object key;
        private String keyAsString;
        /**
         * 文档总数
         */
        private Long docCount;

        public GroupBucketDetail() {
        }

        public GroupBucketDetail(Object key, String keyAsString, Long docCount) {
            this.key = key;
            this.keyAsString = keyAsString;
            this.docCount = docCount;
        }


    }
}
