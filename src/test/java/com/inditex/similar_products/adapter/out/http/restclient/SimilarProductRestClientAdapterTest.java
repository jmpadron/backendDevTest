package com.inditex.similar_products.adapter.out.http.restclient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.inditex.similar_products.adapter.out.http.restclient.client.ProductRestClient;
import com.inditex.similar_products.adapter.out.http.restclient.dto.ProductRestClientDto;
import com.inditex.similar_products.domain.exception.ProductNotFoundException;
import com.inditex.similar_products.domain.exception.SimilarProductsRetrievalException;
import com.inditex.similar_products.domain.model.ProductId;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SimilarProductRestClientAdapterTest {

  @Mock
  private ProductRestClient productRestClient;

  private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

  private SimilarProductRestClientAdapter similarProductRestClientAdapter;

  private static final ProductId FIXED_PRODUCT_ID_1 = new ProductId("1");
  private static final ProductId FIXED_PRODUCT_ID_2 = new ProductId("2");
  private static final ProductRestClientDto FIXED_PRODUCT_DTO_2 = new ProductRestClientDto(
      "2", "Dress", BigDecimal.valueOf(19.99), true
  );
  private static final ProductRestClientDto FIXED_PRODUCT_DTO_3 = new ProductRestClientDto(
      "3", "Blazer", BigDecimal.valueOf(29.99), false
  );
  private static final ProductRestClientDto FIXED_PRODUCT_DTO_4 = new ProductRestClientDto(
      "4", "Boots", BigDecimal.valueOf(39.99), true
  );

  @BeforeEach
  void setup() {
    similarProductRestClientAdapter = new SimilarProductRestClientAdapter(productRestClient,
        virtualThreadExecutor);
  }

  @Test
  void getSimilarProductIdsShouldReturnListOfProductIdsWhenClientReturnsAsExpected() {
    when(productRestClient.getSimilarProductIds("1")).thenReturn(List.of("2", "3", "4"));

    var result = similarProductRestClientAdapter.getSimilarProductIds(FIXED_PRODUCT_ID_1);

    assertEquals(3, result.size());
    assertEquals("2", result.get(0).value());
    assertEquals("3", result.get(1).value());
    assertEquals("4", result.get(2).value());
  }

  @Test
  void getSimilarProductIdsShouldReturnEmptyListWhenClientReturnsEmptyList() {
    when(productRestClient.getSimilarProductIds("1")).thenReturn(List.of());

    var result = similarProductRestClientAdapter.getSimilarProductIds(FIXED_PRODUCT_ID_1);

    assertTrue(result.isEmpty());
  }

  @Test
  void getSimilarProductIdsShouldThrowProductNotFoundExceptionWhenClientReturnsIt() {
    when(productRestClient.getSimilarProductIds("1")).thenThrow(
        new ProductNotFoundException("Product not found"));

    assertThrows(ProductNotFoundException.class,
        () -> similarProductRestClientAdapter.getSimilarProductIds(FIXED_PRODUCT_ID_1));
  }

  @Test
  void getSimilarProductIdsShouldThrowSimilarProductsRetrievalExceptionWhenClientReturnsIt() {
    when(productRestClient.getSimilarProductIds("1")).thenThrow(
        new SimilarProductsRetrievalException("Something went wrong"));

    assertThrows(SimilarProductsRetrievalException.class,
        () -> similarProductRestClientAdapter.getSimilarProductIds(FIXED_PRODUCT_ID_1));
  }

  @Test
  void getSimilarProductIdsShouldThrowRuntimeExceptionWhenClientThrowsIt() {
    when(productRestClient.getSimilarProductIds("1")).thenThrow(
        new RuntimeException("Something went wrong"));

    assertThrows(RuntimeException.class,
        () -> similarProductRestClientAdapter.getSimilarProductIds(FIXED_PRODUCT_ID_1));
  }

  @Test
  void getProductsShouldReturnListOfProductsWhenClientReturnsAsExpected() {
    when(productRestClient.getProductById("2")).thenReturn(FIXED_PRODUCT_DTO_2);
    when(productRestClient.getProductById("3")).thenReturn(FIXED_PRODUCT_DTO_3);
    when(productRestClient.getProductById("4")).thenReturn(FIXED_PRODUCT_DTO_4);

    var result = similarProductRestClientAdapter.getProducts(List.of(
        new ProductId("2"), new ProductId("3"), new ProductId("4")
    ));

    assertEquals(3, result.size());
    assertEquals("2", result.get(0).getId().value());
    assertEquals("3", result.get(1).getId().value());
    assertEquals("4", result.get(2).getId().value());
  }

  @Test
  void getProductsShouldReturnEmptyListWhenInputListIsEmpty() {
    var result = similarProductRestClientAdapter.getProducts(List.of());

    assertEquals(0, result.size());
  }

  @Test
  void getProductsShouldThrowSimilarProductsRetrievalExceptionWhenClientThrowsProductNotFoundException() {
    when(productRestClient.getProductById("2")).thenThrow(
        new ProductNotFoundException("Product not found"));

    assertThrows(SimilarProductsRetrievalException.class,
        () -> similarProductRestClientAdapter.getProducts(List.of(FIXED_PRODUCT_ID_2)));
  }

  @Test
  void getProductsShouldThrowSimilarProductsRetrievalExceptionWhenClientThrowsSimilarProductsRetrievalException() {
    when(productRestClient.getProductById("2")).thenThrow(
        new SimilarProductsRetrievalException("Something went wrong"));

    assertThrows(SimilarProductsRetrievalException.class,
        () -> similarProductRestClientAdapter.getProducts(List.of(FIXED_PRODUCT_ID_2)));
  }

  @Test
  void getProductsShouldThrowRuntimeExceptionWhenClientThrowsIt() {
    when(productRestClient.getProductById("2")).thenThrow(
        new RuntimeException("Something went wrong"));

    assertThrows(RuntimeException.class,
        () -> similarProductRestClientAdapter.getProducts(List.of(FIXED_PRODUCT_ID_2)));
  }
}