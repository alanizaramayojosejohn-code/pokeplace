package com.example.demo.controller;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.dto.SalesReportSummaryDTO;
import com.example.demo.service.SalesReportService;
import lombok.RequiredArgsConstructor;

// SalesReportController.java
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class SalesReportController {

    private final SalesReportService salesReportService;

    // GET /api/reports/summary?start=2025-01-01&end=2025-01-31
    @GetMapping("/summary")
    public ResponseEntity<SalesReportSummaryDTO> getSummary(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return ResponseEntity.ok(salesReportService.getSummary(start, end));
    }
}