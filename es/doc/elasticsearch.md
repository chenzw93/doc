[TOC]

# [Elasticsearch ](https://www.elastic.co/cn)

> 基于 es 6.2版本

## 介绍

基于[Lucene](https://lucene.apache.org/)构建的开源、分布式，RESTful搜索引擎; store data

* 是一个开源、高扩展、分布式全文检索引擎，它可以**近乎实时的存储**、检索数据；本身扩展性很好，可以扩展到上百台服务器，处理PB级别的数据。

* Elasticsearch使用Java开发，基于`Lucene`实现索引和搜索的功能，但是它的目的是通过简单的RESTful API来隐藏Lucene的复杂性，从而让全文搜索变得简单。

## 优势

* 分布式实时文件存储，可将每一个字段存入索引，使其可以被检索到;

* 近乎实时分析的分布式搜索引擎;

* 分布式：索引分拆成多个分片，每个分片可有零个或多个副本。集群中的每个数据节点都可承载一个或多个分片，并且协调和处理各种操作;

* 负载再平衡和路由在大多数情况下自动完成;

* 可以扩展到上百台服务器，处理PB级别的结构化或非结构化数据。也可以运行在单台PC上;

* 支持插件机制，分词插件、同步插件、Hadoop插件、可视化插件等;

## 官方文档

https://www.elastic.co/guide/en/elasticsearch/reference/6.2/index.html

## 主要概念解释

### index

类比于关系型数据库中的 **数据库**；

es进行分片存储的的时候，也是基于index进行分片，每个分片可以有多个副本(防止数据丢失)

### type

类比于关系型数据库中的 **表**

6.x版本之前，一个index可以有多个type，其实更加像关系型数据库中表的概念; 但是6.x之后，取消了一个index对应多个type的情况，所以type的概念进行了弱化，默认的type值为 `_doc`

### document

es中的每条数据称为一个document，也就是一条记录，每条数据的样式是json数据类型

![](image\es-document.png)

> _index: 当前文档属于哪个index
>
> _type: 当前文档的类型
>
> _id: 当前文档的数据id，如果不指定，则会默认生成，实例就是默认生成的 _id
>
> _score: 文档的评分，跟数据的匹配度等等有关
>
> _source: 具体存储的数据,以json格式存储

### mapping

定义文档中元数据的字段类型

![](image\es-mapping.png)

> nginx-access-2021-09-08: index名称
>
> 上图所示的，字段 `host` 的字段类型为`text`， 字段`@timestamp`的字段类型为 `date`

mapping内容有两种创建的型式：

* 创建index时指定mapping，然后自定义mapping的内容，包括字段类型，分词器等等

* 如果不自己设置，插入数据时，es会根据插入的数据类型，自动匹配创建对应的字段类型


### 常用数据类型

#### 字符串

`text`：该类型的字段数据，存储时会先进行分词;

`keyword`: 与text恰好相反,如果存储的字段字段数据，不想进行分词，则可以将type设置为keyword类型

#### 数字类型

`long`, `integer`, `short`, `byte`, `double`, `float`, `half_float`, `scaled_float`

#### 日期

`date`

#### 对象

`object`

### analyzer

分词：顾名思义就是将数据分成不同的词语；

分为两种情况：

* 创建或者更新文档时，会先对文档进行分词，然后对分词建立索引，进行存储
* 查询时，对查询条件进行分词，再进行匹配

#### 分词器

es默认内置了多种分词器，当前目前也有较多的三方分词器，都可以使用

其中 `standard分词器` 对英文分词较好，通过空格，标点符号进行分词，同时会将文档全部小写进行存储；但是对中文分词就很不友好，如果使用`standard分词器`进行中文的文档分词，则时一个字一个字的分词

三方有比较好的中文分词，[ik](https://github.com/medcl/elasticsearch-analysis-ik)

#### 分词器指定

##### 创建/更新

创建index时，可以通过 `mappings` 来指定对应字段的 `analyzer`

```shell
PUT /index-test
{
  "mappings": {
    "_doc":{
      "properties": {
      "content": {
          "type": "text",
          "analyzer": "ik_max_word",
          "search_analyzer": "ik_smart"
        }
      }
    }
  }
}
```

##### 查询

查询时指定字段的分词器

```shell
GET index-test/_search
{
  "query": {
    "match": {
      "content": {
        "query": "Quick foxes",
        "analyzer": "standard"
      }
    }
  }
}
```

## 部署

### [download](https://www.elastic.co/cn/downloads/elasticsearch)

### configuration

配置文件主要在config目录下

![es-config](image\es-config.png)

配置文件列表主要如上图所示

##### elasticsearch.yml

主要的配置点有：

* `http.host` `network.host` http及tcp绑定的ip
* `discovery.zen.ping.unicast.hosts` 哪些节点可以访问到es服务

其他配置点可以参考`elasticsearch.yml`中的注释解释

##### jvm.options

es也是java编写的服务，此文件中可以配置es服务启动JVM的一些参数，主要可以参考文件中的注释信息

##### role*/users*

es也可以进行创建用户、角色，对功能进行角色控制

##### log4j2.porperties

es服务的日志配置文件

### run

linux 版本的服务启动直接用如下命令，启动完成后，可以通过 配置的 `http.host` + `http.port` 访问es服务

```shell
bin/elasticsearch
```

![es-start](image\es-start.png)

### plugin

es有很多插件，可以使用在线安装、离线安装，[安装文档](https://www.elastic.co/guide/en/elasticsearch/plugins/6.2/installation.html)

#### 离线

```shell
sudo bin/elasticsearch-plugin install file:///path/to/plugin.zip
```

其中 `file://` 是必须要有的，后面跟离线插件包的全路径 

## API

以下`CRUD`操作都是通过 curl 等方式操作

### [Index API](https://www.elastic.co/guide/en/elasticsearch/reference/6.2/docs-index_.html)

![es-index](image\es-index.png)

```shell
POST /index-test/_doc/
{"content":"美国留给伊拉克的是个烂摊子吗"}
```

> request
>
> * `index-test`: 已经创建的index名称
>
> * `_doc`: type,默认为 `_doc`
>
> * 参数为json格式即可
>
> response
>
> * 如果不指定id，则会默认生成一个id，见响应结果 `"_id" : "gfze3XsBWbHXLbipFnPC"`
> * result: 代表请求做了什么操作；create、update
> * _shards: 分区



### [Get API](https://www.elastic.co/guide/en/elasticsearch/reference/6.2/docs-get.html)

![es-get](image\es-get.png)

```shell
GET /index-test/_doc/gfze3XsBWbHXLbipFnPC
```

> request
>
> * `index-test`: 已经创建的index名称
>
> * `_doc`: type,默认为 `_doc`
>
> * gfze3XsBWbHXLbipFnPC: 文档的id
>
> response
>
> * _source: document详情

Get请求是**RealTime**

### [Delete API](https://www.elastic.co/guide/en/elasticsearch/reference/6.2/docs-delete.html)

```shell

DELETE /index-test/_doc/gfze3XsBWbHXLbipFnPC

	
DELETE /test
```

第一个是根据id进行文档删除

第二个是删除对应index

### [Update API](https://www.elastic.co/guide/en/elasticsearch/reference/6.2/docs-update.html)

```shell
PUT /index-test/_doc/1
{"content":"美国留给伊拉克的是个烂摊子吗"}
```

> 会根据id进行update

## Search

有多种方式，比如 **uri search** 、**request body search**

#### match

##### match

相当于like；

插入es的数据，`text`类型的数据，都会进行分词存储，所以使用`match`查询的时候，查询条件也会进行分词查询，所以匹配的时候会把所有跟分词后的字段匹配的所有结果查询出来

> eg: 我想要查询的条件是  `/managecenter-api/login` ,如果使用`matchQuery`,查询到的结果中可能会出现`/developercenter-api/login`这样的结果，就是因为条件也会进行分词

##### match_phrase

可以解决match产生的问题，分词之后需要完全匹配

##### multi-match

多字段匹配

#### term

如果使用term查询可能会出现，es的数据中包含了查询的字段，但是查询不出来的情况

> es中包含数据 `/managecenter-api/login` ,但如果使用 `term` 查询，条件为  `/managecenter-api/login` ，是匹配不到结果的，因为如果使用`term`查询，查询条件不会被分词，但是数据存储的时候，是进行分词的，所以就会匹配不到
>
> 解决办法：
>
> * 对应字段存储时，不要使用`text`类型，使用`keyword` 类型,这样存储时不会进行分词；
> * 查询时，给对应的字段加上 `.keyword` ；

## [Aggregations](https://www.elastic.co/guide/en/elasticsearch/reference/6.2/search-aggregations.html)

ES中的分类汇总等等都是通过这个模块来实现的，[文档地址](https://www.elastic.co/guide/en/elasticsearch/reference/6.2/search-aggregations.html)

## springboot

### pom.xml

本次集成是基于es 6.2version + springboot 2.2.1version

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.2.1.RELEASE</version>
    <relativePath/> <!-- lookup parent from repository -->
</parent>

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
        <exclusions>
            <exclusion>
                <groupId>org.elasticsearch.client</groupId>
                <artifactId>transport</artifactId>
            </exclusion>
            <exclusion>
                <groupId>org.elasticsearch.plugin</groupId>
                <artifactId>transport-netty4-client</artifactId>
            </exclusion>
            <exclusion>
                <groupId>org.elasticsearch.client</groupId>
                <artifactId>elasticsearch-rest-high-level-client</artifactId>
            </exclusion>
        </exclusions>
    </dependency>

    <dependency>
        <groupId>org.elasticsearch.client</groupId>
        <artifactId>transport</artifactId>
        <version>6.6.2</version>
    </dependency>
    <dependency>
        <groupId>org.elasticsearch.plugin</groupId>
        <artifactId>transport-netty4-client</artifactId>
        <version>6.6.2</version>
    </dependency>
    <dependency>
        <groupId>org.elasticsearch.client</groupId>
        <artifactId>elasticsearch-rest-high-level-client</artifactId>
        <version>6.6.2</version>
    </dependency>
</dependencies>
```

### configuartion

引入上述三方依赖包，然后需要配置es服务的访问uri、username、password、connectTimeout、readTimeout等属性，如果不配置，默认的uri是 `http://localhost:9200`

这里配置的是http协议的访问配置

在`application.properties`中配置如下内容

```properties
# 需要访问的es服务地址
spring.elasticsearch.rest.uris=http://192.168.180.176:9200
# 建立链接超时时间, 默认是1秒
spring.elasticsearch.rest.connection-timeout=60
# 请求超时时间，默认是30秒
spring.elasticsearch.rest.read-timeout=60
# 如果es配置使用密码访问，需要配置如下两个
spring.elasticsearch.rest.username=aaa
spring.elasticsearch.rest.password=xxxx
```

### use

以上两步做好之后，就可以开始使用了;在需要使用的地方将 `org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate` 注入进来，就可以使用响应的api进行`CRUD`等相关操作了

```java
@Resource
private ElasticsearchRestTemplate template;
```



## Q&A

### java.net.SocketTimeoutException: 30,000 milliseconds timeout on connection http-outgoing-0 [ACTIVE]

> 初始化RestClient时，可以配置connection-timeout、read-timeout,默认为30s

### [type=illegal_argument_exception, reason=Fielddata is disabled on text fields by default. Set fielddata=true on

> [参考](https://kalasearch.cn/community/tutorials/elasticsearch-fielddata-is-disabled-on-text-fields-error/)
>
> 如果想要根据字段排序，设置字段后，会出现上述报错，其中解决办法：
>
> * 字段后面带上 `.keyword` 即可解决
> * `fielddata` 设为 true
>
> 多数情况下，出现 Fielddata 相关的错误都是因为尝试聚合和排序一个 text 字段，因此只要把字段的 `fielddata` 设为 true 或者类型调整为 keyword 即可。如果是已存在字段可能需要重新索引。

### 默认存在区别大小写

存储到ES的字段,如果包含大写，搜索的时候如果用term，则必须使用小写，es会将大写转为消息存储

### 安装es后，设置绑定ip，启动服务报错

![error](image\es-error1.png)

如果启动es服务日志报错信息如上图，那就需要找运维修改部署机器的内核参数

`/etc/sysctl.conf` 添加如下一行数据

```shell
vm.max_map_count = 262144
```

需要重新价值配置的数据，完成之后，再重启服务即可

