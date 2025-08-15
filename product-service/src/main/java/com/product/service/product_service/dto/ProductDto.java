package com.product.service.product_service.dto;

import com.product.service.product_service.model.Category;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDto {
    private long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Category category;
    private List<ImageDto> images;
}
