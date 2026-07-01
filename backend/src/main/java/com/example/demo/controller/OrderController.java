package com.example.demo.controller;

import com.example.demo.dto.OrderRequest;
import com.example.demo.dto.OrderResponse;
import com.example.demo.model.Client;
import com.example.demo.model.Order;
import com.example.demo.model.OrderDetail;
import com.example.demo.model.Product;
import com.example.demo.model.User;
import com.example.demo.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAll() {
        return ResponseEntity.ok(
                orderService.getAll().stream().map(OrderResponse::from).toList()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(OrderResponse.from(orderService.getById(id)));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponse>> getByStatus(@PathVariable Order.OrderStatus status) {
        return ResponseEntity.ok(
                orderService.getByStatus(status).stream().map(OrderResponse::from).toList()
        );
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest request) {
        Order order = toEntity(request);
        return ResponseEntity.ok(OrderResponse.from(orderService.create(order)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long id,
                                                      @RequestParam Order.OrderStatus status) {
        return ResponseEntity.ok(OrderResponse.from(orderService.updateStatus(id, status)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private Order toEntity(OrderRequest request) {
        User user = new User();
        user.setId(request.getUserId());

        Client client = null;
        if (request.getClientId() != null) {
            client = new Client();
            client.setId(request.getClientId());
        }

        List<OrderDetail> details = request.getDetails().stream().map(d -> {
            Product product = new Product();
            product.setId(d.getProductId());
            return OrderDetail.builder()
                    .product(product)
                    .quantity(d.getQuantity())
                    .build();
        }).toList();

        return Order.builder()
                .tableNumber(request.getTableNumber())
                .paymentMethod(request.getPaymentMethod())
                .notes(request.getNotes())
                .user(user)
                .client(client)
                .details(new java.util.ArrayList<>(details))
                .build();
    }
}
