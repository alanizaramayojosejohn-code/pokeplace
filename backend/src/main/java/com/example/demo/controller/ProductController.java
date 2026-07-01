package com.example.demo.controller;

import com.example.demo.dto.EdibleRequest;
import com.example.demo.dto.InedibleRequest;
import com.example.demo.dto.LowStockProductDTO;
import com.example.demo.dto.ProductResponse;
import com.example.demo.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAll() {
        return ResponseEntity.ok(
                productService.getAll().stream().map(ProductResponse::from).toList()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ProductResponse.from(productService.getById(id)));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponse>> getByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(
                productService.getByCategory(categoryId).stream().map(ProductResponse::from).toList()
        );
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<LowStockProductDTO>> getLowStock() {
        return ResponseEntity.ok(productService.getLowStockProducts());
    }

    @PostMapping("/edible")
    public ResponseEntity<ProductResponse> createEdible(@Valid @RequestBody EdibleRequest request) {
        return ResponseEntity.ok(ProductResponse.from(productService.create(request.toEntity())));
    }

    @PostMapping("/inedible")
    public ResponseEntity<ProductResponse> createInedible(@Valid @RequestBody InedibleRequest request) {
        return ResponseEntity.ok(ProductResponse.from(productService.create(request.toEntity())));
    }

    @PutMapping("/edible/{id}")
    public ResponseEntity<ProductResponse> updateEdible(@PathVariable Long id,
                                                        @Valid @RequestBody EdibleRequest request) {
        return ResponseEntity.ok(ProductResponse.from(productService.updateEdible(id, request.toEntity())));
    }

    @PutMapping("/inedible/{id}")
    public ResponseEntity<ProductResponse> updateInedible(@PathVariable Long id,
                                                          @Valid @RequestBody InedibleRequest request) {
        return ResponseEntity.ok(ProductResponse.from(productService.updateInedible(id, request.toEntity())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
