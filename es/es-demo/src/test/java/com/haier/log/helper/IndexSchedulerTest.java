package com.haier.log.helper;

import com.haier.log.scheduler.IndexScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

//@SpringBootTest
//@RunWith(SpringRunner.class)
class IndexSchedulerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexSchedulerTest.class);
    @Resource
    IndexScheduler indexScheduler;

//    @Test
    void test_spring_task() throws InterruptedException {
        TimeUnit.SECONDS.sleep(7);
        LOGGER.info("------------reset cron-------");
        indexScheduler.setCron("0/1 * * * * ? ");
        TimeUnit.SECONDS.sleep(5);
    }

}