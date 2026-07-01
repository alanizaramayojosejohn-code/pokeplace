package com.example.demo.dto;

import com.example.demo.model.Payment;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class PaymentResponse {
    private Long id;
    private Double amount;
    private LocalDate paymentDate;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String note;

    private Long userId;
    private String userName;
    private String userEmail;

    public static PaymentResponse from(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .paymentDate(payment.getPaymentDate())
                .periodStart(payment.getPeriodStart())
                .periodEnd(payment.getPeriodEnd())
                .note(payment.getNote())
                .userId(payment.getUser() != null ? payment.getUser().getId() : null)
                .userName(payment.getUser() != null ? payment.getUser().getName() : null)
                .userEmail(payment.getUser() != null ? payment.getUser().getEmail() : null)
                .build();
    }
}
