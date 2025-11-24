package com.inditex.similar_products.adapter.out.http.restclient.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServiceUnavailable;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.inditex.similar_products.domain.exception.ProductNotFoundException;
import com.inditex.similar_products.domain.exception.SimilarProductsRetrievalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

@RestClientTest(value = ProductRestClient.class,
    properties = {
        "product-api.base-url=http://mock-product-api",
        "product-api.retry.max-attempts=3",
        "product-api.retry.delay=100",
        "product-api.retry.multiplier=1"
    })
class ProductRestClientTest {

  private static final String FIXED_PRODUCT_ID_1 = "1";
  private static final String FIXED_PRODUCT_ID_2 = "1";
  private static final String FIXED_BASED_URL = "http://mock-product-api";
  private static final String FIXED_SIMILAR_PRODUCT_IDS_URL = "/product/{productId}/similarids";
  private static final String FIXED_PRODUCT_DETAILS_URL = "/product/{productId}";
  private static final String FIXED_SIMILAR_PRODUCTS_IDS_RAW_RESPONSE = """
      [2,3,4]
      """;
  private static final String FIXED_GET_PRODUCT_RAW_RESPONSE_2 = """
      {"id":"2","name":"Dress","price":19.99,"availability":true}
      """;

  @Autowired
  private MockRestServiceServer productApiMock;

  @Autowired
  private ProductRestClient productRestClient;

  @TestConfiguration
  @EnableRetry
  static class TestConfig {

    @Bean
    RestClient productApiRestClient(RestClient.Builder builder) {
      return builder.baseUrl(FIXED_BASED_URL).build();
    }
  }

  @BeforeEach
  void setup() {
    ReflectionTestUtils.setField(
        productRestClient, "getSimilarProductIdsUrl", FIXED_SIMILAR_PRODUCT_IDS_URL
    );
    ReflectionTestUtils.setField(
        productRestClient, "getProductDetailsUrl", FIXED_PRODUCT_DETAILS_URL
    );
  }

  @Test
  void getSimilarProductIdsShouldReturnListOfIdsWhenApiReturnsAsExpected() {
    var url = FIXED_BASED_URL + FIXED_SIMILAR_PRODUCT_IDS_URL.replace("{productId}",
        FIXED_PRODUCT_ID_1);
    productApiMock.expect(once(), requestTo(url))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withSuccess(FIXED_SIMILAR_PRODUCTS_IDS_RAW_RESPONSE, MediaType.APPLICATION_JSON));

    var result = productRestClient.getSimilarProductIds(FIXED_PRODUCT_ID_1);

