package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserSalesReportDTO {
    private Long userId;
    private String userName;
    private long orderCount;
    private double totalSales;
}
