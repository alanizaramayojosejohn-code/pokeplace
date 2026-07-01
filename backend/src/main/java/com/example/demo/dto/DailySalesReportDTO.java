package com.example.demo.dto;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;

// DailySalesReportDTO.java
@Data 
@AllArgsConstructor
public class DailySalesReportDTO {
    private LocalDate date;
    private Long orderCount;
    private Double totalSales;
}