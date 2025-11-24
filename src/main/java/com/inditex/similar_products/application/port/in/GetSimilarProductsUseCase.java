package com.inditex.similar_products.application.port.in;

import com.inditex.similar_products.domain.model.Product;
import com.inditex.similar_products.domain.model.ProductId;
import java.util.List;

public interface GetSimilarProductsUseCase {

  List<Product> getSimilarProducts(ProductId productId);
}
