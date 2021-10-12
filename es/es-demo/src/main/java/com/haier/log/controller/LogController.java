package com.haier.log.controller;

import com.alibaba.fastjson.JSONObject;
import com.haier.log.document.OperationLogDocument;
import com.haier.log.scheduler.IndexScheduler;
import com.haier.log.service.EsTemplate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/log")
public class LogController {
    @Resource
    private EsTemplate<OperationLogDocument> template;

    @Resource
    private IndexScheduler indexScheduler;

    @PostMapping("/query-nor")
    public Map<String, Object> queryNormal(@RequestBody JSONObject param) {
        List<Map> result = template.query(param);
        return new JSONObject().fluentPut("retCode", 20000).fluentPut("retInfo", "success").fluentPut("data", result);
    }

    @PostMapping("/query")
    public Map<String, Object> query(@RequestBody JSONObject param) {
        if (StringUtils.isAnyBlank(param.getString("start_time"), param.getString("end_time"))) {
            return new JSONObject().fluentPut("retCode", 40000).fluentPut("retInfo", "param illegal").fluentPut("data", StringUtils.EMPTY);
        }
        List<Map> result = template.queryByConditional(param);
        return new JSONObject().fluentPut("retCode", 20000).fluentPut("retInfo", "success").fluentPut("data", result);
    }

    @PostMapping("/update/cron")
    public Map<String, Object> updateCron(@RequestBody JSONObject param) {
        if (StringUtils.isBlank(param.getString("cron"))) {
            return new JSONObject().fluentPut("retCode", 20000).fluentPut("retInfo", "success").fluentPut("data", StringUtils.EMPTY);
        }
        indexScheduler.setCron(param.getString("cron"));
        return new JSONObject().fluentPut("retCode", 20000).fluentPut("retInfo", "success").fluentPut("data", StringUtils.EMPTY);
    }
}
