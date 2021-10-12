package com.haier.log.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.haier.log.document.OperationLogDocument;
import com.haier.log.service.EsBasicTemplate;
import com.haier.log.service.EsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/v1")
public class Controller {
    private static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);
    @Resource
    private EsBasicTemplate esBasicTemplate;
    @Resource
    private EsTemplate<OperationLogDocument> template;

    @GetMapping("/insert")
    public Map<String, Object> insertByFile(@RequestBody JSONObject param) {
        String filePath = param.getString("file_path");
        esBasicTemplate.insertByLogFile(filePath);
        return new JSONObject().fluentPut("retCode", 20000).fluentPut("retInfo", "success").fluentPut("message", "");
    }

    /**
     * If the id does not exist, insert, otherwise update
     *
     * @param id
     * @param time
     * @param level
     * @param value
     * @return
     */
    @GetMapping("/insert/{id}/{time}/{level}/{value}")
    public Map<String, Object> insertOne(@PathVariable String id, @PathVariable String time, @PathVariable String level, @PathVariable String value) {
        Map<String, Object> document = new JSONObject()
                .fluentPut("id", id)
                .fluentPut("time", time)
                .fluentPut("level", level)
                .fluentPut("value", value);
        esBasicTemplate.insertOne(JSON.toJSONString(document), id);
        return new JSONObject().fluentPut("retCode", 20000).fluentPut("retInfo", "success").fluentPut("message", "");
    }

    /**
     * delete by document id
     *
     * @param id id
     * @return result
     */
    @GetMapping("/delete/{id}")
    public Map<String, Object> deleteById(@PathVariable String id) {
        esBasicTemplate.deleteById(id);
        return new JSONObject().fluentPut("retCode", 20000).fluentPut("retInfo", "success").fluentPut("message", "");
    }

    /**
     * delete by time
     *
     * @param time time
     * @return result
     */
    @GetMapping("/delete/time/{time}")
    public Map<String, Object> deleteByCondition(@PathVariable String time) {
        esBasicTemplate.deleteByConditaion(time);
        return new JSONObject().fluentPut("retCode", 20000).fluentPut("retInfo", "success").fluentPut("message", "");
    }

    /**
     * 一共调用了多少次？
     * 成功了多少次？失败了多少次？
     * 失败了的具体原因是什么？
     *
     * @param param
     * @return
     */

    @PostMapping("/statistics/indicator")
    public Map<String, Object> statistics(@RequestBody JSONObject param) {
        LOGGER.info("param: {}", param);
        Map<String, Object> result = template.statistics(param);
        return new JSONObject().fluentPut("data", result).fluentPut("retCode", 20000).fluentPut("retInfo", "success");
    }

    @GetMapping("/test/error")
    public Map<String, Object> testError() {
        try {
            int i = 1 / 0;
        } catch (Exception exception) {
            LOGGER.error("error msg: ", exception);
        }
        return new JSONObject();
    }

    @GetMapping("/test/ok")
    public Map<String, Object> testOk() {
        try {
            String test = "aaa";
            String test_a = "bbbb";
            String test_b = "cc";

            LOGGER.info("it is normal message: {}, {}, {}", test, test_a, test_b);
        } catch (Exception exception) {
            LOGGER.error("error msg: ", exception);
        }
        return new JSONObject();
    }
}
