package com.example.demo.repository;

import com.example.demo.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.client LEFT JOIN FETCH o.user LEFT JOIN FETCH o.details d LEFT JOIN FETCH d.product ORDER BY o.dateTime DESC")
    List<Order> findAllWithDetails();

    List<Order> findByStatus(Order.OrderStatus status);
    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);
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

    @Query(value = """
        SELECT EXTRACT(MONTH FROM o.date_time) AS month,
               COUNT(DISTINCT o.id) AS orderCount,
               SUM(o.total) AS totalSales,
               SUM(d.subtotal - d.quantity * COALESCE(p.cost, 0)) AS totalProfit
        FROM orders o
        JOIN order_details d ON o.id = d.id_order
        JOIN products p ON d.id_product = p.id
        WHERE o.status = 'DELIVERED'
          AND EXTRACT(YEAR FROM o.date_time) = :year
        GROUP BY EXTRACT(MONTH FROM o.date_time)
        ORDER BY EXTRACT(MONTH FROM o.date_time)
    """, nativeQuery = true)
    List<Object[]> findMonthlyReport(@Param("year") int year);

    @Query("""
        SELECT u.id, u.name, u.lastname, COUNT(DISTINCT o.id), SUM(o.total)
        FROM Order o
        JOIN o.user u
        WHERE o.status = 'DELIVERED'
        AND o.dateTime BETWEEN :start AND :end
        GROUP BY u.id, u.name, u.lastname
        ORDER BY SUM(o.total) DESC
    """)
    List<Object[]> findSalesByUser(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT c.id, c.name, COUNT(DISTINCT o.id), SUM(o.total)
        FROM Order o
        JOIN o.client c
        WHERE o.status = 'DELIVERED'
        AND o.dateTime BETWEEN :start AND :end
        GROUP BY c.id, c.name
        ORDER BY SUM(o.total) DESC
    """)
    List<Object[]> findSalesByClient(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT SUM(o.total), COUNT(o)
        FROM Order o
        WHERE o.status = 'DELIVERED'
        AND o.dateTime BETWEEN :start AND :end
    """)
    List<Object[]> findSalesTotalAndCountBetween(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT EXTRACT(HOUR FROM o.dateTime), COUNT(o), SUM(o.total)
        FROM Order o
        WHERE o.status = 'DELIVERED'
        AND o.dateTime BETWEEN :start AND :end
        GROUP BY EXTRACT(HOUR FROM o.dateTime)
        ORDER BY EXTRACT(HOUR FROM o.dateTime)
    """)
    List<Object[]> findHourlySales(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT cat.name, SUM(od.quantity), SUM(od.subtotal)
        FROM OrderDetail od
        JOIN od.product p
        JOIN p.category cat
        JOIN od.order o
        WHERE o.status = 'DELIVERED'
        AND o.dateTime BETWEEN :start AND :end
        GROUP BY cat.name
        ORDER BY SUM(od.subtotal) DESC
    """)
    List<Object[]> findSalesByCategory(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    long countByStatus(Order.OrderStatus status);

    @Query("""
        SELECT o FROM Order o
        LEFT JOIN FETCH o.client
        JOIN FETCH o.user
        WHERE o.status = 'DELIVERED'
        AND o.dateTime BETWEEN :start AND :end
        ORDER BY o.dateTime DESC
    """)
    List<Order> findDeliveredOrdersWithDetails(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Query(value = """
        SELECT EXTRACT(MONTH FROM o.date_time) AS month,
               COUNT(DISTINCT o.id) AS orderCount,
               SUM(o.total) AS totalSales,
               SUM(d.subtotal - d.quantity * COALESCE(p.cost, 0)) AS totalProfit
        FROM orders o
        JOIN order_details d ON o.id = d.id_order
        JOIN products p ON d.id_product = p.id
        WHERE o.status = 'DELIVERED'
          AND EXTRACT(YEAR FROM o.date_time) = :year
          AND EXTRACT(MONTH FROM o.date_time) = :month
        GROUP BY EXTRACT(MONTH FROM o.date_time)
    """, nativeQuery = true)
    List<Object[]> findMonthlyReportForMonth(
        @Param("year") int year,
        @Param("month") int month
    );

    @Modifying
    @Query("UPDATE Order o SET o.createdBy = :name, o.updatedBy = :name WHERE o.id = :id")
    void setAuditUser(@Param("id") Long id, @Param("name") String name);
}