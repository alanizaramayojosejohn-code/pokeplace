package com.example.demo.service;

import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Category;
import com.example.demo.model.Edible;
import com.example.demo.model.Inedible;
import com.example.demo.model.Product;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

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
        return productRepository.save(product);
    }

    @Transactional
    public Edible updateEdible(Long id, Edible updated) {
        Product existing = getById(id);
        if (!(existing instanceof Edible edible)) {
            throw new BadRequestException("Product with id " + id + " is not an Edible");
        }
        edible.setName(updated.getName());
        edible.setPrice(updated.getPrice());
        edible.setPokeName(updated.getPokeName());
        if (updated.getCategory() != null) {
            Category category = categoryRepository.findById(updated.getCategory().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            edible.setCategory(category);
        }
        return productRepository.save(edible);
    }

    @Transactional
    public Inedible updateInedible(Long id, Inedible updated) {
        Product existing = getById(id);
        if (!(existing instanceof Inedible inedible)) {
            throw new BadRequestException("Product with id " + id + " is not an Inedible");
        }
        inedible.setName(updated.getName());
        inedible.setPrice(updated.getPrice());
        inedible.setStock(updated.getStock());
        inedible.setMinStock(updated.getMinStock());
        if (updated.getCategory() != null) {
            Category category = categoryRepository.findById(updated.getCategory().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            inedible.setCategory(category);
        }
        return productRepository.save(inedible);
    }

    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }
}
