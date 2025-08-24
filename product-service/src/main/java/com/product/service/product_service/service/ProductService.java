package com.product.service.product_service.service;

import com.product.service.product_service.dto.InventoryResponse;
import com.product.service.product_service.dto.ProductRequest;
import com.product.service.product_service.dto.ProductResponse;
import com.product.service.product_service.exceptions.AlreadyExistsException;
import com.product.service.product_service.exceptions.ResourceNotFoundException;
import com.product.service.product_service.model.Product;
import com.product.service.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductService implements IProductService {

    private final ProductRepository productRepository;
    private final RestClient inventoryRestClient;

    public ProductResponse createProduct(ProductRequest productRequest) {
        InventoryResponse inventoryResponse = inventoryRestClient.get()
                .uri("/{skuCode}", productRequest.getSkuCode())
                .retrieve()
                .body(InventoryResponse.class);

        if (inventoryResponse != null) {
            throw new AlreadyExistsException(
                "Product with SKU " + productRequest.getSkuCode() + " already exists in inventory."
            );
        }
        Product product = Product.builder().skuCode(productRequest.getSkuCode())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .build();
        productRepository.save(product);
        log.info("Product Created Succssefully");
        return ProductResponse.builder()
                .id(product.getId())
                .skuCode(product.getSkuCode())
                .description(product.getDescription())
                .price(product.getPrice())
                .build();
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse getProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return mapToResponse(product);
    }

    public ProductResponse updateProduct(String id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        product.setSkuCode(request.getSkuCode());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());

        return mapToResponse(productRepository.save(product));
    }

    public void deleteProduct(String id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .skuCode(product.getSkuCode())
                .description(product.getDescription())
                .price(product.getPrice())
                .build();
    }

}
