package com.example.demo.dto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data 
@AllArgsConstructor
public class PaymentMethodReportDTO {
    private String paymentMethod;
    private Long orderCount;
    private Double total;
}
