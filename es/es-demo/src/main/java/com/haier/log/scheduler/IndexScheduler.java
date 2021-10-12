package com.haier.log.scheduler;

import com.haier.log.configuration.OperationLogConfiguration;
import com.haier.log.document.OperationLogDocument;
import com.haier.log.helper.ConfigHelper;
import com.haier.log.helper.DistributedLock;
import com.haier.log.service.EsTemplate;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Configuration
public class IndexScheduler implements SchedulingConfigurer {
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexScheduler.class);
    private static final DateTimeFormatter DATA_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");
    @Value("${es.index.generation.cron}")
    private String cron;
    @Resource
    private EsTemplate<OperationLogDocument> esTemplate;
    @Resource
    private DistributedLock distributedLock;
    @Resource
    private OperationLogConfiguration logConfiguration;
    @Resource
    private ConfigHelper config;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        Trigger trigger = context -> new CronTrigger(cron).nextExecutionTime(context);
        taskRegistrar.addTriggerTask(() -> {
            Thread.currentThread().setName("schedule-create-index");
            String index = String.join(StringUtils.EMPTY, config.indexPrefix(), LocalDate.now().plusMonths(1).format(DATA_FORMATTER));
            logConfiguration.setDynamicIndex(index);

            RLock lock = distributedLock.getLock();
            lock.lock();
            esTemplate.createIndex(OperationLogDocument.class);
            lock.unlock();
            LOGGER.info("scheduler success create index, {}", index);
        }, trigger);
    }

    public void setCron(String cron) {
        this.cron = cron;
    }
}
