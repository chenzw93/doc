package com.haier.log.helper;

import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class RestHelper {
    private HttpClientBuilder httpsBuilder;

    public CloseableHttpClient httpsClient() {
        return httpsBuilder.build();
    }

    @PostConstruct
    private void init() {
        this.httpsBuilder = HttpClients.custom().setConnectionManager(new PoolingHttpClientConnectionManager(RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", SSLConnectionSocketFactory.getSocketFactory())
                .build()));
    }
}
