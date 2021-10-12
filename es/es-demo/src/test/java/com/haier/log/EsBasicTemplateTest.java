package com.haier.log;

import com.alibaba.fastjson.JSONObject;
import com.haier.log.configuration.OperationLogConfiguration;
import com.haier.log.document.OperationLogDocument;
import com.haier.log.service.EsBasicTemplate;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.ScrolledPage;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

//@SpringBootTest
//@RunWith(SpringRunner.class)
class EsBasicTemplateTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(EsBasicTemplateTest.class);
    @Resource
    private EsBasicTemplate template;
    @Resource
    private ElasticsearchRestTemplate es;
    @Resource
    private OperationLogConfiguration operationLogConfiguration;

    @BeforeEach
    void setUp() {
    }

//    @Test
    void test_insert_success() {
        template.insertByLogFile("D:\\haier\\work\\damand\\elastic\\paas.log");
    }

//    @Test
    void insertOne() {
    }

//    @Test
    void deleteById() {
        template.deleteIndex("paas-log*");
    }

//    @Test
    void deleteByConditaion() {
        DeleteQuery deleteQuery = new DeleteQuery();
//        TermQueryBuilder query = QueryBuilders.termQuery("request", "sec_js");
        BoolQueryBuilder query = QueryBuilders
                .boolQuery()
                .must(QueryBuilders.matchQuery("@timestamp", "2021-09-08T23:54:37.738Z"));
        deleteQuery.setQuery(query);
        deleteQuery.setIndex("nginx-access-2021-09-08");
        deleteQuery.setType("_doc");
        deleteQuery.setScrollTimeInMillis(60000L);
        es.delete(deleteQuery);
    }

//    @Test
    void test_index() {
        template.deleteIndex("paas-log*");
    }

//    @Test
    void test_query_by_condition_success() {
        List<Map> value = template.queryByConditaion(new JSONObject().fluentPut("value", "start"));
        LOGGER.info("match count: {}", value.size());
        Assertions.assertNotNull(value);
    }

//    @Test
    void test_query() {

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

        LocalDateTime now = LocalDateTime.now(Clock.systemUTC());
        String start = now.minusHours(3).atZone(ZoneId.of("Asia/Shanghai")).format(dateTimeFormatter);
        String end = now.atZone(ZoneId.of("Asia/Shanghai")).format(dateTimeFormatter);

        BoolQueryBuilder queryBuilder = QueryBuilders
                .boolQuery()
                .must(QueryBuilders.matchPhraseQuery("request", "managecenter-api"))
                .must(QueryBuilders.matchPhraseQuery("request", "login"))
                .must(QueryBuilders.rangeQuery("time_local").gte(start).lte(end));
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withIndices("nginx-access*")
                .withTypes("_doc")
                .withQuery(queryBuilder)
                .withPageable(PageRequest.of(1, 1000))
//                .withSort(SortBuilders.fieldSort("time.keyword").order(SortOrder.ASC))
                .build();

        ScrolledPage<Map> maps1 = es.startScroll(60000, query, Map.class);
        LOGGER.info("match result: {}", maps1);
    }

//    @Test
    void test_create_index() {
    }

//    @Test
    void test_insert() {
//        LogDocument logDocument = new LogDocument();
//        logDocument.setId("1");
//        logDocument.setTime("2021-09-07 13:00:00");
//        logDocument.setValue("start test ----");
//        logDocument.setThreadName("[thread-1]");
//        logDocument.setLogLevel("INFO");
//        IndexQuery indexQuery = new IndexQueryBuilder().withObject(logDocument).build();

        IndexQuery indexQuery = new IndexQueryBuilder()
                .withIndexName("paas-log-test")
                .withType("logdocument")
                .withId("2")
                .withSource(new JSONObject().fluentPut("id", "2").fluentPut("time", "2021-09-07 14:00:00")
                        .fluentPut("value", "end test ----").fluentPut("threadName", "[thread-1]").fluentPut("logLevel", "INFO").toJSONString())
                .build();
        String index = es.index(indexQuery);
        LOGGER.info("index: {}", index);
    }

//    @Test
    void test_current_create_index() throws IOException {
        System.out.println(es.createIndex(OperationLogDocument.class));
        es.putMapping(OperationLogDocument.class);

        operationLogConfiguration.setDynamicIndex("operation-log-202110");
        System.out.println(es.createIndex(OperationLogDocument.class));
    }
}