package com.example.demo.controller;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.dto.*;
import com.example.demo.dto.*;
import com.example.demo.service.SalesReportService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class SalesReportController {

    private final SalesReportService salesReportService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardReportDTO> getDashboard() {
        return ResponseEntity.ok(salesReportService.getDashboard());
    }

    @GetMapping("/summary")
    public ResponseEntity<SalesReportSummaryDTO> getSummary(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return ResponseEntity.ok(salesReportService.getSummary(start, end));
    }

    @GetMapping("/monthly")
    public ResponseEntity<List<MonthlyReportDTO>> getMonthlyReport(
        @RequestParam(required = false) Integer year
    ) {
        if (year == null) {
            year = Year.now().getValue();
        }
        return ResponseEntity.ok(salesReportService.getMonthlyReport(year));
    }

    @GetMapping("/hourly")
    public ResponseEntity<List<HourlyReportDTO>> getHourlySales(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return ResponseEntity.ok(salesReportService.getHourlySales(start, end));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryReportDTO>> getSalesByCategory(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return ResponseEntity.ok(salesReportService.getSalesByCategory(start, end));
    }

    @GetMapping("/comparison")
    public ResponseEntity<YearComparisonDTO> getYearComparison(
        @RequestParam int year1,
        @RequestParam int year2
    ) {
        return ResponseEntity.ok(salesReportService.getYearComparison(year1, year2));
    }

    @GetMapping("/inventory-value")
    public ResponseEntity<InventoryValueDTO> getInventoryValue() {
        return ResponseEntity.ok(salesReportService.getInventoryValue());
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportExcel(@RequestParam(required = false) Integer year) {
        if (year == null) {
            year = Year.now().getValue();
        }
        byte[] data = salesReportService.exportExcel(year);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "cierre-gestion-" + year + ".xlsx");
        headers.setContentLength(data.length);
        return ResponseEntity.ok().headers(headers).body(data);
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf(@RequestParam(required = false) Integer year) {
        if (year == null) {
            year = Year.now().getValue();
        }
        byte[] data = salesReportService.exportPdf(year);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "cierre-gestion-" + year + ".pdf");
        headers.setContentLength(data.length);
        return ResponseEntity.ok().headers(headers).body(data);
    }

    @GetMapping("/sales-by-user")
    public ResponseEntity<List<UserSalesReportDTO>> getSalesByUser(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return ResponseEntity.ok(salesReportService.getSalesByUser(start, end));
    }

    @GetMapping("/sales-by-client")
    public ResponseEntity<List<ClientSalesReportDTO>> getSalesByClient(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return ResponseEntity.ok(salesReportService.getSalesByClient(start, end));
    }

    @GetMapping("/export/summary/excel")
    public ResponseEntity<byte[]> exportSummaryExcel(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        byte[] data = salesReportService.exportSummaryExcel(start, end);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "reporte-ventas-" + start + "-" + end + ".xlsx");
        headers.setContentLength(data.length);
        return ResponseEntity.ok().headers(headers).body(data);
    }

    @GetMapping("/export/summary/pdf")
    public ResponseEntity<byte[]> exportSummaryPdf(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        byte[] data = salesReportService.exportSummaryPdf(start, end);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "reporte-ventas-" + start + "-" + end + ".pdf");
        headers.setContentLength(data.length);
        return ResponseEntity.ok().headers(headers).body(data);
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getDeliveredOrders(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return ResponseEntity.ok(salesReportService.getDeliveredOrders(start, end));
    }
}
