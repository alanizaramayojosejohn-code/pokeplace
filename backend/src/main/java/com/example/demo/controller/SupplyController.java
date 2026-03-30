package com.example.demo.controller;

import com.example.demo.model.Supply;
import com.example.demo.service.SupplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supplies")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class SupplyController {

    private final SupplyService supplyService;

    @GetMapping
    public ResponseEntity<List<Supply>> getAll() {
        return ResponseEntity.ok(supplyService.getAll());
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Supply>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(supplyService.getByProduct(productId));
    }

    @PostMapping
    public ResponseEntity<Supply> create(@RequestBody Supply supply) {
        return ResponseEntity.ok(supplyService.create(supply));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        supplyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}