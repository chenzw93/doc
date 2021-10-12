package com.haier.log;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


//@SpringBootTest
//@RunWith(SpringRunner.class)
class EsApplicationTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(EsApplicationTests.class);
    private static final Pattern PATTERN = Pattern.compile("^(\\d{4})([/-]\\d{2}){2}\\s\\d{1,2}(:\\d{1,2}){2}.*$");
    @Resource
    private ElasticsearchRestTemplate restTemplate;

//    @Test
    void test_insert_onebyone() throws IOException {
        List<String> list = readLine(Paths.get("D:\\haier\\work\\damand\\elastic\\paas.log"));
        String line = list.get(0);
        if (PATTERN.matcher(line).matches()) {
            String[] s = line.split(" ");
            Map<String, String> cell = new HashMap<>();
            cell.put("time", Joiner.on(" ").join(s[0], s[1]));
            cell.put("thread-name", s[2]);
            cell.put("log-level", s[3]);
            cell.put("value", Joiner.on(" ").join(Arrays.copyOfRange(s, 4, s.length)));
            IndexQuery build = new IndexQueryBuilder().withObject(cell).build();
//            restTemplate.createIndex("paas-log");
            restTemplate.index(build);
        }
//        list.stream().forEach(x -> {
//            if (PATTERN.matcher(x).matches()) {
//                String[] s = x.split(" ");
//                Map<String, String> cell = new HashMap<>();
//                cell.put("time", Joiner.on(" ").join(s[0], s[1]));
//                cell.put("thread-name", s[2]);
//                cell.put("log-level", s[3]);
//                cell.put("value", Joiner.on(" ").join(Arrays.copyOfRange(s, 4, s.length)));
//                IndexQuery build = new IndexQueryBuilder().withObject(cell).build();
//                restTemplate.index(build, IndexCoordinates.of("paas-log"));
//            }
//        });
    }

    /**
     * bulkIndex: request entity limit
     *
     * @throws IOException
     */
//    @Test
    void test_insert_batch() throws IOException {
        List<String> list = readLine(Paths.get("D:\\haier\\work\\damand\\elastic\\paas_bak.log"));
        AtomicLong id = new AtomicLong(1);
        List<IndexQuery> indexs = list.stream().map(x -> {
            String[] s = x.split(" ");
            Map<String, String> cell = new HashMap<>();
            cell.put("time", Joiner.on(" ").join(s[0], s[1]));
            cell.put("thread-name", s[2]);
            cell.put("log-level", s[3]);
            cell.put("value", Joiner.on(" ").join(Arrays.copyOfRange(s, 4, s.length)));
            return new IndexQueryBuilder().withId(String.valueOf(id.getAndIncrement())).withObject(cell).build();
        }).collect(Collectors.toList());
        LOGGER.info("id: {}", id.get());
        Lists.partition(indexs, 50000).forEach(x -> restTemplate.bulkIndex(x));
//        Lists.partition(indexs, 50000).forEach(x -> restTemplate.bulkIndex(x, IndexCoordinates.of("paas-log")));
    }

//    @Test
    void test_query() {
        NativeSearchQuery query = new NativeSearchQueryBuilder().withIndices("paas-log").withTypes("_doc").withQuery(QueryBuilders.matchQuery("value", "start")).build();
        List<Map> maps = restTemplate.queryForList(query, Map.class);
        LOGGER.info("total: {}", maps);

    }

//    @Test
//    void test_delete() {
//        Criteria criteria = new Criteria("time");
//        criteria.matchesAll("2021-09-02");
//        Query query = new CriteriaQuery(criteria);
//        restTemplate.delete(query, Map.class, IndexCoordinates.of("paas-log"));
//    }

    List<String> readLine(Path path) {
        List<String> content = new ArrayList<>(300000);
        List<String> temp = new ArrayList<>();
        try (BufferedReader bufferedReader = Files.newBufferedReader(path)) {
            String line = null;
            while (Objects.nonNull(line = bufferedReader.readLine())) {
                if (PATTERN.matcher(line).matches()) {
                    if (!CollectionUtils.isEmpty(temp)) {
                        temp.add(0, content.remove(content.size() - 1));
                        content.add(Joiner.on("").join(temp));
                        temp.clear();
                    }
                    content.add(line);
                    continue;
                }
                temp.add(line);
            }
        } catch (IOException e) {

        }
        LOGGER.info("list: {}", content.size());
        return content;
    }

}
