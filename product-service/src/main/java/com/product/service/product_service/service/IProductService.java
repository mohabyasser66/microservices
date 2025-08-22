package com.product.service.product_service.service;

import com.product.service.product_service.dto.ProductRequest;
import com.product.service.product_service.dto.ProductResponse;
import java.util.List;

public interface IProductService {

    ProductResponse createProduct(ProductRequest productRequest);

    List<ProductResponse> getAllProducts();

}
