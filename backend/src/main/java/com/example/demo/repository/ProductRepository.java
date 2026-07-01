package com.example.demo.repository;

import com.example.demo.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryId(Long categoryId);
    boolean existsByName(String name);

    @Query(value = """
        SELECT p.id, p.name, i.stock, i.min_stock
        FROM products p
        JOIN inedibles i ON p.id = i.id
        WHERE i.stock <= i.min_stock
        ORDER BY (CAST(i.stock AS float) / i.min_stock) ASC
    """, nativeQuery = true)
    List<Object[]> findLowStockProducts();

    @Query(value = """
        SELECT COALESCE(SUM(i.stock * COALESCE(p.cost, 0)), 0),
               COALESCE(SUM(i.stock * p.price), 0),
               COUNT(p.id),
               COUNT(CASE WHEN i.stock <= i.min_stock THEN 1 END)
        FROM products p
        JOIN inedibles i ON p.id = i.id
        WHERE i.stock > 0
    """, nativeQuery = true)
    List<Object[]> findInventoryValue();
}