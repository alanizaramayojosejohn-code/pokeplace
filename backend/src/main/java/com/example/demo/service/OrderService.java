package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;

    public List<Order> getAll() {
        return orderRepository.findAll();
    }

    public Order getById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
    }

    public List<Order> getByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    @Transactional
    public Order create(Order order) {
        // Asigna la fecha y hora actual
        order.setDateTime(LocalDateTime.now());
        order.setStatus(Order.OrderStatus.PENDING);

        // Verifica que el usuario existe
        User user = userRepository.findById(order.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        order.setUser(user);

        // Cliente es opcional
        if (order.getClient() != null && order.getClient().getId() != null) {
            Client client = clientRepository.findById(order.getClient().getId())
                    .orElseThrow(() -> new RuntimeException("Client not found"));
            order.setClient(client);
        }

        // Calcula el total y verifica los productos
        double total = 0;
        for (OrderDetail detail : order.getDetails()) {
            Product product = productRepository.findById(detail.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            detail.setProduct(product);
            detail.setSubtotal(product.getPrice() * detail.getQuantity());
            detail.setOrder(order);
            total += detail.getSubtotal();
        }

        order.setTotal(total);
        return orderRepository.save(order);
    }

    @Transactional
    public Order updateStatus(Long id, Order.OrderStatus status) {
        Order order = getById(id);
        order.setStatus(status);
        return orderRepository.save(order);
    }

    public void delete(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new RuntimeException("Order not found with id: " + id);
        }
        orderRepository.deleteById(id);
    }
}