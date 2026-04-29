package com.example.demo.service;

import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
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
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    public List<Order> getByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    @Transactional
    public Order create(Order order) {
        order.setDateTime(LocalDateTime.now());
        order.setStatus(Order.OrderStatus.PENDING);

        User user = userRepository.findById(order.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        order.setUser(user);

        if (order.getClient() != null && order.getClient().getId() != null) {
            Client client = clientRepository.findById(order.getClient().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
            order.setClient(client);
        } else {
            order.setClient(null);
        }

        double total = 0;
        for (OrderDetail detail : order.getDetails()) {
            Product product = productRepository.findById(detail.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id: " + detail.getProduct().getId()));

            // Los Inedible (llaveros, peluches) descuentan stock.
            // Los Edible (poke bowls) no tienen stock — se controlan por Supply.
            if (product instanceof Inedible inedible) {
                if (inedible.getStock() < detail.getQuantity()) {
                    throw new BadRequestException("Insufficient stock for product: " + product.getName()
                            + " (available: " + inedible.getStock() + ", requested: " + detail.getQuantity() + ")");
                }
                inedible.setStock(inedible.getStock() - detail.getQuantity());
            }

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
            throw new ResourceNotFoundException("Order not found with id: " + id);
        }
        orderRepository.deleteById(id);
    }
}
