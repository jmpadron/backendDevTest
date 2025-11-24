package com.inditex.similar_products.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.inditex.similar_products.application.port.out.GetProductsByIdsPort;
import com.inditex.similar_products.application.port.out.GetSimilarProductIdsPort;
import com.inditex.similar_products.domain.exception.ProductNotFoundException;
import com.inditex.similar_products.domain.exception.SimilarProductsRetrievalException;
import com.inditex.similar_products.domain.model.Product;
import com.inditex.similar_products.domain.model.ProductId;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetSimilarProductsServiceTest {

  private static final ProductId FIXED_PRODUCT_ID = new ProductId("1");
  private static final List<ProductId> FIXED_LIST_OF_PRODUCT_IDS = List.of(
      new ProductId("2"), new ProductId("3"), new ProductId("4")
  );
  private static final List<Product> FIXED_LIST_OF_SIMILAR_PRODUCTS = List.of(
      new Product(FIXED_LIST_OF_PRODUCT_IDS.get(0), "Dress", BigDecimal.valueOf(19.99), true),
      new Product(FIXED_LIST_OF_PRODUCT_IDS.get(1), "Blazer", BigDecimal.valueOf(29.99), false),
      new Product(FIXED_LIST_OF_PRODUCT_IDS.get(2), "Boots", BigDecimal.valueOf(39.99), true)
  );

  @Mock
  private GetProductsByIdsPort getProductsByIdsPort;
  @Mock
  private GetSimilarProductIdsPort getSimilarProductIdsPort;

  @InjectMocks
  private GetSimilarProductsService getSimilarProductsService;

  @Test
  void shouldReturnSimilarProductsWhenPortsReturnAsExpected() {
    when(getSimilarProductIdsPort.getSimilarProductIds(FIXED_PRODUCT_ID)).thenReturn(
        FIXED_LIST_OF_PRODUCT_IDS);
    when(getProductsByIdsPort.getProducts(FIXED_LIST_OF_PRODUCT_IDS)).thenReturn(
        FIXED_LIST_OF_SIMILAR_PRODUCTS);

    var products = getSimilarProductsService.getSimilarProducts(FIXED_PRODUCT_ID);

    assertEquals(FIXED_LIST_OF_SIMILAR_PRODUCTS, products);
  }

  @Test
  void shouldThrowProductNotFoundExceptionWhenGetIdsPortThrowsIt() {
    when(getSimilarProductIdsPort.getSimilarProductIds(FIXED_PRODUCT_ID)).thenThrow(
        new ProductNotFoundException("Product not found")
    );

    assertThrows(ProductNotFoundException.class,
        () -> getSimilarProductsService.getSimilarProducts(FIXED_PRODUCT_ID));
  }

  @Test
  void shouldThrowSimilarProductsRetrievalExceptionWhenGetIdsPortThrowsIt() {
    when(getSimilarProductIdsPort.getSimilarProductIds(FIXED_PRODUCT_ID)).thenThrow(
        new SimilarProductsRetrievalException("Something went wrong")
    );

    assertThrows(SimilarProductsRetrievalException.class,
        () -> getSimilarProductsService.getSimilarProducts(FIXED_PRODUCT_ID));
  }

  @Test
  void shouldThrowSimilarProductsRetrievalExceptionWhenGetProductsPortThrowsIt() {
    when(getSimilarProductIdsPort.getSimilarProductIds(FIXED_PRODUCT_ID)).thenReturn(
        FIXED_LIST_OF_PRODUCT_IDS);
    when(getProductsByIdsPort.getProducts(FIXED_LIST_OF_PRODUCT_IDS)).thenThrow(
        new SimilarProductsRetrievalException("Something went wrong")
    );

    assertThrows(SimilarProductsRetrievalException.class,
        () -> getSimilarProductsService.getSimilarProducts(FIXED_PRODUCT_ID));
  }

  @Test
  void shouldPropagateExceptionWhenGetIdsPortThrowsIt() {
    when(getSimilarProductIdsPort.getSimilarProductIds(FIXED_PRODUCT_ID)).thenThrow(
        new RuntimeException("Something went wrong")
    );
    assertThrows(RuntimeException.class,
        () -> getSimilarProductsService.getSimilarProducts(FIXED_PRODUCT_ID));
  }

  @Test
  void shouldPropagateExceptionWhenGetProductsPortThrowsIt() {
    when(getSimilarProductIdsPort.getSimilarProductIds(FIXED_PRODUCT_ID)).thenReturn(
        FIXED_LIST_OF_PRODUCT_IDS);
    when(getProductsByIdsPort.getProducts(FIXED_LIST_OF_PRODUCT_IDS)).thenThrow(
        new RuntimeException("Something went wrong")
    );
    assertThrows(RuntimeException.class,
        () -> getSimilarProductsService.getSimilarProducts(FIXED_PRODUCT_ID));
  }
}