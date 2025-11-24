package com.inditex.similar_products.adapter.out.http.restclient.client;

import com.inditex.similar_products.adapter.out.http.restclient.dto.ProductRestClientDto;
import com.inditex.similar_products.domain.exception.ProductNotFoundException;
import com.inditex.similar_products.domain.exception.SimilarProductsRetrievalException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductRestClient {

  @Value("${product-api.similar-product-ids-url}")
  private String getSimilarProductIdsUrl;

  @Value("${product-api.product-details-url}")
  private String getProductDetailsUrl;

  private final RestClient productApiRestClient;

  @Retryable(
      retryFor = {RestClientException.class},
      noRetryFor = {ProductNotFoundException.class},
      maxAttemptsExpression = "${product-api.retry.max-attempts}",
      backoff = @Backoff(
          delayExpression = "${product-api.retry.delay}",
          multiplierExpression = "${product-api.retry.multiplier}"
      )
  )
  public List<String> getSimilarProductIds(String productId) {
    var ids = productApiRestClient.get()
        .uri(getSimilarProductIdsUrl, productId)
        .retrieve()
        .onStatus(status -> status.isSameCodeAs(HttpStatus.NOT_FOUND),
            ((request, response) -> {
              throw new ProductNotFoundException(
                  "Product " + productId + " was not found");
            }))
        .body(new ParameterizedTypeReference<List<String>>() {
        });
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    return ids;
  }

  @Recover
  public List<String> recoverGetSimilarProductIds(ProductNotFoundException e, String productId) {
    log.error("Product {} was not found", productId, e);
    throw e;
  }

  @Recover
  public List<String> recoverGetSimilarProductIds(Exception e, String productId) {
    log.error("Something went wrong retrieving similar product ids for product {}", productId, e);
    throw new SimilarProductsRetrievalException(
        "Something went wrong retrieving similar product ids for product: " + productId);
  }

  @Retryable(
      retryFor = {RestClientException.class, SimilarProductsRetrievalException.class},
      noRetryFor = {ProductNotFoundException.class},
      maxAttemptsExpression = "${product-api.retry.max-attempts}",
      backoff = @Backoff(
          delayExpression = "${product-api.retry.delay}",
          multiplierExpression = "${product-api.retry.multiplier}"
      )
  )
  public ProductRestClientDto getProductById(String productId) {
    var productDto = productApiRestClient.get()
        .uri(getProductDetailsUrl, productId)
        .retrieve()
        .onStatus(status -> status.isSameCodeAs(HttpStatus.NOT_FOUND),
            ((request, response) -> {
              throw new ProductNotFoundException(
                  "Product " + productId + " was not found");
            }))
        .body(ProductRestClientDto.class);
    if (productDto == null) {
      throw new SimilarProductsRetrievalException("Empty product retrieved for id " + productId);
    }
    return productDto;
  }

  @Recover
  public ProductRestClientDto recoverGetProductById(Exception e, String productId) {
    log.error("Something went wrong retrieving product with id: {}", productId, e);
    throw new SimilarProductsRetrievalException(
        "Something went wrong retrieving product with id: " + productId);
  }
}
