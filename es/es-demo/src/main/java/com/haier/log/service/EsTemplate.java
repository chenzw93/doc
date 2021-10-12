package com.haier.log.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.haier.log.helper.ConfigHelper;
import com.haier.log.helper.GeoHelper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.ScrolledPage;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class EsTemplate<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(EsTemplate.class);
    private static final DateTimeFormatter STANDARD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter UTC_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);
    private static final DateTimeFormatter INDEX_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    @Resource
    private ElasticsearchRestTemplate template;
    @Resource
    private ConfigHelper config;
    @Resource
    private GeoHelper geoHelper;

    /**
     * 指定时间范围下，某个请求、指定参数访，请求了多少次
     * <p>
     * 某个账号登录了多少次
     *
     * @param param
     * @return
     */
    public Map<String, Object> statistics(JSONObject param) {
        BoolQueryBuilder queryBuilder = timeBuilder(param);
        for (String key : param.getString("url").split("/")) {
            if (StringUtils.isBlank(key)) {
                continue;
            }
            queryBuilder.must(QueryBuilders.matchPhraseQuery("request", key));
        }
        queryBuilder.must(QueryBuilders.matchQuery("request_body", param.getString("content")));

        NativeSearchQuery doc = new NativeSearchQueryBuilder()
                .withIndices(config.indexPrefix())
                .withTypes(config.type())
                .withQuery(queryBuilder)
                .addAggregation(AggregationBuilders.terms("status_term").field("status"))
                .withPageable(PageRequest.of(0, 10))
                .build();
        AggregatedPage<Map> aggregatedPage = template.queryForPage(doc, Map.class);
        Terms status_term = (Terms) aggregatedPage.getAggregations().getAsMap().get("status_term");

        return new JSONObject().fluentPut("data", status_term.getBuckets().stream().map(x ->
                new JSONObject().fluentPut("status", x.getKey()).fluentPut("count", x.getDocCount())
        ).collect(Collectors.toList())).fluentPut("retCode", 20000).fluentPut("retInfo", "success");
    }

    /**
     * 某个时间范围内，请求数量排序前多少名
     * numOfTop : 请求总数排序前多少名，默认为10
     *
     * @param param
     * @return
     */
    public Map<String, Object> requestCount(JSONObject param) {
        BoolQueryBuilder queryBuilder = timeBuilder(param);
        int batchSize = Objects.isNull(param.getInteger("numOfTop")) ? 10 : param.getInteger("numOfTop");
        NativeSearchQuery doc = new NativeSearchQueryBuilder()
                .withIndices(config.indexPrefix())
                .withTypes(config.type())
                .addAggregation(AggregationBuilders.terms("request_terms").field("request.keyword").size(batchSize))
                .withQuery(queryBuilder)
                .withPageable(PageRequest.of(0, 10))
                .build();
        AggregatedPage<Map> aggregatedPage = template.queryForPage(doc, Map.class);
        Terms request_terms = (Terms) aggregatedPage.getAggregations().getAsMap().get("request_terms");

        return new JSONObject().fluentPut("data", request_terms.getBuckets().stream().map(x ->
                new JSONObject().fluentPut("url", x.getKey()).fluentPut("count", x.getDocCount())
        ).collect(Collectors.toList())).fluentPut("retCode", 20000).fluentPut("retInfo", "success");
    }

    public void save(List<String> data) {
        List<IndexQuery> index = data.stream().filter(StringUtils::isNoneBlank).map(x -> {
            JSONObject param = JSON.parseObject(x);
            Long startTime = param.getLong("startTime");
            Long endTime = param.getLong("endTime");
            param.put("cost", endTime - startTime);
            param.computeIfPresent("startTime", (y, z) -> LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(String.valueOf(z))), ZoneId.systemDefault()));
            param.computeIfPresent("endTime", (y, z) -> LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(String.valueOf(z))), ZoneId.systemDefault()));

            param.put("userName", "未知");
            param.put("userEmail", "未知");
            param.put("userMobile", "未知");

            geoHelper.location(param);
            param.computeIfPresent("clientIp", (z, y) -> ObjectUtils.isEmpty(y) ? "127.0.0.1" : y);

            return new IndexQueryBuilder()
                    .withIndexName(Joiner.on(StringUtils.EMPTY).join(config.indexPrefix(), milli2YearMonth(startTime)))
                    .withType(config.type())
                    .withSource(param.toJSONString()).build();
        }).collect(Collectors.toList());
        template.bulkIndex(index);
    }

    public List<Map> query(JSONObject param) {
        RangeQueryBuilder timeQuery = QueryBuilders.rangeQuery("startTime")
                .gte(str2Milli(param.getString("startTime")))
                .lte(str2Milli(param.getString("endTime")));
        BoolQueryBuilder builder = QueryBuilders.boolQuery().must(timeQuery);
        if (StringUtils.isNotBlank(param.getString("userName"))) {
            builder.must(QueryBuilders.matchPhraseQuery("user_name", param.getString("user_name")));
        }
        if (StringUtils.isNotBlank(param.getString("interface"))) {
            builder.must(QueryBuilders.matchPhraseQuery("interface_name", param.getString("interface_name")));
        }
        NativeSearchQuery doc = new NativeSearchQueryBuilder()
                .withIndices("gateway*")
                .withTypes("log")
                .withQuery(builder)
                .withPageable(PageRequest.of(0, 50))
                .build();
        ScrolledPage<Map> maps1 = template.startScroll(60000, doc, Map.class);
        List<Map> result = new ArrayList<>();
        if (maps1.hasContent()) {
            String scrollId = maps1.getScrollId();
            result.addAll(maps1.getContent());
            while (true) {
                ScrolledPage<Map> maps = template.continueScroll(scrollId, 60000, Map.class);
                if (maps.hasContent()) {
                    result.addAll(maps.getContent());
                    continue;
                }
                break;
            }
        }

        return result;
    }

    /**
     * 联系人 接口名 时间范围
     *
     * @param param
     * @return
     */
    public List<Map> queryByConditional(JSONObject param) {
        LocalDateTime startTime = LocalDateTime.parse(param.getString("start_time"), STANDARD_FORMATTER);
        LocalDateTime endTime = LocalDateTime.parse(param.getString("end_time"), STANDARD_FORMATTER);
        RangeQueryBuilder timeQuery = QueryBuilders.rangeQuery("startTime").gte(startTime).lte(endTime);
        BoolQueryBuilder builder = QueryBuilders.boolQuery().must(timeQuery);
        if (StringUtils.isNotBlank(param.getString("contact"))) {
            builder.must(QueryBuilders.matchPhraseQuery("userName", param.getString("contact")));
        }
        if (StringUtils.isNotBlank(param.getString("interface_name"))) {
            builder.must(QueryBuilders.matchPhraseQuery("interface_name", param.getString("interface_name")));
//            builder.must(QueryBuilders.matchPhraseQuery("interfaceName", param.getString("interface_name")));
        }
        NativeSearchQuery doc = new NativeSearchQueryBuilder()
                .withIndices(config.indexFuzzy())
                .withTypes(config.type())
                .withQuery(builder)
                .withPageable(PageRequest.of(0, 20))
                .build();
        ScrolledPage<Map> maps1 = template.startScroll(60000, doc, Map.class);
        List<Map> result = new ArrayList<>();
        if (maps1.hasContent()) {
            String scrollId = maps1.getScrollId();
            result.addAll(maps1.getContent());
            while (true) {
                ScrolledPage<Map> maps = template.continueScroll(scrollId, 60000, Map.class);
                if (maps.hasContent()) {
                    result.addAll(maps.getContent());
                    continue;
                }
                break;
            }
        }
        return result;
    }

    public void createIndex(Class<T> documentClass) {
        if (template.indexExists(documentClass)) {
            return;
        }
        template.createIndex(documentClass);
        template.putMapping(documentClass);
    }

    private BoolQueryBuilder timeBuilder(JSONObject param) {
        String startDate = param.getString("startTime");
        String endDate = param.getString("endTime");
        String utcStart = LocalDateTime.parse(startDate, STANDARD_FORMATTER).minusHours(8).atZone(ZoneId.of("UTC")).format(UTC_FORMATTER);
        String utcEnd = LocalDateTime.parse(endDate, STANDARD_FORMATTER).minusHours(8).atZone(ZoneId.of("UTC")).format(UTC_FORMATTER);
        RangeQueryBuilder timeQuery = QueryBuilders.rangeQuery("time_local").gte(utcStart).lte(utcEnd);
        return QueryBuilders.boolQuery().must(timeQuery);
    }

    private long str2Milli(String time) {
        return LocalDateTime.parse(time, STANDARD_FORMATTER).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private String milli2YearMonth(long time) {
        return INDEX_FORMATTER.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()));
    }
}
