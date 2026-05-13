package com.example.demo.dto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data 
@AllArgsConstructor
public class ProductReportDTO {
    private String productName;
    private Long quantitySold;
    private Double revenue;
}
