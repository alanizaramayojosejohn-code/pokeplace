package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MonthlyReportDTO {
    private int month;
    private long orderCount;
    private double totalSales;
    private double totalProfit;
}
