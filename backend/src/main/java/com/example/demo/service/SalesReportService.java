package com.example.demo.service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.example.demo.dto.DailySalesReportDTO;
import com.example.demo.dto.PaymentMethodReportDTO;
import com.example.demo.dto.ProductReportDTO;
import com.example.demo.dto.SalesReportSummaryDTO;
import com.example.demo.repository.OrderRepository;

// SalesReportService.java
@Service
@RequiredArgsConstructor
public class SalesReportService {

    private final OrderRepository orderRepository;

    public SalesReportSummaryDTO getSummary(LocalDate start, LocalDate end) {
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(23, 59, 59);

        List<DailySalesReportDTO> dailySales = getDailySales(startDateTime, endDateTime);
        List<ProductReportDTO> bestSelling = getBestSellingProductsByDateRange(startDateTime, endDateTime);
        List<PaymentMethodReportDTO> byPayment = getSalesByPaymentMethod(startDateTime, endDateTime);

        Double periodTotal = dailySales.stream()
            .mapToDouble(DailySalesReportDTO::getTotalSales)
            .sum();

        Long totalOrders = dailySales.stream()
            .mapToLong(DailySalesReportDTO::getOrderCount)
            .sum();

        return new SalesReportSummaryDTO(periodTotal, totalOrders, dailySales, bestSelling, byPayment);
    }
    private List<ProductReportDTO> getBestSellingProductsByDateRange(LocalDateTime start, LocalDateTime end) {
    return orderRepository.findBestSellingProductsByDateRange(start, end)
        .stream()
        .map(row -> new ProductReportDTO(
            (String) row[0],
            (Long) row[1],
            (Double) row[2]
        ))
        .collect(Collectors.toList());
}
    private List<DailySalesReportDTO> getDailySales(LocalDateTime start, LocalDateTime end) {
        return orderRepository.findDailySales(start, end)
            .stream()
            .map(row -> new DailySalesReportDTO(
                ((java.sql.Date) row[0]).toLocalDate(),
                (Long) row[1],
                (Double) row[2]
            ))
            .collect(Collectors.toList());
    }

    private List<PaymentMethodReportDTO> getSalesByPaymentMethod(LocalDateTime start, LocalDateTime end) {
        return orderRepository.findSalesByPaymentMethod(start, end)
            .stream()
            .map(row -> new PaymentMethodReportDTO(
                (String) row[0],
                (Long) row[1],
                (Double) row[2]
            ))
            .collect(Collectors.toList());
    }
}
