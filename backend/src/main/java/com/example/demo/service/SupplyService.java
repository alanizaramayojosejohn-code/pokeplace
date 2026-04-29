package com.example.demo.service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Product;
import com.example.demo.model.Supply;
import com.example.demo.model.User;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.SupplyRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplyService {

    private final SupplyRepository supplyRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public List<Supply> getAll() {
        return supplyRepository.findAll();
    }

    public List<Supply> getByProduct(Long productId) {
        return supplyRepository.findByProductId(productId);
    }

    @Transactional
    public Supply create(Supply supply) {
        Product product = productRepository.findById(supply.getProduct().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        supply.setProduct(product);

        User user = userRepository.findById(supply.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        supply.setUser(user);

        return supplyRepository.save(supply);
    }

    public void delete(Long id) {
        if (!supplyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Supply not found with id: " + id);
        }
        supplyRepository.deleteById(id);
    }
}
