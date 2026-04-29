package com.example.demo.dto;

import com.example.demo.model.OrderDetail;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderDetailResponse {
    private Long id;
    private Integer quantity;
    private Double subtotal;
    private Long productId;
    private String productName;

    public static OrderDetailResponse from(OrderDetail detail) {
        return new OrderDetailResponse(
                detail.getId(),
                detail.getQuantity(),
                detail.getSubtotal(),
                detail.getProduct() != null ? detail.getProduct().getId() : null,
                detail.getProduct() != null ? detail.getProduct().getName() : null
        );
    }
}
