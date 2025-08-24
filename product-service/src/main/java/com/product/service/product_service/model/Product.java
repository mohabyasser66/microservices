package com.product.service.product_service.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Document(value = "Product")
@Builder
@Data
public class Product {

    @Id
    private String id;
    private String skuCode;
    private String description;
    private BigDecimal price;

}
