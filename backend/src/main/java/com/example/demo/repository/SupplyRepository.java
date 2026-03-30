package com.example.demo.repository;

import com.example.demo.model.Supply;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SupplyRepository extends JpaRepository<Supply, Long> {
    List<Supply> findByProductId(Long productId);
}