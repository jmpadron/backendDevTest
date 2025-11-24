package com.inditex.similar_products.adapter.out.http.restclient.dto;

import java.math.BigDecimal;

public record ProductRestClientDto(String id, String name, BigDecimal price, Boolean availability) {

}
