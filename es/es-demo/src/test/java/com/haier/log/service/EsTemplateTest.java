package com.haier.log.service;

import com.alibaba.fastjson.JSONObject;
import com.haier.log.document.OperationLogDocument;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

//@SpringBootTest
//@RunWith(SpringRunner.class)
public class EsTemplateTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(EsTemplateTest.class);
    @Resource
    private EsTemplate<OperationLogDocument> template;
    @Resource
    private ElasticsearchRestTemplate es;

    /**
     * 在指定时间段，接口 /managecenter-api/login 入参包含 admin@uplus.com 调用的次数,按请求是否成功进行统计
     */
//    @Test
    void test_count_by_conditional_success() {
        JSONObject param = new JSONObject().fluentPut("url", "/managecenter-api/login")
                .fluentPut("startTime", "2021-09-14 12:00:00")
                .fluentPut("endTime", "2021-09-14 18:00:00")
                .fluentPut("content", "sanbudeyu2020@sina.com");
        Map<String, Object> statistics = template.statistics(param);
        LOGGER.info("result: {}", statistics);
    }

    /**
     * 指定时间段下，所有接口访问次数排序前5
     */
//    @Test
    void test_requestCount_success() {
        JSONObject param = new JSONObject().fluentPut("startTime", "2021-09-14 16:30:00").fluentPut("endTime", "2021-09-14 17:00:00").fluentPut("numOfTop", 5);
        Map<String, Object> statistics = template.requestCount(param);
        LOGGER.info("result: {}", statistics);
        Assertions.assertNotNull(statistics);
    }

//    @Test
    void test_gateway_query() {
        List<Map> result = template.query(new JSONObject().fluentPut("startTime", "2021-09-18 13:00:00").fluentPut("endTime", "2021-09-18 15:15:00"));
        LOGGER.info("query: {}", result.size());
    }

//    @Test
    void test_save() {
//        long m = Instant.now().toEpochMilli();
//        System.out.println(m);
        template.save(Arrays.asList("{\"clientIp\":\"\",\"endTime\":1632304475603,\"method\":\"POST\",\"param\":\"{\\\"appDetailsVersionId\\\":\\\"3\\\"}\",\"path\":\"/devicecustomui/appDetailsVersionManager/getVersionDetails\",\"responseBody\":{\"data\":{},\"retInfo\":\"操作成功\",\"retCode\":\"00000\"},\"serverName\":\"详情页服务\",\"startTime\":1632304475603,\"userEmail\":\"sanbudeyu2020@sina.com\",\"userId\":\"100013957366166399\",\"userMobile\":\"\"}"));
    }

    private static final DateTimeFormatter STANDARD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

//    @Test
    void test_query_time_range() {
        List<Map> maps = template.queryByConditional(new JSONObject().fluentPut("start_time", "2021-09-22 13:54:00").fluentPut("end_time", "2021-09-24 13:54:00").fluentPut("contact", "散步").fluentPut("interface_name", "常见问题"));
//        LocalDateTime startTime = LocalDateTime.parse("2021-09-22 14:54:00", STANDARD_FORMATTER);
//        LocalDateTime endTime = LocalDateTime.parse("2021-09-24 14:54:00", STANDARD_FORMATTER);
//        RangeQueryBuilder timeQuery = QueryBuilders.rangeQuery("startTime").gte(startTime).lte(endTime);
//        NativeSearchQuery doc = new NativeSearchQueryBuilder()
//                .withIndices("operation-log-*")
//                .withTypes("log")
//                .withQuery(timeQuery)
//                .withPageable(PageRequest.of(0, 10))
//                .build();
//        AggregatedPage<Map> aggregatedPage = es.queryForPage(doc, Map.class);
        LOGGER.info("aggregatedPage: {}", maps);
    }
}
