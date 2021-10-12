package com.haier.log.helper;

import com.alibaba.fastjson.JSONObject;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.Location;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;


@Component
public class GeoHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(GeoHelper.class);
    private DatabaseReader reader;

    /**
     * 获取客户端IP地址
     * <p>
     * 使用Nginx等反向代理软件， 则不能通过request.getRemoteAddr()获取IP地址
     * 如果使用了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP地址，X-Forwarded-For中第一个非unknown的有效IP字符串，则为真实IP地址
     */
    public static String getIP(HttpServletRequest request) {
        String ip = null;
        try {
            ip = request.getHeader("x-forwarded-for");
            if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (StringUtils.isEmpty(ip) || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
            }
            if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
        } catch (Exception e) {
            LOGGER.error("IPUtils ERROR ", e);
        }
        return ip;
    }

    public static void main(String[] args) throws Exception {
        // String path = req.getSession().getServletContext().getRealPath("/WEB-INF/classes/GeoLite2-City.mmdb");
        String path = "D:\\haier\\work\\damand\\elastic\\GeoLite2-City.mmdb";
        // 创建 GeoLite2 数据库
        File database = new File(path);
        // 读取数据库内容
        DatabaseReader reader = new DatabaseReader.Builder(database).build();
        // 访问IP
//        String ip = "110.184.229.98";
        // 中国-四川省-成都 纬度:104.0667 经度:30.6667
//        String ip = "39.107.247.209";
        // 中国-null-null 纬度:113.722 经度:34.7732

        String ip = "123.57.31.57";
        // 中国-null-null 纬度:113.722 经度:34.7732
        ip = "111.30.185.195";
//        ip = "123.103.113.201";
        ip = "10.180.108.19";
        CityResponse city = reader.city(InetAddress.getByName(ip));
//        String site = IPHelper.getCountry(reader, ip) + "-" + IPHelper.getProvince(reader, ip) + "-" + IPHelper.getCity(reader, ip) + " 纬度:" + IPHelper.getLongitude(reader, ip) + " 经度:" + IPHelper.getLatitude(reader, ip);
        System.out.println(city);
    }

    public void location(JSONObject param) {
        param.put("country", StringUtils.EMPTY);
        param.put("province", StringUtils.EMPTY);
        param.put("city", StringUtils.EMPTY);
        param.put("latitude", StringUtils.EMPTY);
        param.put("longitude", StringUtils.EMPTY);

        try {
            String clientIp = param.getString("clientIp");
            CityResponse city = reader.city(InetAddress.getByName(clientIp));
            Location location = city.getLocation();
            param.put("latitude", location.getLatitude());
            param.put("longitude", location.getLongitude());
            param.put("country", city.getCountry());
            param.put("province", city.getMostSpecificSubdivision().getNames().get("zh-CN"));
            param.put("city", city.getCity());
        } catch (IOException | GeoIp2Exception ignored) {

        }
    }

    @PostConstruct
    private void init() {
        ClassPathResource resource = new ClassPathResource("GeoLite2-City.mmdb");
        try (InputStream inputStream = resource.getInputStream()) {
            this.reader = new DatabaseReader.Builder(inputStream).build();
        } catch (Exception exception) {
            LOGGER.error("exception: {}", exception);
        }

    }
}
