package com.haier.log.helper;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConfigHelper {
    @Value("${es.index.type}")
    private String type;

    @Value("${es.index.name.prefix}")
    private String indexPrefix;

    public String type() {
        return type;
    }

    public String indexPrefix() {
        return indexPrefix;
    }

    public String indexFuzzy() {
        return String.join(StringUtils.EMPTY, indexPrefix, "*");
    }
}
