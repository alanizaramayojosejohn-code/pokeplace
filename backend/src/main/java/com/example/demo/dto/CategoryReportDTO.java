package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategoryReportDTO {
    private String categoryName;
    private long quantitySold;
    private double revenue;
}
