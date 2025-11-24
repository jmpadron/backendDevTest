package com.inditex.similar_products.adapter.in.web.dto;

import java.math.BigDecimal;

public record ProductWebDtoResponse(String id, String name, BigDecimal price,
                                    Boolean availability) {

}
