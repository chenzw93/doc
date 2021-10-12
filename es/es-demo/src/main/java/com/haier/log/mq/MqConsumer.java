package com.haier.log.mq;

import com.haier.log.document.OperationLogDocument;
import com.haier.log.service.EsTemplate;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MqConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(MqConsumer.class);

    @Resource
    private EsTemplate<OperationLogDocument> esTemplate;

    /**
     * 一次处理几条？
     * 异常消息如何处理?消息如何确认?
     *
     * @param record
     */
    @KafkaListener(topics = {"testaaa"}, clientIdPrefix = "test", groupId = "test_log_group", containerFactory = "batchFactory")
    public void receiver(ConsumerRecords<String, String> record, Acknowledgment ack) {
        List<String> messages = record.partitions().stream()
                .flatMap(x -> record.records(x).stream())
                .map(ConsumerRecord::value)
                .filter(StringUtils::isNoneBlank)
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(messages)) {
            LOGGER.info("----> to es: {}", messages);
            esTemplate.save(messages);
        }
        LOGGER.info("record： {}", record.count());
        ack.acknowledge();
    }
}
