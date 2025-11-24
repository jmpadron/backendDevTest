package com.inditex.similar_products.application.port.out;

import com.inditex.similar_products.domain.model.ProductId;
import java.util.List;

public interface GetSimilarProductIdsPort {
  List<ProductId> getSimilarProductIds(ProductId productId);
}
