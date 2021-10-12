package com.haier.log.configuration;

import com.haier.log.document.OperationLogDocument;
import com.haier.log.helper.ConfigHelper;
import com.haier.log.helper.DistributedLock;
import com.haier.log.service.EsTemplate;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


@Setter
@Getter
@Component("operationLogConfiguration")
public class OperationLogConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(OperationLogConfiguration.class);
    private static final DateTimeFormatter DATA_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");
    @Resource
    private ConfigHelper helper;
    @Resource
    private DistributedLock distributedLock;
    @Resource
    private EsTemplate<OperationLogDocument> esTemplate;

    private String dynamicIndex;

    @PostConstruct
    private void init() {
        this.dynamicIndex = String.join(StringUtils.EMPTY, helper.indexPrefix(), LocalDate.now().format(DATA_FORMATTER));
        LOGGER.info("after service start, start create index : {}", dynamicIndex);
        RLock lock = distributedLock.getLock();
        lock.lock();
        esTemplate.createIndex(OperationLogDocument.class);
        lock.unlock();
    }
}
