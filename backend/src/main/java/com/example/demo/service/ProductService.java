package com.example.demo.service;

import com.example.demo.config.SecurityUtils;
import com.example.demo.dto.LowStockProductDTO;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Audit;
import com.example.demo.model.Category;
import com.example.demo.model.Edible;
import com.example.demo.model.Inedible;
import com.example.demo.model.Product;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final AuditService auditService;
    private final SecurityUtils securityUtils;

    public List<Product> getAll() {
        return productRepository.findAll();
    }

    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    public List<Product> getByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    @Transactional
    public Product create(Product product) {
        if (productRepository.existsByName(product.getName())) {
            throw new DuplicateResourceException("Product already exists: " + product.getName());
        }
        Category category = categoryRepository.findById(product.getCategory().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        product.setCategory(category);
        Product saved = productRepository.save(product);
        auditService.log("PRODUCT", saved.getId(), Audit.AuditAction.CREATE,
                null, snapshot(saved), securityUtils.getCurrentUsername());
        return saved;
    }

    @Transactional
    public Edible updateEdible(Long id, Edible updated) {
        Product existing = getById(id);
        if (!(existing instanceof Edible edible)) {
            throw new BadRequestException("Product with id " + id + " is not an Edible");
        }
        Map<String, Object> previous = snapshot(edible);
        edible.setName(updated.getName());
        edible.setPrice(updated.getPrice());
        edible.setPokeName(updated.getPokeName());
        edible.setDescription(updated.getDescription());
        edible.setCost(updated.getCost());
        if (updated.getCategory() != null) {
            Category category = categoryRepository.findById(updated.getCategory().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            edible.setCategory(category);
        }
        Edible saved = productRepository.save(edible);
        auditService.log("PRODUCT", id, Audit.AuditAction.UPDATE,
                previous, snapshot(saved), securityUtils.getCurrentUsername());
        return saved;
    }

    @Transactional
    public Inedible updateInedible(Long id, Inedible updated) {
        Product existing = getById(id);
        if (!(existing instanceof Inedible inedible)) {
            throw new BadRequestException("Product with id " + id + " is not an Inedible");
        }
        Map<String, Object> previous = snapshot(inedible);
        inedible.setName(updated.getName());
        inedible.setPrice(updated.getPrice());
        inedible.setStock(updated.getStock());
        inedible.setMinStock(updated.getMinStock());
        inedible.setDescription(updated.getDescription());
        inedible.setCost(updated.getCost());
        if (updated.getCategory() != null) {
            Category category = categoryRepository.findById(updated.getCategory().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            inedible.setCategory(category);
        }
        Inedible saved = productRepository.save(inedible);
        auditService.log("PRODUCT", id, Audit.AuditAction.UPDATE,
                previous, snapshot(saved), securityUtils.getCurrentUsername());
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        Product existing = getById(id);
        productRepository.deleteById(id);
        auditService.log("PRODUCT", id, Audit.AuditAction.DELETE,
                snapshot(existing), null, securityUtils.getCurrentUsername());
    }

    public List<LowStockProductDTO> getLowStockProducts() {
        return productRepository.findLowStockProducts()
            .stream()
            .map(row -> new LowStockProductDTO(
                ((Number) row[0]).longValue(),
                (String) row[1],
                ((Number) row[2]).intValue(),
                ((Number) row[3]).intValue()
            ))
            .collect(java.util.stream.Collectors.toList());
    }

    private Map<String, Object> snapshot(Product p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("name", p.getName());
        m.put("price", p.getPrice());
        m.put("category", p.getCategory() != null ? p.getCategory().getName() : null);
        if (p instanceof Edible e) {
            m.put("pokeName", e.getPokeName());
            m.put("cost", e.getCost());
        } else if (p instanceof Inedible i) {
            m.put("stock", i.getStock());
            m.put("minStock", i.getMinStock());
            m.put("cost", i.getCost());
        }
        return m;
    }
}
