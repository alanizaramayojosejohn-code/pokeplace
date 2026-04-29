package com.example.demo.controller;

import com.example.demo.dto.SupplyRequest;
import com.example.demo.dto.SupplyResponse;
import com.example.demo.model.Product;
import com.example.demo.model.Supply;
import com.example.demo.model.User;
import com.example.demo.service.SupplyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supplies")
@RequiredArgsConstructor
public class SupplyController {

    private final SupplyService supplyService;

    @GetMapping
    public ResponseEntity<List<SupplyResponse>> getAll() {
        return ResponseEntity.ok(
                supplyService.getAll().stream().map(SupplyResponse::from).toList()
        );
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<SupplyResponse>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(
                supplyService.getByProduct(productId).stream().map(SupplyResponse::from).toList()
        );
    }

    @PostMapping
    public ResponseEntity<SupplyResponse> create(@Valid @RequestBody SupplyRequest request) {
        Product product = new Product();
        product.setId(request.getProductId());

        User user = new User();
        user.setId(request.getUserId());

        Supply supply = Supply.builder()
                .note(request.getNote())
                .product(product)
                .user(user)
                .build();

        return ResponseEntity.ok(SupplyResponse.from(supplyService.create(supply)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        supplyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
