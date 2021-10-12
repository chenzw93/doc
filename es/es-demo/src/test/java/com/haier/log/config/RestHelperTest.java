package com.haier.log.config;

import com.haier.log.helper.RestHelper;
import org.apache.http.Consts;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.BeforeEach;

import javax.annotation.Resource;
import java.io.IOException;

//@SpringBootTest
//@RunWith(SpringJUnit4ClassRunner.class)
class RestHelperTest {
    @Resource
    private RestHelper restHelper;

    @BeforeEach
    void setUp() {
    }

    /**
     * 制定项目下接口集合
     * https://stp.haier.net/api/interface/list_menu?project_id=275&token=8d8237198f5edbd7fa831c806e3bcfff2dd298843d9d16800dba3277c28ec7e4
     * <p>
     * https://stp.haier.net/api/interface/list_menu?project_id=275&token&8d8237198f5edbd7fa831c806e3bcfff2dd298843d9d16800dba3277c28ec7e4
     *
     * @throws IOException
     */
//    @Test
    void httpsClient() throws IOException {
        HttpGet get = new HttpGet("https://stp.haier.net/api/interface/list_menu?project_id=275&token=8d8237198f5edbd7fa831c806e3bcfff2dd298843d9d16800dba3277c28ec7e4");
        CloseableHttpResponse execute = restHelper.httpsClient().execute(get);

        EntityUtils.toString(execute.getEntity(), Consts.UTF_8);
    }

//    @Test
    void test_tencent_success() throws IOException {
        HttpGet get = new HttpGet("https://apis.map.qq.com/ws/location/v1/ip?ip=10.180.108.19&key=OB4BZ-D4W3U-B7VVO-4PJWW-6TKDJ-WPB77");
        CloseableHttpResponse execute = restHelper.httpsClient().execute(get);

        EntityUtils.toString(execute.getEntity(), Consts.UTF_8);
    }
}