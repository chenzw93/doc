[TOC]

# Kafka



## 介绍

分布式、分区的、多副本的、多订阅者，基于zookeeper协调的分布式日志系统/MQ系统

主要应用场景是：日志收集系统和消息系统

## 优势

- 以时间复杂度为O(1)的方式提供消息持久化能力，即使对TB级以上数据也能保证常数时间的访问性能
- 高吞吐率。即使在非常廉价的商用机器上也能做到单机支持每秒100K条消息的传输
- 支持Kafka Server间的消息分区，及分布式消费，同时保证每个partition内的消息顺序传输
- 同时支持离线数据处理和实时数据处理

## 主要概念

### Broker

kafka服务部署的节点机器

### Topic

kafka的数据逻辑上是以topic为单位进行存储的

消息发布-订阅是通过创建、监听topic来实现的，

### Partition

分区，topic的数据存储是以分区为单位的，创建topic时可以指定分区的个数

消息进入partition中是连续追加的，不会删除消费过的数据

同一个partition中的数据是有序的，但不是不同的partition中数据不一定有序

### Offset

partition中的每条数据都有一个联系的序号，标识了唯一一条数据

#### Current Offset

表示当前消费者下一次获取消息的起始序号

#### Committed Offset

consumer确认消费过的消息的序号，有两种提交方式，`commitSync`  `commitAsync`

主要用于consumer rebalance

在rebalance的过程中，一个partition被分配给了一个consumer，那么这个consumer该从什么位置开始消费消息呢？答案就是Committed Offset

如果一个Consumer消费了2条消息（poll并且成功commitSync）之后宕机了，重新启动之后它仍然能够从第6条消息开始消费，因为Committed Offset已经被Kafka记录为2

### Consumer Group

定义消费者的组别

如果多个消费者监听同一个topic，并且多个消费者配置了同一个消费者组，那么这些消费者执行负载均衡策略；

如果多个消费者监听同一个topic，但是属于多个消费之组，那么消费者都能收到topic的消息，相当于广播；

消费者组的名称可以自己定义

## 安装

### Download

选择想要的版本下载即可

https://kafka.apache.org/downloads

### Configuration

解压后的安装包，内容如下

```she
[cloud-user@lt-haigeek-re-01 kafka_2.11-1.1.0]$ ll
total 52
drwxr-xr-x 3 cloud-user cloud-user  4096 Mar 24  2018 bin
drwxr-xr-x 2 cloud-user cloud-user  4096 Mar 24  2018 config
drwxr-xr-x 2 cloud-user cloud-user  4096 Sep 27 16:01 libs
-rw-r--r-- 1 cloud-user cloud-user 28824 Mar 24  2018 LICENSE
-rw-r--r-- 1 cloud-user cloud-user   336 Mar 24  2018 NOTICE
drwxr-xr-x 2 cloud-user cloud-user  4096 Mar 24  2018 site-docs
[cloud-user@lt-haigeek-re-01 kafka_2.11-1.1.0]$
```

> bin: 启动脚本相关
>
> config：启动kafka的配置文件相关
>
> libs：kafka服务启动依赖的包

其中`config`下的配置文件如下：

```shell
[cloud-user@lt-haigeek-re-01 kafka_2.11-1.1.0]$ ll config/
total 64
-rw-r--r-- 1 cloud-user cloud-user  906 Mar 24  2018 connect-console-sink.properties
-rw-r--r-- 1 cloud-user cloud-user  909 Mar 24  2018 connect-console-source.properties
-rw-r--r-- 1 cloud-user cloud-user 5807 Mar 24  2018 connect-distributed.properties
-rw-r--r-- 1 cloud-user cloud-user  883 Mar 24  2018 connect-file-sink.properties
-rw-r--r-- 1 cloud-user cloud-user  881 Mar 24  2018 connect-file-source.properties
-rw-r--r-- 1 cloud-user cloud-user 1111 Mar 24  2018 connect-log4j.properties
-rw-r--r-- 1 cloud-user cloud-user 2730 Mar 24  2018 connect-standalone.properties
-rw-r--r-- 1 cloud-user cloud-user 1221 Mar 24  2018 consumer.properties
-rw-r--r-- 1 cloud-user cloud-user 4727 Mar 24  2018 log4j.properties
-rw-r--r-- 1 cloud-user cloud-user 1919 Mar 24  2018 producer.properties
-rw-r--r-- 1 cloud-user cloud-user 6851 Mar 24  2018 server.properties
-rw-r--r-- 1 cloud-user cloud-user 1032 Mar 24  2018 tools-log4j.properties
-rw-r--r-- 1 cloud-user cloud-user 1023 Mar 24  2018 zookeeper.properties
```

