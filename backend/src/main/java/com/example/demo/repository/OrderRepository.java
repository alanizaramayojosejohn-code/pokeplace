package com.example.demo.repository;

import com.example.demo.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatus(Order.OrderStatus status);
    List<Order> findByClientId(Long clientId);
     @Query("SELECT o FROM Order o WHERE o.dateTime BETWEEN :start AND :end AND o.status = 'DELIVERED'")
    List<Order> findDeliveredByDateBetween(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT CAST(o.dateTime AS date), COUNT(o), SUM(o.total)
        FROM Order o
        WHERE o.status = 'DELIVERED'
        AND o.dateTime BETWEEN :start AND :end
        GROUP BY CAST(o.dateTime AS date)
        ORDER BY CAST(o.dateTime AS date)
    """)
    List<Object[]> findDailySales(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT p.name, SUM(od.quantity), SUM(od.subtotal)
        FROM OrderDetail od
        JOIN od.product p
        JOIN od.order o
        WHERE o.status = 'DELIVERED'
        GROUP BY p.name
        ORDER BY SUM(od.quantity) DESC
    """)
    List<Object[]> findBestSellingProducts();

    @Query("""
        SELECT o.paymentMethod, COUNT(o), SUM(o.total)
        FROM Order o
        WHERE o.status = 'DELIVERED'
        AND o.dateTime BETWEEN :start AND :end
        GROUP BY o.paymentMethod
    """)
    List<Object[]> findSalesByPaymentMethod(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Query("""
    SELECT p.name, SUM(od.quantity), SUM(od.subtotal)
    FROM OrderDetail od
    JOIN od.product p
    JOIN od.order o
    WHERE o.status = 'DELIVERED'
    AND o.dateTime BETWEEN :start AND :end
    GROUP BY p.name
    ORDER BY SUM(od.quantity) DESC
""")
List<Object[]> findBestSellingProductsByDateRange(
    @Param("start") LocalDateTime start,
    @Param("end") LocalDateTime end
);
}