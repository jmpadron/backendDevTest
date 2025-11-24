package com.inditex.similar_products.application.port.out;

import com.inditex.similar_products.domain.model.Product;
import com.inditex.similar_products.domain.model.ProductId;
import java.util.List;

public interface GetProductsByIdsPort {

  List<Product> getProducts(List<ProductId> productIds);
}