server.properties 需要关注的点

```shell
# The id of the broker. This must be set to a unique integer for each broker. 指定kafka唯一标识符，如果是kafka集群，值必须全局唯一
broker.id=0

# 暴漏服务的访问地址
listeners=PLAINTEXT://192.168.180.176:9092

# kafka使用的zookeeper节点信息
zookeeper.connect=localhost:2181
# Timeout in ms for connecting to zookeeper
zookeeper.connection.timeout.ms=6000
```

### Run

#### zookeeper

kafka自带了快速单节点的 `zookeeper` 实例，可以通过如下命令启动

```shell
bin/zookeeper-server-start.sh config/zookeeper.properties
```

#### kafka

kafka服务启动命令 

```sh
bin/kafka-server-start.sh config/server.properties
```

##### 创建topic

```shell
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic test
```

> --replication-factor 保存的副本个数
>
> --partitions 分区的个数
>
> --topic 想要创建的topic的名称

查看topic的列表
```shell
bin/kafka-topics.sh --list --zookeeper localhost:2181
```

查看topic的详情

```shell
bin/kafka-topics.sh --zookeeper localhost:2181 --describe --topic test
```



删除topic

```shell
bin/kafka-topics.sh --zookeeper localhost:2181 --delete  --topic test
```

##### 发送消息

```shell
 bin/kafka-console-producer.sh --broker-list localhost:9092 --topic test
```

> --topic 消息发送的topic

注意： 执行完该命令后，就可以在命令行发送消息了

##### 消费消息

```shell
bin/kafka-console-consumer.sh --bootstrap-server 192.168.180.176:9092 --topic testaaa --from-beginning
```

> --bootstrap-server kafka的访问地址
>
> --topic 监听的topic
>
> --from-beginning 是否从头开始获取消息 可选项

## SpringBoot

### pom

```xml
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka</artifactId>
        </dependency>
```

### application.properties

kafka的配置类`org.springframework.boot.autoconfigure.kafka.KafkaProperties`

```properties
# kafka节点的访问地址，集群的话可以配置多个，用 逗号 分割
spring.kafka.bootstrap-servers=192.168.180.176:9092
# consumer的key、value的反序列话类
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer
# offset 自动提交 commitAsync
spring.kafka.consumer.enable-auto-commit=true
```

### Listener

`@KafkaListener`

* 一次接收一条消息

  ```java
      /**
       * topics 监听的topic列表,
       * groupId 定义消费组id
       *
       */
      @KafkaListener(topics = {"testaaa"}, clientIdPrefix = "test", groupId = "test_log_group")
      public void receiver(ConsumerRecord<String, String> record) {
          LOGGER.info("record： {}, {}", record.value(), record.offset());
      }
  ```

* 一次接收多条消息

  ```java
  	@Bean
      public ConcurrentKafkaListenerContainerFactory<String, String> batchFactory(ConsumerFactory<String, String> consumerFactory) {
          ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
          // 设置batchListener为true
          factory.setBatchListener(true);
          // 设置并发度，比如设置为2，就会有两个线程去监听topic
          factory.setConcurrency(2);
          factory.setConsumerFactory(consumerFactory);
          return factory;
      }
  
   	/**
       * topics 监听的topic列表,
       * groupId 定义消费组id
       * containerFactory listener自定义配置工厂
       *
       */
      @KafkaListener(topics = {"testaaa"}, clientIdPrefix = "test", groupId = "test_log_group", containerFactory = "batchFactory")
      public void receiver(ConsumerRecords<String, String> record) {
          List<String> messages = record.partitions().stream()
                  .flatMap(x -> record.records(x).stream())
                  .map(ConsumerRecord::value)
                  .collect(Collectors.toList());
          LOGGER.info("record： {}, {}", messages.size(), record);
      }
  
  ```

  



## Q&A

### 重复消费

* 消费者有两种提交策略，自动提交和手动提交。如果消费者处理数据过程简单，耗时较小，那么这两种策略都没问题，都能进行提交。但消费者处理数据过程复杂，耗时较大，使用自动提交时，取出的数据在session.timeout.ms时间内没有处理完成，自动提交offset失败，然后kafka会重新分配partition给消费者，消费者又重新消费之前的一批数据，又出现了消费超时，所以会造成死循环，一直消费相同的数据

### kafka配置zookeeper

kafka在启动时，需要配置zookeeper的访问地址，需要确保zookeeper上是否还配置有其他节点的kafka，否则会出现消息接收不到的情况

创建topic的时候，指定的zookeeper地址，也需要同样注意这个问题

