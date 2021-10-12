package com.haier.log.document;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Mapping;

@Mapping(mappingPath = "mapping.json")
@Document(indexName = "#{operationLogConfiguration.dynamicIndex}", type = "log")
public class OperationLogDocument {
}
