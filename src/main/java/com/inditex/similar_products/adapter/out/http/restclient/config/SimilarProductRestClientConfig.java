package com.inditex.similar_products.adapter.out.http.restclient.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class SimilarProductRestClientConfig {

  @Value("${product-api.base-url}")
  private String baseUrl;
  @Value("${product-api.timeouts.read}")
  private int readTimeout;
  @Value("${product-api.timeouts.connect}")
  private int connectTimeout;

  @Bean
  public RestClient productApiRestClient(RestClient.Builder builder) {
    var requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(connectTimeout);
    requestFactory.setReadTimeout(readTimeout);
    return builder
        .baseUrl(baseUrl)
        .requestFactory(requestFactory)
        .build();
  }

  @Bean(destroyMethod = "close")
  public ExecutorService virtualThreadExecutor() {
    return Executors.newVirtualThreadPerTaskExecutor();
  }
}
