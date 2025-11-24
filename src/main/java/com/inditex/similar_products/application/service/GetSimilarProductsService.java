package com.inditex.similar_products.application.service;

import com.inditex.similar_products.application.port.in.GetSimilarProductsUseCase;
import com.inditex.similar_products.application.port.out.GetProductsByIdsPort;
import com.inditex.similar_products.application.port.out.GetSimilarProductIdsPort;
import com.inditex.similar_products.domain.model.Product;
import com.inditex.similar_products.domain.model.ProductId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetSimilarProductsService implements GetSimilarProductsUseCase {
  private final GetSimilarProductIdsPort getSimilarProductIdsPort;
  private final GetProductsByIdsPort getProductsByIdsPort;

  @Override
  public List<Product> getSimilarProducts(ProductId productId) {
    var similarProductIds = getSimilarProductIdsPort.getSimilarProductIds(productId);
    return getProductsByIdsPort.getProducts(similarProductIds);
  }
}
