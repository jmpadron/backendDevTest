package com.inditex.similar_products.adapter.out.http.restclient;

import com.inditex.similar_products.adapter.out.http.restclient.client.ProductRestClient;
import com.inditex.similar_products.adapter.out.http.restclient.mapper.ProductRestClientDtoMapper;
import com.inditex.similar_products.application.port.out.GetProductsByIdsPort;
import com.inditex.similar_products.application.port.out.GetSimilarProductIdsPort;
import com.inditex.similar_products.domain.exception.SimilarProductsRetrievalException;
import com.inditex.similar_products.domain.model.Product;
import com.inditex.similar_products.domain.model.ProductId;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SimilarProductRestClientAdapter implements GetSimilarProductIdsPort,
    GetProductsByIdsPort {

  private final ProductRestClient productRestClient;

  private final ExecutorService virtualThreadExecutor;

  @Override
  public List<ProductId> getSimilarProductIds(ProductId productId) {
    var ids = productRestClient.getSimilarProductIds(productId.value());
    return ids.stream().map(ProductId::new).toList();
  }

  @Override
  public List<Product> getProducts(List<ProductId> productIds) {
    var futures = productIds.stream().map(
        id -> CompletableFuture.supplyAsync(() -> getProductById(id.value()), virtualThreadExecutor)
    ).toList();
    try {
      return futures.stream().map(CompletableFuture::join).toList();
    } catch (CompletionException e) {
      futures.forEach(f -> f.cancel(true));
      throw new SimilarProductsRetrievalException(e.getCause().getMessage());
    }
  }

  private Product getProductById(String id) {
    var productDto = productRestClient.getProductById(id);
    return ProductRestClientDtoMapper.toProduct(productDto);
  }
}
