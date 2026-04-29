package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SupplyRequest {

    private String note;

    @NotNull(message = "Product id is required")
    private Long productId;

    @NotNull(message = "User id is required")
    private Long userId;
}