    assertEquals(3, result.size());
    assertEquals("2", result.get(0));
    assertEquals("3", result.get(1));
    assertEquals("4", result.get(2));
    productApiMock.verify();
  }

  @Test
  void getSimilarProductIdsShouldReturnEmptyListWhenApiReturnsEmptyList() {
    var url = FIXED_BASED_URL + FIXED_SIMILAR_PRODUCT_IDS_URL.replace("{productId}",
        FIXED_PRODUCT_ID_1);
    productApiMock.expect(once(), requestTo(url))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

    var result = productRestClient.getSimilarProductIds(FIXED_PRODUCT_ID_1);

    assertTrue(result.isEmpty());
    productApiMock.verify();
  }

  @Test
  void getSimilarProductIdsShouldReturnEmptyListWhenApiReturnsEmptyBody() {
    var url = FIXED_BASED_URL + FIXED_SIMILAR_PRODUCT_IDS_URL.replace("{productId}",
        FIXED_PRODUCT_ID_1);
    productApiMock.expect(once(), requestTo(url))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

    var result = productRestClient.getSimilarProductIds(FIXED_PRODUCT_ID_1);

    assertTrue(result.isEmpty());
    productApiMock.verify();
  }

  @Test
  void getSimilarProductIdsShouldThrowProductNotFoundExceptionAndNoRetryWhenApiReturns404() {
    var url = FIXED_BASED_URL + FIXED_SIMILAR_PRODUCT_IDS_URL.replace("{productId}",
        FIXED_PRODUCT_ID_1);
    productApiMock.expect(once(), requestTo(url))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withResourceNotFound());

    assertThrows(ProductNotFoundException.class,
        () -> productRestClient.getSimilarProductIds(FIXED_PRODUCT_ID_1));
    productApiMock.verify();
  }

  @Test
  void getSimilarProductIdsShouldThrowSimilarProductsRetrievalExceptionAndRetry3TimesWhenApiReturns500() {
    var url = FIXED_BASED_URL + FIXED_SIMILAR_PRODUCT_IDS_URL.replace("{productId}",
        FIXED_PRODUCT_ID_1);
    productApiMock.expect(ExpectedCount.times(3), requestTo(url))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withServerError());

    assertThrows(SimilarProductsRetrievalException.class,
        () -> productRestClient.getSimilarProductIds(FIXED_PRODUCT_ID_1));
    productApiMock.verify();
  }

  @Test
  void getSimilarProductIdsShouldThrowSimilarProductsRetrievalExceptionAndRetry3TimesWhenApiReturns503() {
    var url = FIXED_BASED_URL + FIXED_SIMILAR_PRODUCT_IDS_URL.replace("{productId}",
        FIXED_PRODUCT_ID_1);
    productApiMock.expect(ExpectedCount.times(3), requestTo(url))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withServiceUnavailable());

    assertThrows(SimilarProductsRetrievalException.class,
        () -> productRestClient.getSimilarProductIds(FIXED_PRODUCT_ID_1));
    productApiMock.verify();
  }

  @Test
  void getProductByIdShouldReturnProductWhenApiReturnsAsExpected() {
    var url = FIXED_BASED_URL + FIXED_PRODUCT_DETAILS_URL.replace("{productId}",
        FIXED_PRODUCT_ID_2);
    productApiMock.expect(once(), requestTo(url))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(FIXED_GET_PRODUCT_RAW_RESPONSE_2, MediaType.APPLICATION_JSON));

    var result = productRestClient.getProductById(FIXED_PRODUCT_ID_2);

    assertEquals("2", result.id());
    assertEquals("Dress", result.name());
    assertEquals(19.99, result.price().doubleValue());
    assertEquals(true, result.availability());
    productApiMock.verify();
  }

  @Test
  void getProductByIdShouldThrowSimilarProductsRetrievalExceptionAndRetry3TimesWhenApiReturnsEmptyBody() {
    var url = FIXED_BASED_URL + FIXED_PRODUCT_DETAILS_URL.replace("{productId}",
        FIXED_PRODUCT_ID_2);
    productApiMock.expect(ExpectedCount.times(3), requestTo(url))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

    assertThrows(SimilarProductsRetrievalException.class,
        () -> productRestClient.getProductById(FIXED_PRODUCT_ID_2));
    productApiMock.verify();
  }

  @Test
  void getProductByIdShouldThrowSimilarProductsRetrievalExceptionAndNoRetryWhenApiReturns404() {
    var url = FIXED_BASED_URL + FIXED_PRODUCT_DETAILS_URL.replace("{productId}",
        FIXED_PRODUCT_ID_2);
    productApiMock.expect(once(), requestTo(url))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withResourceNotFound());

    assertThrows(SimilarProductsRetrievalException.class,
        () -> productRestClient.getProductById(FIXED_PRODUCT_ID_2));
    productApiMock.verify();
  }

  @Test
  void getProductByIdShouldThrowSimilarProductsRetrievalExceptionAndRetry3TimesWhenApiReturns500() {
    var url = FIXED_BASED_URL + FIXED_PRODUCT_DETAILS_URL.replace("{productId}",
        FIXED_PRODUCT_ID_2);
    productApiMock.expect(ExpectedCount.times(3), requestTo(url))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withServerError());

    assertThrows(SimilarProductsRetrievalException.class,
        () -> productRestClient.getProductById(FIXED_PRODUCT_ID_2));
    productApiMock.verify();
  }

  @Test
  void getProductByIdShouldThrowSimilarProductsRetrievalExceptionAndRetry3TimesWhenApiReturns503() {
    var url = FIXED_BASED_URL + FIXED_PRODUCT_DETAILS_URL.replace("{productId}",
        FIXED_PRODUCT_ID_2);
    productApiMock.expect(ExpectedCount.times(3), requestTo(url))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withServiceUnavailable());

    assertThrows(SimilarProductsRetrievalException.class,
        () -> productRestClient.getProductById(FIXED_PRODUCT_ID_2));
    productApiMock.verify();
  }
}