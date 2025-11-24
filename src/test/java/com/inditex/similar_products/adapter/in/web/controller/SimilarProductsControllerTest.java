package com.inditex.similar_products.adapter.in.web.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.inditex.similar_products.application.port.in.GetSimilarProductsUseCase;
import com.inditex.similar_products.domain.exception.ProductNotFoundException;
import com.inditex.similar_products.domain.exception.SimilarProductsRetrievalException;
import com.inditex.similar_products.domain.model.Product;
import com.inditex.similar_products.domain.model.ProductId;
import com.jayway.jsonpath.JsonPath;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(SimilarProductsController.class)
@Import(GlobalExceptionHandler.class)
class SimilarProductsControllerTest {

  private static final ProductId FIXED_PRODUCT_ID = new ProductId("1");
  private static final String BASE_URL = "/product";
  private static final List<Product> FIXED_LIST_OF_SIMILAR_PRODUCTS = List.of(
      new Product(new ProductId("2"), "Dress", BigDecimal.valueOf(19.99), true),
      new Product(new ProductId("3"), "Blazer", BigDecimal.valueOf(29.99), false),
      new Product(new ProductId("4"), "Boots", BigDecimal.valueOf(39.99), true)
  );

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private GetSimilarProductsUseCase getSimilarProductsUseCase;

  @Test
  void shouldResponse404WhenEndpointNotFound() throws Exception {
    var request = MockMvcRequestBuilders
        .get(BASE_URL + "/missing")
        .contentType(MediaType.APPLICATION_JSON);

    var response = mockMvc.perform(request).andReturn();

    assertEquals(HttpStatus.NOT_FOUND.value(), response.getResponse().getStatus());
  }

  @Test
  void shouldResponse200AndSimilarProductsWhenServiceReturnProducts() throws Exception {
    when(getSimilarProductsUseCase.getSimilarProducts(FIXED_PRODUCT_ID))
        .thenReturn(FIXED_LIST_OF_SIMILAR_PRODUCTS);
    var request = MockMvcRequestBuilders
        .get(BASE_URL + "/{id}/similar", "1")
        .contentType(MediaType.APPLICATION_JSON);

    var response = mockMvc.perform(request).andReturn();

    assertEquals(HttpStatus.OK.value(), response.getResponse().getStatus());
    var document = JsonPath.parse(response.getResponse().getContentAsString());
    assertEquals(FIXED_LIST_OF_SIMILAR_PRODUCTS.get(0).getId().value(), document.read("$[0].id"));
    assertEquals(FIXED_LIST_OF_SIMILAR_PRODUCTS.get(1).getId().value(), document.read("$[1].id"));
    assertEquals(FIXED_LIST_OF_SIMILAR_PRODUCTS.get(2).getId().value(), document.read("$[2].id"));
  }

  @Test
  void shouldResponse200AndEmptyListWhenServiceReturnsEmptyList() throws Exception {
    when(getSimilarProductsUseCase.getSimilarProducts(FIXED_PRODUCT_ID))
        .thenReturn(List.of());
    var request = MockMvcRequestBuilders
        .get(BASE_URL + "/{id}/similar", "1")
        .contentType(MediaType.APPLICATION_JSON);

    var response = mockMvc.perform(request).andReturn();

    assertEquals(HttpStatus.OK.value(), response.getResponse().getStatus());
    var document = JsonPath.parse(response.getResponse().getContentAsString());
    assertEquals(0, (Integer) document.read("$.length()"));
  }

  @Test
  void shouldResponse404WhenServiceThrowsProductNotFoundException() throws Exception {
    when(getSimilarProductsUseCase.getSimilarProducts(FIXED_PRODUCT_ID))
        .thenThrow(new ProductNotFoundException("Product not found"));

    var request = MockMvcRequestBuilders
        .get(BASE_URL + "/{id}/similar", "1")
        .contentType(MediaType.APPLICATION_JSON);

    var response = mockMvc.perform(request).andReturn();

    assertEquals(HttpStatus.NOT_FOUND.value(), response.getResponse().getStatus());
  }

  @Test
  void shouldResponse500WhenServiceThrowsSimilarProductsRetrievalException() throws Exception {
    when(getSimilarProductsUseCase.getSimilarProducts(FIXED_PRODUCT_ID))
        .thenThrow(new SimilarProductsRetrievalException("Something went wrong"));
    var request = MockMvcRequestBuilders
        .get(BASE_URL + "/{id}/similar", "1")
        .contentType(MediaType.APPLICATION_JSON);

    var response = mockMvc.perform(request).andReturn();

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getResponse().getStatus());
    var document = JsonPath.parse(response.getResponse().getContentAsString());
    assertEquals("Failed to retrieve similar products", document.read("$.title"));
  }

  @Test
  void shouldResponse500WhenServiceThrowsRuntimeException() throws Exception {
    when(getSimilarProductsUseCase.getSimilarProducts(FIXED_PRODUCT_ID))
        .thenThrow(new RuntimeException("Something went wrong"));
    var request = MockMvcRequestBuilders
        .get(BASE_URL + "/{id}/similar", "1")
        .contentType(MediaType.APPLICATION_JSON);

    var response = mockMvc.perform(request).andReturn();

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getResponse().getStatus());
    var document = JsonPath.parse(response.getResponse().getContentAsString());
    assertEquals("Internal Server Error", document.read("$.title"));
  }
}