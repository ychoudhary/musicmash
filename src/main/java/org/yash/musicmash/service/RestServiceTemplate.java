package org.yash.musicmash.service;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.yash.musicmash.config.AppConfiguration;

@Component
public class RestServiceTemplate {

  private AppConfiguration config;
  private RestTemplateBuilder restTemplateBuilder;

  @Autowired
  public RestServiceTemplate(AppConfiguration config, RestTemplateBuilder restTemplateBuilder) {
    this.config = config;
    this.restTemplateBuilder = restTemplateBuilder;
  }

  @Bean
  public RestTemplate restTemplate() {
    return restTemplateBuilder.requestFactory(this::httpComponentsClientHttpRequestFactory).build();
  }

  @Bean
  public HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory() {
    PoolingHttpClientConnectionManager poolingConnectionManager =
        new PoolingHttpClientConnectionManager();
    poolingConnectionManager.setMaxTotal(config.getSource().getConnectionPoolMaxTotal());
    poolingConnectionManager.setDefaultMaxPerRoute(
        config.getSource().getConnectionPoolMaxPerRoute());
    CloseableHttpClient client =
        HttpClientBuilder.create()
            .setConnectionManager(poolingConnectionManager)
            .disableCookieManagement()
            .build();

    HttpComponentsClientHttpRequestFactory clientHttpRequestFactory =
        new HttpComponentsClientHttpRequestFactory(client);
    clientHttpRequestFactory.setConnectTimeout(config.getSource().getConnectTimeOutMs());
    clientHttpRequestFactory.setReadTimeout(config.getSource().getReadTimeoutMS());
    clientHttpRequestFactory.setConnectionRequestTimeout(
        config.getSource().getConnectionRequestTimeoutMS());

    return clientHttpRequestFactory;
  }

}
