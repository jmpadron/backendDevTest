package com.inditex.similar_products.domain.model;

import java.math.BigDecimal;

public class Product {

  private final ProductId id;
  private final String name;
  private final BigDecimal price;
  private final Boolean isAvailable;

  public Product(ProductId id, String name, BigDecimal price, Boolean isAvailable) {
    this.id = id;
    this.name = name;
    this.price = price;
    this.isAvailable = isAvailable;
  }

  public ProductId getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public Boolean getIsAvailable() {
    return isAvailable;
  }
}
