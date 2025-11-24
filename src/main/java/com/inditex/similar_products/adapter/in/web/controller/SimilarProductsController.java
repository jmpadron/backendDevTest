package com.inditex.similar_products.adapter.in.web.controller;

import com.inditex.similar_products.adapter.in.web.dto.ProductWebDtoResponse;
import com.inditex.similar_products.adapter.in.web.mapper.ProductWebDtoResponseMapper;
import com.inditex.similar_products.application.port.in.GetSimilarProductsUseCase;
import com.inditex.similar_products.domain.model.ProductId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class SimilarProductsController {

  private final GetSimilarProductsUseCase getSimilarProductsUseCase;

  @GetMapping("/{id}/similar")
  public ResponseEntity<List<ProductWebDtoResponse>> getSimilarProducts(
      @PathVariable("id") String id) {
    var similarProducts = getSimilarProductsUseCase.getSimilarProducts(new ProductId(id))
        .stream()
        .map(ProductWebDtoResponseMapper::fromProduct)
        .toList();
    return ResponseEntity.ok(similarProducts);
  }
}
