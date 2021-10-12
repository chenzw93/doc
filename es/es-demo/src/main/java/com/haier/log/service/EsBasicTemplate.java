package com.haier.log.service;

import com.google.common.base.Joiner;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.ScrolledPage;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.stereotype.Component;
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

@Component
@Deprecated
public class EsBasicTemplate {
    private static final Logger LOGGER = LoggerFactory.getLogger(EsBasicTemplate.class);
    private static final Pattern PATTERN = Pattern.compile("^(\\d{4})([/-]\\d{2}){2}\\s\\d{1,2}(:\\d{1,2}){2}.*$");

    @Resource
    private ElasticsearchRestTemplate template;

    /**
     * id is random
     *
     * @param filePath log file
     */
    public void insertByLogFile(String filePath) {
        List<String> content = readLine(Paths.get(filePath));
        AtomicLong id = new AtomicLong(1);
        List<IndexQuery> indexs = content.stream().map(x -> {
            String[] s = x.split(" ");
            Map<String, String> cell = new HashMap<>();
            cell.put("time", Joiner.on(" ").join(s[0], s[1]));
            cell.put("thread-name", s[2]);
            cell.put("log-level", s[3]);
            cell.put("value", Joiner.on(" ").join(Arrays.copyOfRange(s, 4, s.length)));
            return new IndexQueryBuilder().withIndexName(Joiner.on("_").join("paas-log", s[0])).withType("_doc").withId(String.valueOf(id.getAndIncrement())).withObject(cell).build();
        }).collect(Collectors.toList());

        template.bulkIndex(indexs);
    }

    /**
     * The document format is Json
     *
     * @param document data
     * @param id       unique identifier
     */
    public void insertOne(String document, String id) {
        IndexQuery build = new IndexQueryBuilder().withSource(document).withIndexName("paas-log").withType("_doc").withId(id).build();
        template.index(build);
    }

    /**
     * delete by _id
     *
     * @param id document id
     */
    public void deleteById(String id) {
        template.delete("paas-log", "_doc", id);
    }

    /**
     * match delete
     *
     * @param time match time
     */
    public void deleteByConditaion(String time) {
        DeleteQuery deleteQuery = new DeleteQuery();
        BoolQueryBuilder query = QueryBuilders
                .boolQuery()
                .must(QueryBuilders.matchQuery("@timestamp", time));
        deleteQuery.setQuery(query);
        deleteQuery.setIndex("nginx-access*");
        deleteQuery.setType("_doc");
        deleteQuery.setScrollTimeInMillis(60000L);
        template.delete(deleteQuery);
    }

    /**
     * delete index
     *
     * @param name index name
     */
    public void deleteIndex(String name) {
        boolean deleted = template.deleteIndex(name);
        LOGGER.info("delete result: {}", deleted);
    }

    /**
     * simple query
     *
     * @param param
     * @return
     */
    public List<Map> queryByConditaion(Map<String, Object> param) {
        BoolQueryBuilder queryBuilder = QueryBuilders
                .boolQuery()
                .must(QueryBuilders.matchQuery("value", param.get("value")));
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withIndices("paas-log*")
                .withTypes("_doc")
                .withQuery(queryBuilder)
                .withPageable(PageRequest.of(1, 1000))
                .withSort(SortBuilders.fieldSort("time.keyword").order(SortOrder.ASC))
                .build();

        ScrolledPage<Map> maps1 = template.startScroll(60000, query, Map.class);
        template.continueScroll(maps1.getScrollId(), 60000, Map.class);
        return maps1.getContent();
    }

    private List<String> readLine(Path path) {
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
