package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class InventoryValueDTO {
    private double totalCostValue;
    private double totalSaleValue;
    private double potentialProfit;
    private long totalProducts;
    private long lowStockProducts;
}
