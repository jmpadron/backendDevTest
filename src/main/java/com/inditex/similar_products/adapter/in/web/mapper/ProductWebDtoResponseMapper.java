package com.inditex.similar_products.adapter.in.web.mapper;

import com.inditex.similar_products.adapter.in.web.dto.ProductWebDtoResponse;
import com.inditex.similar_products.domain.model.Product;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ProductWebDtoResponseMapper {

  public static ProductWebDtoResponse fromProduct(Product product) {
    return new ProductWebDtoResponse(
        product.getId().value(),
        product.getName(),
        product.getPrice(),
        product.getIsAvailable()
    );
  }
}
