package com.example.demo.dto;

import com.example.demo.model.Supply;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SupplyResponse {
    private Long id;
    private String note;
    private Long productId;
    private String productName;
    private Long userId;
    private String userEmail;
    private LocalDateTime createdAt;

    public static SupplyResponse from(Supply supply) {
        return SupplyResponse.builder()
                .id(supply.getId())
                .note(supply.getNote())
                .productId(supply.getProduct() != null ? supply.getProduct().getId() : null)
                .productName(supply.getProduct() != null ? supply.getProduct().getName() : null)
                .userId(supply.getUser() != null ? supply.getUser().getId() : null)
                .userEmail(supply.getUser() != null ? supply.getUser().getEmail() : null)
                .createdAt(supply.getCreatedAt())
                .build();
    }
}
