package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClientSalesReportDTO {
    private Long clientId;
    private String clientName;
    private long orderCount;
    private double totalSales;
}
