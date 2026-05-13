package com.example.demo.dto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data 
@AllArgsConstructor
public class SalesReportSummaryDTO {
    private Double periodTotal;
    private Long totalOrders;
    private List<DailySalesReportDTO> dailySales;
    private List<ProductReportDTO> bestSellingProducts;
    private List<PaymentMethodReportDTO> salesByPaymentMethod;
}
