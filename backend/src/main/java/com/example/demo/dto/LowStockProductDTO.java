package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LowStockProductDTO {
    private Long id;
    private String name;
    private int stock;
    private int minStock;
}
