package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class PaymentRequest {

    @NotNull(message = "User id is required")
    private Long userId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;

    @NotNull(message = "Payment date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate paymentDate;

    @NotNull(message = "Period start is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate periodStart;

    @NotNull(message = "Period end is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate periodEnd;

    private String note;
}
