package com.example.demo.dto;

import com.example.demo.model.Order;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private Long id;
    private Integer tableNumber;
    private LocalDateTime dateTime;
    private String paymentMethod;
    private String status;
    private Double total;
    private String notes;

    private Long clientId;
    private String clientName;

    private Long userId;
    private String userName;

    private List<OrderDetailResponse> details;

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .tableNumber(order.getTableNumber())
                .dateTime(order.getDateTime())
                .paymentMethod(order.getPaymentMethod())
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .total(order.getTotal())
                .notes(order.getNotes())
                .clientId(order.getClient() != null ? order.getClient().getId() : null)
                .clientName(order.getClient() != null ? order.getClient().getName() : null)
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .userName(order.getUser() != null ? order.getUser().getName() : null)
                .details(order.getDetails() != null
                        ? order.getDetails().stream().map(OrderDetailResponse::from).toList()
                        : Collections.emptyList())
                .build();
    }
}
