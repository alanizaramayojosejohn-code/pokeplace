package com.example.demo.controller;

import com.example.demo.dto.PaymentRequest;
import com.example.demo.dto.PaymentResponse;
import com.example.demo.model.Payment;
import com.example.demo.model.User;
import com.example.demo.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAll() {
        return ResponseEntity.ok(
                paymentService.getAll().stream().map(PaymentResponse::from).toList()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(PaymentResponse.from(paymentService.getById(id)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentResponse>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(
                paymentService.getByUser(userId).stream().map(PaymentResponse::from).toList()
        );
    }

    @GetMapping("/range")
    public ResponseEntity<List<PaymentResponse>> getByRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(
                paymentService.getByPaymentDateBetween(start, end).stream()
                        .map(PaymentResponse::from).toList()
        );
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> create(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(PaymentResponse.from(paymentService.create(toEntity(request))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(PaymentResponse.from(paymentService.update(id, toEntity(request))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        paymentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private Payment toEntity(PaymentRequest request) {
        User user = new User();
        user.setId(request.getUserId());
        return Payment.builder()
                .user(user)
                .amount(request.getAmount())
                .paymentDate(request.getPaymentDate())
                .periodStart(request.getPeriodStart())
                .periodEnd(request.getPeriodEnd())
                .note(request.getNote())
                .build();
    }
}
