package com.example.demo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {

    @NotNull(message = "Table number is required")
    @Min(value = 0, message = "Table number must be positive")
    private Integer tableNumber;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    private String notes;

    // Opcional (orden sin cliente registrado)
    private Long clientId;

    @NotNull(message = "User id is required")
    private Long userId;

    @NotNull(message = "Amount paid is required")
    private Double amountPaid;

    @NotEmpty(message = "Order must have at least one detail")
    @Valid
    private List<OrderDetailRequest> details;
}
