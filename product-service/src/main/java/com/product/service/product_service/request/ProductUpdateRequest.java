package com.product.service.product_service.request;

import com.product.service.product_service.model.Category;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductUpdateRequest {
    private Long id;
    private String name;
    private BigDecimal price;
    private String description;
    private Category category;
}
