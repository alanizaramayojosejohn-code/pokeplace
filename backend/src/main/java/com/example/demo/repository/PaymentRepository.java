package com.example.demo.repository;

import com.example.demo.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUserId(Long userId);
    List<Payment> findByPaymentDateBetween(LocalDate start, LocalDate end);
}
