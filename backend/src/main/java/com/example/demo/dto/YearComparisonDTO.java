package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class YearComparisonDTO {
    private int year1;
    private int year2;
    private double year1Sales;
    private double year2Sales;
    private double year1Profit;
    private double year2Profit;
    private double salesGrowthPercent;
    private double profitGrowthPercent;
    private List<MonthlyReportDTO> year1Monthly;
    private List<MonthlyReportDTO> year2Monthly;
}
