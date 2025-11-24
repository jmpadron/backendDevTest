package com.inditex.similar_products.adapter.out.http.restclient.mapper;

import com.inditex.similar_products.adapter.out.http.restclient.dto.ProductRestClientDto;
import com.inditex.similar_products.domain.model.Product;
import com.inditex.similar_products.domain.model.ProductId;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ProductRestClientDtoMapper {

  public static Product toProduct(ProductRestClientDto productRestClientDto) {
    return new Product(
        new ProductId(productRestClientDto.id()),
        productRestClientDto.name(),
        productRestClientDto.price(),
        productRestClientDto.availability()
    );
  }
}
