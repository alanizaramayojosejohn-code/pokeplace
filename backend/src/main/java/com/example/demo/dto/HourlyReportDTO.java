package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HourlyReportDTO {
    private int hour;
    private long orderCount;
    private double totalSales;
}
