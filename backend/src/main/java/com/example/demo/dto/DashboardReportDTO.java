package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class DashboardReportDTO {
    private double todaySales;
    private double yesterdaySales;
    private double salesGrowthPercent;
    private double weekSales;
    private double monthSales;
    private double monthProfit;
    private long todayOrders;
    private long weekOrders;
    private long monthOrders;
    private long pendingOrders;
    private List<ProductReportDTO> topProducts;
    private List<DailySalesReportDTO> weeklyTrend;
    private List<PaymentMethodReportDTO> salesByPaymentMethod;
}
